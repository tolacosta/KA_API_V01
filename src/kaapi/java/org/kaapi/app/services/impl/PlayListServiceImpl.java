package org.kaapi.app.services.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.jsoup.select.Evaluator.IsEmpty;
import org.kaapi.app.entities.Pagination;
import org.kaapi.app.entities.Playlist;
import org.kaapi.app.entities.PlaylistDetail;
import org.kaapi.app.entities.Video;
import org.kaapi.app.forms.FrmCreatePlaylist;
import org.kaapi.app.forms.FrmUpdatePlaylist;
import org.kaapi.app.forms.PlaylistDTO;
import org.kaapi.app.forms.RecommendedVideos;
import org.kaapi.app.services.PlayListServics;
import org.kaapi.app.utilities.Encryption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;






@Service("PlayListService")
public class PlayListServiceImpl implements PlayListServics{
	@Autowired
	DataSource dataSource;
	Connection con;
	
	
	@Override
	public ArrayList<Playlist> list(Pagination pagin, Playlist dto) {
		try {
			con = dataSource.getConnection();
			int begin =(pagin.getItem()*pagin.getPage())-pagin.getItem();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			String sql = " SELECT P.*, U.username, COUNT(DISTINCT PD.videoid) countvideos FROM TBLPLAYLIST P INNER JOIN TBLUSER U ON P.userid=U.userid"
							+" LEFT JOIN TBLPLAYlISTDETAIL PD ON P.playlistid=PD.playlistid"
							+" WHERE LOWER(P.playlistname) LIKE LOWER(?) and  U.Userid =? GROUP BY P.playlistid, U.username order by  P.playlistid desc offset ? limit ?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, "%"+dto.getPlaylistName()+"%");
			ps.setInt(2, Integer.parseInt(Encryption.decode(dto.getUserId())));
			ps.setInt(3, begin);
			ps.setInt(4, pagin.getItem());
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()){
				Playlist playlist = new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setCountVideos(rs.getInt("countvideos"));
				playlist.setPublicView(rs.getBoolean("publicview"));
				playlist.setStatus(rs.getBoolean("status"));
				playlists.add(playlist);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public ArrayList<Video> listVideoInPlaylist(String playlistid, Pagination pagin) {
		try {
			con = dataSource.getConnection();
			ArrayList<Video> playlists =new ArrayList<Video>();
			int begin =(pagin.getItem()*pagin.getPage())-pagin.getItem();
			ResultSet rs = null;
			String sql = "SELECT PL.*, V.*, U.USERNAME, CC.CATEGORYNAMES, COUNT(DISTINCT C.VIDEOID) COUNTCOMMENTS, COUNT(DISTINCT VP.*) COUNTVOTEPLUS, COUNT(DISTINCT VM.*) COUNTVOTEMINUS, PD.INDEX ,V.publicview  ispublic "
					+ "FROM TBLVIDEO V LEFT JOIN TBLUSER U ON V.USERID=U.USERID "
					+ "LEFT JOIN (SELECT CV.videoid, string_agg(CT.categoryname, ', ') CATEGORYNAMES FROM TBLCATEGORY CT LEFT JOIN TBLCATEGORYVIDEO CV ON CT.categoryid=CV.categoryid GROUP BY CV.videoid) CC ON V.videoid=CC.videoid "
					+ "LEFT JOIN TBLCOMMENT C ON V.VIDEOID=C.VIDEOID "
					+ "LEFT JOIN (SELECT * FROM TBLVOTE WHERE VOTETYPE=1) VP ON V.VIDEOID=VP.VIDEOID "
					+ "LEFT JOIN (SELECT * FROM TBLVOTE WHERE VOTETYPE=-1) VM ON V.VIDEOID=VM.VIDEOID "
					+ "INNER JOIN TBLPLAYLISTDETAIL PD ON PD.VIDEOID=V.VIDEOID "
					+ "INNER JOIN tblplaylist PL ON PD.PLAYLISTID = PL.playlistid "
					+ "WHERE PD.PLAYLISTID=? "
					+ "GROUP BY V.VIDEOID, U.USERNAME, CC.CATEGORYNAMES, PD.INDEX , PL.playlistid "
					+ "ORDER BY PD.INDEX desc   offset ? limit ? ";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(playlistid)));
			ps.setInt(2, begin);
			ps.setInt(3, pagin.getItem());
			rs = ps.executeQuery();
			Video dto=null;
			while(rs.next()){
				
				dto = new Video();
//				dto.setVideoId(rs.getInt("videoid"));
				dto.setVideoId(Encryption.encode(rs.getString("videoid")));
				dto.setVideoName(rs.getString("videoname"));
				dto.setDescription(rs.getString("description"));
				dto.setYoutubeUrl(rs.getString("youtubeurl"));
				dto.setFileUrl(rs.getString("fileurl"));
				dto.setPublicView(rs.getBoolean("publicview"));
				dto.setPostDate(rs.getDate("postdate"));
				dto.setUserId(Encryption.encode(rs.getString("userid")));
				dto.setViewCounts(rs.getInt("viewcount"));
				dto.setUsername(rs.getString("username"));
				dto.setCountVotePlus(rs.getInt("countvoteplus"));
				dto.setCountVoteMinus(rs.getInt("countvoteminus"));
				playlists.add(dto);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	
	
	//well
	@Override
	public String getPlaylistName(String playlistid) {
		try{	
				con = dataSource.getConnection();
				String pname= "";
				ResultSet rs = null;
				String sql = "select playlistname from tblplaylist where playlistid=?";
				PreparedStatement ps = con.prepareStatement(sql);
				ps.setInt(1, Integer.parseInt(Encryption.decode(playlistid)));
				rs = ps.executeQuery();
				if(rs.next()){
					pname = rs.getString("playlistname");
				}
				return pname;
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			return null;
	}

	//well
	@Override
	public ArrayList<Video> listVideo(String playlistid) {
		try {
			con = dataSource.getConnection();
			ArrayList<Video> playlists =new ArrayList<Video>();
			ResultSet rs = null;
			String sql = "SELECT V.*, U.USERNAME, CC.CATEGORYNAMES, COUNT(DISTINCT C.VIDEOID) COUNTCOMMENTS, COUNT(DISTINCT VP.*) COUNTVOTEPLUS, COUNT(DISTINCT VM.*) COUNTVOTEMINUS, PD.INDEX "
					+ "FROM TBLVIDEO V LEFT JOIN TBLUSER U ON V.USERID=U.USERID "
					+ "LEFT JOIN (SELECT CV.videoid, string_agg(CT.categoryname, ', ') CATEGORYNAMES FROM TBLCATEGORY CT LEFT JOIN TBLCATEGORYVIDEO CV ON CT.categoryid=CV.categoryid GROUP BY CV.videoid) CC ON V.videoid=CC.videoid "
					+ "LEFT JOIN TBLCOMMENT C ON V.VIDEOID=C.VIDEOID "
					+ "LEFT JOIN (SELECT * FROM TBLVOTE WHERE VOTETYPE=1) VP ON V.VIDEOID=VP.VIDEOID "
					+ "LEFT JOIN (SELECT * FROM TBLVOTE WHERE VOTETYPE=-1) VM ON V.VIDEOID=VM.VIDEOID "
					+ "INNER JOIN TBLPLAYLISTDETAIL PD ON PD.VIDEOID=V.VIDEOID "
					+ "WHERE PD.PLAYLISTID=? "
					+ "GROUP BY V.VIDEOID, U.USERNAME, CC.CATEGORYNAMES, PD.INDEX "
					+ "ORDER BY PD.INDEX";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(playlistid)));
			rs = ps.executeQuery();
			Video dto=null;
			while(rs.next()){
				dto = new Video();
				dto.setVideoId(Encryption.encode(rs.getString("videoid")));
				dto.setVideoName(rs.getString("videoname"));
				dto.setDescription(rs.getString("description"));
				dto.setYoutubeUrl(rs.getString("youtubeurl"));
				dto.setFileUrl(rs.getString("fileurl"));
				dto.setPublicView(rs.getBoolean("publicview"));
				dto.setPostDate(rs.getDate("postdate"));
				dto.setUserId(Encryption.encode(rs.getString("userid")));
				dto.setViewCounts(rs.getInt("viewcount"));
				dto.setUsername(rs.getString("username"));
				dto.setCountVotePlus(rs.getInt("countvoteplus"));
				dto.setCountVoteMinus(rs.getInt("countvoteminus"));
				playlists.add(dto);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	//well
	@Override
	public Playlist listplaylistname(Playlist dto) {
		try {
			con = dataSource.getConnection();
			//ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			Playlist playlist = new Playlist();
			ResultSet rs = null;
			String sql = "select playlistid , playlistname,publicview from tblplaylist where LOWER(playlistname) like  LOWER(?) and userid = ?  order by playlistid desc";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, "%"+dto.getPlaylistName()+"%");
			ps.setInt(2, Integer.parseInt(Encryption.decode(dto.getUserId())));
			rs = ps.executeQuery();
			while(rs.next()){
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setPublicView(rs.getBoolean("publicview"));
			}
			return playlist;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	//well but i don't see where this fucntion was used
	@Override
	public ArrayList<Playlist> listplaylistbyPublicView(boolean publicview) {
		try {
			con = dataSource.getConnection();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			String sql = "select playlistid,playlistname  from tblplaylist where publicview= ? order by playlistid desc";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setBoolean(1,publicview);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				
				Playlist playlist = new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlists.add(playlist);
				
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	//well
	@Override
	public ArrayList<Playlist> listplaylistbyAdmin(boolean publicview) {
		try {
				con = dataSource.getConnection();
				ArrayList<Playlist> playlists =new ArrayList<Playlist>();
				String sql = "select playlistid, playlistname from tblplaylist P inner join tbluser U on P.userid=U.userid inner join tblusertype UT on U.usertypeid=UT.usertypeid where publicview=? and UT.userable=true order by playlistid desc";
				PreparedStatement ps = con.prepareStatement(sql);
				ps.setBoolean(1,publicview);
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					
					Playlist playlist = new Playlist();
					playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
					playlist.setPlaylistName(rs.getString("playlistname"));
					playlists.add(playlist);
					
				}
				return playlists;
			} catch (SQLException e) {
			e.printStackTrace();
			}finally{
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			return null;
	}
	//well
	@Override
	public ArrayList<PlaylistDetail> listplaylistdetail(String userid) {
		try {
			con = dataSource.getConnection();
			ArrayList<PlaylistDetail> playlists =new ArrayList<PlaylistDetail>();
			ResultSet rs = null;
			String sql = "select D.playlistid , D.videoid , U.userid from TBLPLAYlISTDETAIL D "
					+ " INNER JOIN TBLPLAYLIST L ON D.playlistid = L.playlistid "
					+ " INNER JOIN TBLUSER U ON L.userid = L.userid "
					+ " Where U.Userid = ? order by D.Playlistid desc ";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(userid)));
			rs = ps.executeQuery();
			while(rs.next()){
				PlaylistDetail playlist = new PlaylistDetail();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setVideoId(Encryption.encode(rs.getString("videoid")));
				playlists.add(playlist);
			
				
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	//not sure
	@Override
	public ArrayList<Playlist> listplaylistdetail(String userid, String playlistid) {
		try {
			con = dataSource.getConnection();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			ResultSet rs = null;
			String sql = "select D.playlistid , D.videoid , U.userid from TBLPLAYlISTDETAIL D "
					+ " INNER JOIN TBLPLAYLIST L ON D.playlistid = L.playlistid "
					+ " INNER JOIN TBLUSER U ON L.userid = L.userid "
					+ " Where U.Userid = ? and D.playlistid = ? order by D.Playlistid desc ";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(userid)));
			ps.setInt(2, Integer.parseInt(Encryption.decode(playlistid)));
			rs = ps.executeQuery();
			while(rs.next()){
				
				Playlist playlist = new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setDescription(rs.getString("description"));
				playlist.setUserId(Encryption.encode(rs.getString("userid")));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setPublicView(rs.getBoolean("publicview"));
				playlist.setMaincategory(Encryption.encode(rs.getString("maincategory")));
				playlist.setBgImage(rs.getString("bgimage"));
				playlist.setColor(rs.getString("color"));
				playlist.setStatus(rs.getBoolean("status"));
				playlist.setUsername(rs.getString("username"));
				playlist.setUserImageUrl(rs.getString("userimageurl"));
				playlists.add(playlist);
				
			}
			return playlists;
			
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	//well
	@Override
	public Playlist get(String playlistid) {
		try {
			con = dataSource.getConnection();
			String sql = "SELECT P.*, U.username, COUNT(DISTINCT PD.videoid) countvideos FROM TBLPLAYLIST P INNER JOIN TBLUSER U ON P.userid=U.userid "
					+ "LEFT JOIN TBLPLAYlISTDETAIL PD ON P.playlistid=PD.playlistid "
					+ "WHERE P.playlistid=? GROUP BY P.playlistid, U.username";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(playlistid)));
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				Playlist dto = new Playlist();
				dto.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				dto.setPlaylistName(rs.getString("playlistname"));
				dto.setDescription(rs.getString("description"));
				dto.setUserId(Encryption.encode(rs.getString("userid")));
				dto.setThumbnailUrl(rs.getString("thumbnailurl"));
				dto.setPublicView(rs.getBoolean("publicview"));
				dto.setUsername(rs.getString("username"));
				dto.setCountVideos(rs.getInt("countvideos"));
				dto.setMaincategory(Encryption.encode(rs.getString("maincategory")));
				dto.setBgImage(rs.getString("bgimage"));
				dto.setColor(rs.getString("color"));
				dto.setStatus(rs.getBoolean("status"));
				return dto;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	//well
	@Override
	public Playlist getPlaylistForUpdate(String playlistid) {
		try {
			con = dataSource.getConnection();
			String sql = "select * from tblplaylist where playlistid = ?";
			PreparedStatement ps=con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(playlistid)));
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				Playlist playlist =new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setDescription(rs.getString("description"));
				playlist.setUserId(Encryption.encode(rs.getString("userid")));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setPublicView(rs.getBoolean("publicview"));
				playlist.setMaincategory(Encryption.encode(rs.getString("maincategory")));
				playlist.setBgImage(rs.getString("bgimage"));
				playlist.setColor(rs.getString("color"));
				playlist.setStatus(rs.getBoolean("status"));
				return playlist;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	//well
	@Override
	public boolean addVideoToPlst(String pid, String vid) {
		try {  
			con = dataSource.getConnection();
			PreparedStatement pstmt=con.prepareStatement("select max(index) from tblplaylistdetail");
			ResultSet rs = pstmt.executeQuery();
			int num = 1;
			if(rs.next())
				num =rs.getInt(1)+1;
			String sql = "INSERT INTO tblplaylistdetail VALUES( ?, ? , "+num+" )";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1,Integer.parseInt(Encryption.decode(pid )));
			ps.setInt(2, Integer.parseInt(Encryption.decode(vid )));
			if(ps.executeUpdate()>0){
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	//well
	@Override
	public boolean insert(FrmCreatePlaylist playlist) {
		try {
			con = dataSource.getConnection();
			String sql="";
			if(playlist.getMaincategory()!=null){
				 sql = "INSERT INTO TBLPLAYLIST VALUES(nextval('seq_playlist'), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			}else{
				 sql = "INSERT INTO TBLPLAYLIST VALUES(nextval('seq_playlist'), ?, ?, ?, ?, ?, null, ?, ?, ?)";
			}
			
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, playlist.getPlaylistName());
			ps.setString(2, playlist.getDescription());
			ps.setInt(3, Integer.parseInt(Encryption.decode(playlist.getUserId())));
			ps.setString(4, playlist.getThumbnailUrl());
			ps.setBoolean(5, playlist.isPublicView());
			if(playlist.getMaincategory()!=null){
				ps.setInt(6, Integer.parseInt(Encryption.decode(playlist.getMaincategory())));
				ps.setString(7, playlist.getBgImage());
				ps.setString(8, playlist.getColor());
				ps.setBoolean(9, playlist.isStatus());
			}else{
				ps.setString(6, playlist.getBgImage());
				ps.setString(7, playlist.getColor());
				ps.setBoolean(8, playlist.isStatus());
			}
			
			
			if(ps.executeUpdate()>0){
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	//well
	@Override
	public boolean update(FrmUpdatePlaylist playlist) {
		try {
			con = dataSource.getConnection();
			String sql = "UPDATE TBLPLAYLIST SET playlistname=?, description=?, thumbnailurl=?, publicview=?, maincategory=?, bgimage=?, color=?, status=? WHERE playlistid=?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, playlist.getPlaylistName());
			ps.setString(2, playlist.getDescription());
			ps.setString(3, playlist.getThumbnailUrl());
			ps.setBoolean(4, playlist.isPublicView());
			ps.setInt(5, Integer.parseInt(Encryption.decode(playlist.getMaincategory())));
			ps.setString(6, playlist.getBgImage());
			ps.setString(7, playlist.getColor());
			ps.setBoolean(8, playlist.isStatus());
			ps.setInt(9, Integer.parseInt(Encryption.decode(playlist.getPlaylistId())));
			if(ps.executeUpdate()>0){
//				System.out.println();
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	//well
	@Override
	public boolean delete(String playlistid) {
		try {
			con = dataSource.getConnection();
			String sql = "DELETE FROM TBLPLAYLIST WHERE playlistid=?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(playlistid)));
			if(ps.executeUpdate()>0){
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	//well
	@Override
	public int count(String keyword) {
		try {
			con = dataSource.getConnection();
			String sql = "SELECT COUNT(playlistid) FROM TBLPLAYLIST where LOWER(playlistname) like LOWER(?)";
			PreparedStatement ps=con.prepareStatement(sql);
			ps.setString(1, "%"+keyword+"%");
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				return rs.getInt(1); 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	//well
	@Override
	public int countUserPlaylist(String userid) {
		try {
			con = dataSource.getConnection();
			String sql = "SELECT COUNT(playlistid) FROM TBLPLAYLIST where  userid = ? and status=False";
			PreparedStatement ps=con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(userid)));
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				return rs.getInt(1); 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	//well
	/*
	 * after add video to playlistdetail
	 * will check if countvideos have playlistid ==1 so we process update thumnails
	 * 
	 * after delete from playlist
	 * will check if countvideos have playlistid ==0 so we process update thumnails to default
	 * 
	 */
	@Override
	public int countvideos(String playlistid) {
		try {
			con = dataSource.getConnection();
			String sql = "SELECT COUNT(videoid) FROM TBLPLAYLISTDETAIL WHERE playlistid=?";
			PreparedStatement ps=con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(playlistid)));
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				return rs.getInt(1); 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	//well
	@Override
	public ArrayList<Playlist> recommendPlaylist() {
		try {
			con = dataSource.getConnection();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			ResultSet rs = null;
			String sql= "select p.playlistid, p.playlistname,p.description, p.publicview,p.userid,p.thumbnailurl,p.maincategory,p.bgimage,p.color,p.status,u.username, u.userimageurl from tbluser u "
					+ "INNER JOIN tblplaylist p on u.userid= p.userid "
					+ "where p.userid=1 limit 10";
			PreparedStatement ps = con.prepareStatement(sql);
			rs=ps.executeQuery();
			while(rs.next()){
				Playlist playlist = new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setDescription(rs.getString("description"));
				playlist.setUserId(Encryption.encode(rs.getString("userid")));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setPublicView(rs.getBoolean("publicview"));
				playlist.setMaincategory(Encryption.encode(rs.getString("maincategory")));
				playlist.setBgImage(rs.getString("bgimage"));
				playlist.setColor(rs.getString("color"));
				playlist.setStatus(rs.getBoolean("status"));
				playlist.setUsername(rs.getString("username"));
				playlist.setUserImageUrl(rs.getString("userimageurl"));
				playlists.add(playlist);
				
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	//well
	@Override
	public boolean deleteVideoFromPlaylist(String playlistid, String vid) {
		try {
			con = dataSource.getConnection();
			String sql = "DELETE FROM tblplaylistdetail WHERE playlistid=? and videoid=?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(playlistid)));
			ps.setInt(2, Integer.parseInt(Encryption.decode(vid)));
			if(ps.executeUpdate()>0){
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	//well
	/*
	 * we use when insert data to playlistdetail
	 */
	@Override
	public boolean updateThumbnail(String vid, String pid) {
		try {
			con = dataSource.getConnection();
			String sql = "UPDATE TBLPLAYLIST SET  thumbnailurl=(select youtubeurl from tblvideo where videoid=? ) WHERE playlistid=?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(vid)));
			ps.setInt(2, Integer.parseInt(Encryption.decode(pid)));
			if(ps.executeUpdate()>0){
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	//well
	/*
	 * after delete playlist if playlist id =0 will update thumbnailur to default
	 */
	@Override
	public boolean updateThumbnailToDefault(String pid) {
		try {
			con = dataSource.getConnection();
			String sql = "UPDATE TBLPLAYLIST SET  thumbnailurl='default.png' WHERE playlistid=?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(pid)));
			if(ps.executeUpdate()>0){
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	//well
	@Override
	public ArrayList<Playlist> listAllPlaylist(Pagination pagin) {
		try {
			con = dataSource.getConnection();
			int begin =(pagin.getItem()*pagin.getPage())-pagin.getItem();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			ResultSet rs = null;
			String sql = " SELECT * FROM tblplaylist order by playlistid desc offset ? limit ?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, begin);
			ps.setInt(2, pagin.getItem());
			rs = ps.executeQuery();
			while(rs.next()){
				Playlist playlist = new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setDescription(rs.getString("description"));
				playlist.setUserId(Encryption.encode(rs.getString("userid")));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setPublicView(rs.getBoolean("publicview"));
				playlist.setMaincategory(Encryption.encode(rs.getString("maincategory")));
				playlist.setBgImage(rs.getString("bgimage"));
				playlist.setColor(rs.getString("color"));
				playlist.setStatus(rs.getBoolean("status"));
				playlist.setCountVideos(this.countVideoInPlayList(rs.getInt("playlistid")));
				playlists.add(playlist);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	@Override
	public ArrayList<Playlist> listMainPlaylist() {
		try {
			con = dataSource.getConnection();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			ResultSet rs = null;
			String sql = "SELECT P.playlistid, P.playlistname, P.description, P.userid, P.thumbnailurl, P.publicview, P.maincategory, P.bgimage, p.color, " 
							+ "P.status, M.maincategoryname " 
							+ "FROM tblplaylist P " 
									+ "INNER JOIN tblmaincategory M ON P.maincategory=M.maincategoryid " 
									+ "WHERE P.maincategory NOTNULL AND P.status=TRUE "; 
								
			PreparedStatement ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()){
				
				Playlist playlist = new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setDescription(rs.getString("description"));
				playlist.setUserId(Encryption.encode(rs.getString("userid")));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setPublicView(rs.getBoolean("publicview"));
				playlist.setMaincategory(Encryption.encode(rs.getString("maincategory")));
				playlist.setBgImage(rs.getString("bgimage"));
				playlist.setColor(rs.getString("color"));
				playlist.setStatus(rs.getBoolean("status"));
				playlist.setMaincategoryname(rs.getString("maincategoryname"));
				
				
				playlists.add(playlist);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	@Override
	public ArrayList<Playlist> litsMainElearning() {
		try {
			con = dataSource.getConnection();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			ResultSet rs = null;
			String sql = "SELECT DISTINCT(P.maincategory), M.maincategoryname "
							+"FROM tblplaylist P "
								+"INNER JOIN tblmaincategory M ON P.maincategory=M.maincategoryid "
								+"WHERE P.maincategory NOTNULL AND P.status=TRUE";
				
			PreparedStatement ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()){
				
				Playlist playlist = new Playlist();
				playlist.setMaincategory(Encryption.encode(rs.getString("maincategory")));
				playlist.setMaincategoryname(rs.getString("maincategoryname"));
				playlists.add(playlist);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	@Override
	public int countVideoInPlayList(int playlisid) {
		try {
			con = dataSource.getConnection();
			String sql = "SELECT COUNT(playlistid) AS total FROM tblplaylistdetail  " 
							+"WHERE playlistid = ?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, playlisid);
			ResultSet rs = ps.executeQuery();
			int total = 0;
			while(rs.next()){
				total = rs.getInt("total");
			}
			return total;
		} catch (SQLException e) {
//			System.out.println(e);
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
//				System.out.println(e);
			}
		}
		return 0;
	}
	@Override
	public ArrayList<Playlist> listPlayListByMainCategory(String categoryid) {
		try {
			con = dataSource.getConnection();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			ResultSet rs = null;
			String sql = "SELECT P.playlistid, P.playlistname, P.description, P.userid, P.thumbnailurl, P.publicview, P.maincategory, P.bgimage, p.color, " 
							+"P.status "  
							+"FROM tblplaylist P "
							+"WHERE P.maincategory =? AND P.status=TRUE ORDER BY P.index DESC"; 
								
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(categoryid)));
			rs = ps.executeQuery();
			while(rs.next()){
				
				Playlist playlist = new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setDescription(rs.getString("description"));
				playlist.setUserId(Encryption.encode(rs.getString("userid")));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setPublicView(rs.getBoolean("publicview"));
				playlist.setMaincategory(Encryption.encode(rs.getString("maincategory")));
				playlist.setBgImage(rs.getString("bgimage"));
				playlist.setColor(rs.getString("color"));
				playlist.setStatus(rs.getBoolean("status"));
				playlist.setCountVideos(this.countVideoInPlayList(rs.getInt("playlistid")));
				playlists.add(playlist);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	
	@Override
	public ArrayList<Playlist> searchPlayList(String kesearch, Pagination pagin) {
		try {
			con = dataSource.getConnection();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			int begin =(pagin.getItem()*pagin.getPage())-pagin.getItem();
			
			ResultSet rs = null;
			String sql = "SELECT * FROM tblplaylist P "
							+"WHERE LOWER(P.playlistname) LIKE LOWER(?) AND status=true "
							+"order by playlistid desc offset ? limit ?"; 
								
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, "%"+kesearch+"%");
			ps.setInt(2, begin);
			ps.setInt(3, pagin.getItem());
			rs = ps.executeQuery();
			while(rs.next()){
				
				Playlist playlist = new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setDescription(rs.getString("description"));
				playlist.setUserId(Encryption.encode(rs.getString("userid")));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setPublicView(rs.getBoolean("publicview"));
				if(rs.getString("maincategory")!=null){
					playlist.setMaincategory(Encryption.encode(rs.getString("maincategory")));					
				}
				playlist.setBgImage(rs.getString("bgimage"));
				playlist.setColor(rs.getString("color"));
				playlist.setStatus(rs.getBoolean("status"));
				playlist.setCountVideos(this.countVideoInPlayList(rs.getInt("playlistid")));
				playlists.add(playlist);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	@Override
	public int countSearchPlayList(String kesearch) {
		try {
			con = dataSource.getConnection();
			String sql = "SELECT COUNT(playlistid) FROM TBLPLAYLIST P WHERE LOWER(P.playlistname) LIKE LOWER(?) AND status=true";
			PreparedStatement ps=con.prepareStatement(sql);
			ps.setString(1, "%"+kesearch+"%");
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				return rs.getInt(1); 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	
	@Override
	public ArrayList<Video> listVideoInPlaylist(String playlistid) {
		try {
			con = dataSource.getConnection();
			ArrayList<Video> playlists =new ArrayList<Video>();
			ResultSet rs = null;
			String sql = "SELECT PL.*, V.*, U.USERNAME, CC.CATEGORYNAMES, COUNT(DISTINCT C.VIDEOID) COUNTCOMMENTS, COUNT(DISTINCT VP.*) COUNTVOTEPLUS, COUNT(DISTINCT VM.*) COUNTVOTEMINUS, PD.INDEX ,V.publicview  ispublic "
					+ "FROM TBLVIDEO V LEFT JOIN TBLUSER U ON V.USERID=U.USERID "
					+ "LEFT JOIN (SELECT CV.videoid, string_agg(CT.categoryname, ', ') CATEGORYNAMES FROM TBLCATEGORY CT LEFT JOIN TBLCATEGORYVIDEO CV ON CT.categoryid=CV.categoryid GROUP BY CV.videoid) CC ON V.videoid=CC.videoid "
					+ "LEFT JOIN TBLCOMMENT C ON V.VIDEOID=C.VIDEOID "
					+ "LEFT JOIN (SELECT * FROM TBLVOTE WHERE VOTETYPE=1) VP ON V.VIDEOID=VP.VIDEOID "
					+ "LEFT JOIN (SELECT * FROM TBLVOTE WHERE VOTETYPE=-1) VM ON V.VIDEOID=VM.VIDEOID "
					+ "INNER JOIN TBLPLAYLISTDETAIL PD ON PD.VIDEOID=V.VIDEOID "
					+ "INNER JOIN tblplaylist PL ON PD.PLAYLISTID = PL.playlistid "
					+ "WHERE PD.PLAYLISTID=? "
					+ "GROUP BY V.VIDEOID, U.USERNAME, CC.CATEGORYNAMES, PD.INDEX , PL.playlistid "
					+ "ORDER BY PD.INDEX ";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(playlistid)));
			rs = ps.executeQuery();
			Video dto=null;
			while(rs.next()){
				
				dto = new Video();
//				dto.setVideoId(rs.getInt("videoid"));
				dto.setVideoId(Encryption.encode(rs.getString("videoid")));
				dto.setVideoName(rs.getString("videoname"));
				dto.setDescription(rs.getString("description"));
				dto.setYoutubeUrl(rs.getString("youtubeurl"));
				dto.setFileUrl(rs.getString("fileurl"));
				dto.setPublicView(rs.getBoolean("publicview"));
				dto.setPostDate(rs.getDate("postdate"));
				dto.setUserId(Encryption.encode(rs.getString("userid")));
				dto.setViewCounts(rs.getInt("viewcount"));
				dto.setUsername(rs.getString("username"));
				dto.setCountVotePlus(rs.getInt("countvoteplus"));
				dto.setCountVoteMinus(rs.getInt("countvoteminus"));
				playlists.add(dto);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	@Override
	public ArrayList<Playlist> listUserPlayList(String userid) {
		try {
			con = dataSource.getConnection();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			Playlist playlist = null;
			ResultSet rs = null;
			String sql = "select playlistid , playlistname, thumbnailurl ,publicview from tblplaylist where userid = ?  order by playlistid desc";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(userid)));
			rs = ps.executeQuery();
			while(rs.next()){
				playlist =new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setCountVideos(this.countVideoInPlayList(rs.getInt("playlistid")));
				playlist.setPublicView(rs.getBoolean("publicview"));
				playlists.add(playlist);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	@Override
	public int countPlayList() {
		String sql = "SELECT COUNT(P.playlistid) FROM tblplaylist P";
		try (Connection cnn = dataSource.getConnection(); PreparedStatement ps = cnn.prepareStatement(sql);) {
			ResultSet rs = ps.executeQuery();
			if(rs.next()) return rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
		
	}
	@Override
	public ArrayList<Playlist> UserPlayList(String userid, Pagination pagin) {
		try {
			con = dataSource.getConnection();
			int begin =(pagin.getItem()*pagin.getPage())-pagin.getItem();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			Playlist playlist = null;
			ResultSet rs = null;
			String sql="";
	
				 sql = "select * from tblplaylist P where P.userid = ? AND P.status = false   order by playlistid desc offset ? limit ?";
		
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(userid)));
			ps.setInt(2, begin);
			ps.setInt(3, pagin.getItem());
			rs = ps.executeQuery();
			while(rs.next()){
				playlist =new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setDescription(rs.getString("description"));
				playlist.setUserId(Encryption.encode(rs.getString("userid")));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				
				playlist.setMaincategory(Encryption.encode(rs.getString("maincategory")));
				playlist.setCountVideos(this.countVideoInPlayList(rs.getInt("playlistid")));
				playlist.setPublicView(rs.getBoolean("publicview"));
				playlist.setBgImage(rs.getString("bgimage"));
				playlist.setStatus(rs.getBoolean("status"));
				playlists.add(playlist);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	@Override
	public int countUserPlaylist(String userid, String pname) {
		try {
			con = dataSource.getConnection();
			String sql = "SELECT COUNT(playlistid) FROM TBLPLAYLIST where  userid = ? and status=TRUE and LOWER(playlistname) LIKE LOWER(?)";
			PreparedStatement ps=con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(userid)));
			ps.setString(2, "%"+pname+"%");
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				return rs.getInt(1); 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	@Override
	public ArrayList<Playlist> UserPlayList(String userid) {
		try {
			con = dataSource.getConnection();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			Playlist playlist = null;
			ResultSet rs = null;
			String sql = "select playlistid , playlistname, thumbnailurl ,publicview, status from tblplaylist P where P.status=TRUE and P.userid = ?   order by playlistid desc ";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(userid)));
			rs = ps.executeQuery();
			while(rs.next()){
				playlist =new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setCountVideos(this.countVideoInPlayList(rs.getInt("playlistid")));
				playlist.setPublicView(rs.getBoolean("publicview"));
				playlist.setStatus(rs.getBoolean("status"));
				playlists.add(playlist);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public boolean togglePlaylist(String playlistId) {
		String sql = "UPDATE tblplaylist SET status=not status WHERE playlistid=?";
		try (Connection cnn = dataSource.getConnection(); PreparedStatement ps = cnn.prepareStatement(sql);) {
			ps.setInt(1, Integer.parseInt(Encryption.decode(playlistId)));
			if(ps.executeUpdate()>0){
				return true;
			}	
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e1){
			return false;
		}
		return false;
	}

	
	@Override
	public List<Playlist> listRecentPlaylists(String mainCategoryId) {
		try {
			con = dataSource.getConnection();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			Playlist playlist = null;
			ResultSet rs = null;
			String sql = "SELECT A.playlistid, A.playlistname, A.description, A.userid,  A.bgimage, A.color, A.thumbnailurl, A.status "
					   + " ,(SELECT videoid from tblplaylistdetail where playlistid=A.playlistid and index=(select min(index) from tblplaylistdetail where playlistid=A.playlistid) ) as videoid " 
					   + " ,( select COUNT(videoid) FROM tblplaylistdetail where playlistid = A.playlistid GROUP BY playlistid ) as conutvideo "
					   + " FROM tblplaylist A "
					   + " WHERE   A.status = true " + ((mainCategoryId=="") ? "" : " AND maincategory= "+ mainCategoryId+ " ")
					   + " ORDER BY 1 DESC "
					   + " LIMIT 10";
			PreparedStatement ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()){
				playlist =new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setDescription(rs.getString("description"));
				playlist.setUserId(rs.getString("userid"));
				playlist.setBgImage(rs.getString("bgimage"));
				playlist.setColor(rs.getString("color"));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setStatus(rs.getBoolean("status"));
				playlist.setVideoId(Encryption.encode(rs.getString("videoid")));
				playlist.setCountVideos(rs.getInt("conutvideo"));
				playlists.add(playlist);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	@Override
	public ArrayList<Playlist> listPlaylistByUseridPlaylistNameMainCategoryName(Playlist p, Pagination pagin) {
		try {
			con = dataSource.getConnection();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			int begin =(pagin.getItem()*pagin.getPage())-pagin.getItem();
			Playlist playlist = null;
			ResultSet rs = null;
			String sql =  " SELECT P.*, U.username, COUNT(DISTINCT PD.videoid) countvideos , M.maincategoryname"
					+ "	FROM TBLPLAYLIST P INNER JOIN TBLUSER U ON P.userid=U.userid"
					+ "	INNER JOIN TBLMAINCATEGORY M ON P.maincategory = M.maincategoryid"
					+ " LEFT JOIN TBLPLAYlISTDETAIL PD ON P.playlistid=PD.playlistid"
					+ " WHERE  LOWER(P.playlistname) LIKE   LOWER(?)  AND  LOWER(m.maincategoryname) LIKE  LOWER(?) and U.Userid = ? GROUP BY P.playlistid, U.username,m.maincategoryname order by  P.playlistid"
					+ " desc  offset ? limit ?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, "%"+p.getPlaylistName()+"%");
			ps.setString(2, "%"+p.getMaincategoryname()+"%");
			ps.setInt(3, Integer.parseInt(Encryption.decode(p.getUserId())));
			ps.setInt(4, begin);
			ps.setInt(5, pagin.getItem());
			rs = ps.executeQuery();
			while(rs.next()){
				playlist =new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setBgImage(rs.getString("bgimage"));
				playlist.setColor(rs.getString("color"));
//				playlist.setCountVideos(rs.getInt("countvideos"));
				playlist.setStatus(rs.getBoolean("status"));
				playlist.setUsername(rs.getString("username"));
				playlists.add(playlist);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	
	@Override
	public int countPlaylistByUseridPlaylistNameMainCategoryName(Playlist p) {
//		System.out.println("123 "+ p.getUserId() + " PN " + p.getPlaylistName() + " MN "+ p.getMaincategoryname() );
		String sql = "SELECT COUNT(P.playlistid)"
				+ " FROM TBLPLAYLIST P INNER JOIN TBLUSER U ON P.userid=U.userid"
				+ " INNER JOIN TBLMAINCATEGORY M ON P.maincategory = M.maincategoryid"
				+ " LEFT JOIN TBLPLAYlISTDETAIL PD ON P.playlistid=PD.playlistid"
				+ " WHERE  LOWER(P.playlistname) LIKE   LOWER(?)  AND  LOWER(m.maincategoryname) LIKE  LOWER(?) and U.Userid = ? ";
		try (Connection cnn = dataSource.getConnection(); 
			PreparedStatement ps = cnn.prepareStatement(sql);) {
			ps.setString(1, "%"+p.getPlaylistName()+"%");
			ps.setString(2, "%"+p.getMaincategoryname()+"%");
			ps.setInt(3, Integer.parseInt(Encryption.decode(p.getUserId())));
			ResultSet rs = ps.executeQuery();
			if(rs.next()) 
				return rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
		
	}

	@Override
	public int countSearchPlayListMobile(String kesearch) {
		try {
			con = dataSource.getConnection();
			String sql = "SELECT COUNT(playlistid) FROM TBLPLAYLIST P WHERE LOWER(P.playlistname) LIKE LOWER(?) AND P.status = TRUE";
			PreparedStatement ps=con.prepareStatement(sql);
			ps.setString(1, "%"+kesearch+"%");
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				return rs.getInt(1); 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	@Override
	public ArrayList<Playlist> searchPlayListMobile(String kesearch, Pagination pagin) {
		try {
			con = dataSource.getConnection();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			int begin =(pagin.getItem()*pagin.getPage())-pagin.getItem();
			
			ResultSet rs = null;
			String sql = "SELECT P.playlistid, P.playlistname, P.description, P.userid, U.email, U.username, P.bgimage, P.color, P.thumbnailurl, P.status"
					+ " ,(SELECT videoid from tblplaylistdetail where playlistid=P.playlistid and index=(select min(index) from tblplaylistdetail where playlistid=P.playlistid) )"
					+ " ,( select COUNT(videoid) FROM tblplaylistdetail where playlistid = P.playlistid GROUP BY P.playlistid ) as conutvideo "
					+ " FROM tblplaylist P"
					+ " INNER JOIN tbluser u ON u.userid = P.userid"
					+ " WHERE LOWER(P.playlistname) LIKE LOWER(?) AND P.status = TRUE"
					+ " order by playlistid desc offset ? limit ?"; 
								
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, "%"+kesearch+"%");
			ps.setInt(2, begin);
			ps.setInt(3, pagin.getItem());
			rs = ps.executeQuery();
			while(rs.next()){
				
				Playlist playlist = new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setDescription(rs.getString("description"));
				playlist.setUserId(Encryption.encode(rs.getString("userid")));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setBgImage(rs.getString("bgimage"));
				playlist.setColor(rs.getString("color"));
				playlist.setStatus(rs.getBoolean("status"));
				playlist.setCountVideos(rs.getInt("conutvideo"));
				playlist.setUsername(rs.getString("username"));
				playlist.setVideoId(Encryption.encode(rs.getString("videoid")));
				playlists.add(playlist);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	@Override
	public List<Playlist> recommendedCourses(String userid) {
		try {
			con = dataSource.getConnection();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			Playlist playlist = null;
			ResultSet rs = null;
			String sql = "SELECT A.userid, B.playlistid , COUNT(A.videoid) as watched, P.playlistname, P.description ,  P.thumbnailurl, A.videoid"
					+ " ,( select COUNT(videoid) FROM tblplaylistdetail where playlistid = B.playlistid GROUP BY playlistid ) as conutvideo "
					+ " FROM tbllog A"
					+ " LEFT JOIN tblplaylistdetail B ON A.videoid = B.videoid"
					+ " LEFT JOIN tblplaylist P ON B.playlistid = P.playlistid"
					+ " WHERE A.userid=?  AND P.status=true GROUP BY 1,2,4,5,6,7 ORDER BY watched DESC LIMIT 8;";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1,Integer.parseInt(Encryption.decode(userid)));
			rs = ps.executeQuery();
			while(rs.next()){
				playlist =new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setDescription(rs.getString("description"));
				playlist.setUserId(rs.getString("userid"));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setVideoId(Encryption.encode(rs.getString("videoid")));
				playlist.setCountVideos(rs.getInt("conutvideo"));
				playlists.add(playlist);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	@Override
	public List<RecommendedVideos> recommendedVideos(String userid) {
		try {
			con = dataSource.getConnection();
			ArrayList<RecommendedVideos> videos =new ArrayList<RecommendedVideos>();
			RecommendedVideos video = null;
			ResultSet rs = null;
			String sql = "SELECT A.userid, B.playlistid , COUNT(A.videoid) as watched,  P.playlistname ,A.videoid , v.videoname, v.description, v.viewcount,v.youtubeurl "
					+ " FROM tbllog A"
					+ " LEFT JOIN tblplaylistdetail B ON A.videoid = B.videoid"
					+ " LEFT JOIN tblvideo v ON A.videoid = v.videoid"
					+ " LEFT JOIN tblplaylist P ON B.playlistid = P.playlistid"
					+ " WHERE A.userid=? AND P.maincategory <> 0 AND P.status=true GROUP BY 1,2,4,5,6,7,8,9 ORDER BY watched DESC LIMIT 20;";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(userid)));
			rs = ps.executeQuery();
			while(rs.next()){
				video = new RecommendedVideos();
				video.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				video.setVideoId(Encryption.encode(rs.getString("videoid")));
				video.setVideoTitle(rs.getString("videoname"));
				video.setPlaylistName(rs.getString("playlistname"));
				video.setDescription(rs.getString("description"));
				video.setUserId(rs.getString("userid"));
				video.setYoutubeUrl(rs.getString("youtubeurl"));
				video.setView(rs.getInt("viewcount"));
				videos.add(video);
			}
			return videos;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public List<RecommendedVideos> listVideoInPlaylists(String playlistid) {
		try {
			con = dataSource.getConnection();
			ArrayList<RecommendedVideos> videos =new ArrayList<RecommendedVideos>();
			RecommendedVideos video = null;
			ResultSet rs = null;
			String sql = "SELECT PD.*,P.playlistname, V.userid,V.videoname,V.youtubeurl,V.viewcount,V.description"
					+ " FROM tblplaylistdetail PD"
					+ " LEFT JOIN tblplaylist P ON PD.playlistid = P.playlistid"
					+ " LEFT JOIN tblvideo V ON PD.videoid = V.videoid"
					+ " WHERE  P.status=true and P.maincategory<>0 AND PD.playlistid=?"
					+ " ORDER BY PD.index ASC,P.playlistid DESC ";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(Encryption.decode(playlistid)));
			rs = ps.executeQuery();
			while(rs.next()){
				video = new RecommendedVideos();
				video.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				video.setVideoId(Encryption.encode(rs.getString("videoid")));
				video.setVideoTitle(rs.getString("videoname"));
				video.setPlaylistName(rs.getString("playlistname"));
				video.setDescription(rs.getString("description"));
				video.setUserId(rs.getString("userid"));
				video.setYoutubeUrl(rs.getString("youtubeurl"));
				video.setView(rs.getInt("viewcount"));
				videos.add(video);
			}
			return videos;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public List<Playlist> listPlaylistsByMainCategory(String mainCategoryId) {
		try {
			con = dataSource.getConnection();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			Playlist playlist = null;
			ResultSet rs = null;
			String sql = "SELECT A.playlistid, A.playlistname, A.description, A.userid, A.bgimage, A.color, A.thumbnailurl, A.status ,MC.maincategoryid "
					   + ",(SELECT videoid from tblplaylistdetail where playlistid=A.playlistid and index=(select min(index) from tblplaylistdetail where playlistid=A.playlistid) ) as videoid " 
					   + ",MC.maincategoryname "
						+ " ,( select COUNT(videoid) FROM tblplaylistdetail where playlistid =A.playlistid GROUP BY playlistid ) as conutvideo "
					   + "FROM tblplaylist A "
					   + "INNER JOIN tblmaincategory MC ON A.maincategory = MC.maincategoryid "
					   + "WHERE A.status = true " + ((Encryption.decode(mainCategoryId)=="") ? "" : " AND maincategory= "+ Encryption.decode(mainCategoryId)+ " ")
					   + "ORDER BY 1 DESC ";
//					   + "LIMIT 12";
			PreparedStatement ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()){
				playlist =new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setDescription(rs.getString("description"));
				playlist.setUserId(rs.getString("userid"));
				playlist.setBgImage(rs.getString("bgimage"));
				playlist.setColor(rs.getString("color"));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setStatus(rs.getBoolean("status"));
				playlist.setVideoId(Encryption.encode(rs.getString("videoid")));
				playlist.setMaincategoryname(rs.getString("maincategoryname"));
				playlist.setMaincategory(Encryption.encode(rs.getString("maincategoryid")));
				playlist.setCountVideos(rs.getInt("conutvideo"));
				playlists.add(playlist);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	
	@Override
	public List<RecommendedVideos> mostViewedVideos() {
		try {
			con = dataSource.getConnection();
			ArrayList<RecommendedVideos> videos =new ArrayList<RecommendedVideos>();
			RecommendedVideos video = null;
			ResultSet rs = null;
			String sql = "SELECT A.userid, B.playlistid ,  P.playlistname ,A.videoid , v.videoname, v.description, v.viewcount,v.youtubeurl "
					+ " FROM tblvideo A"
					+ " LEFT JOIN tblplaylistdetail B ON A.videoid = B.videoid"
					+ " LEFT JOIN tblvideo v ON A.videoid = v.videoid"
					+ " LEFT JOIN tblplaylist P ON B.playlistid = P.playlistid"
					+ " WHERE  P.maincategory <> 0 AND P.status=true ORDER BY A.viewcount DESC LIMIT 12;";
			PreparedStatement ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()){
				video = new RecommendedVideos();
				video.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				video.setVideoId(Encryption.encode(rs.getString("videoid")));
				video.setVideoTitle(rs.getString("videoname"));
				video.setPlaylistName(rs.getString("playlistname"));
				video.setDescription(rs.getString("description"));
				video.setUserId(rs.getString("userid"));
				video.setYoutubeUrl(rs.getString("youtubeurl"));
				video.setView(rs.getInt("viewcount"));
				videos.add(video);
			}
			return videos;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	@Override
	public List<Playlist> listPlaylistsByMainCategoryWithPagin(String mainCategoryId,Pagination pagin) {
		try {
			String mID = "";
			if(!mainCategoryId.equals("empty")){
				mID = Encryption.decode(mainCategoryId);
			}
			con = dataSource.getConnection();
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			Playlist playlist = null;
			ResultSet rs = null;
			String sql = " SELECT A.playlistid, A.playlistname, A.description, A.userid, A.bgimage, A.color, A.thumbnailurl, A.status ,MC.maincategoryid "
					   + ",(SELECT videoid from tblplaylistdetail where playlistid=A.playlistid and index=(select min(index) from tblplaylistdetail where playlistid=A.playlistid) ) as videoid " 
					   + ", MC.maincategoryname "
						+ " ,( select COUNT(videoid) FROM tblplaylistdetail where playlistid =A.playlistid GROUP BY playlistid ) as conutvideo "
					   + " FROM tblplaylist A "
					   + " INNER JOIN tblmaincategory MC ON A.maincategory = MC.maincategoryid "
					   + " WHERE A.status = true " + ((mID=="") ? "" : " AND maincategory= "+ mID+ " ")
					   + " ORDER BY 1 DESC "
					   + " LIMIT ? OFFSET ?" ;
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, pagin.getItem());
			ps.setInt(2, pagin.offset());
			rs = ps.executeQuery();
			while(rs.next()){
				playlist =new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setDescription(rs.getString("description"));
				playlist.setUserId(rs.getString("userid"));
				playlist.setBgImage(rs.getString("bgimage"));
				playlist.setColor(rs.getString("color"));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setStatus(rs.getBoolean("status"));
				playlist.setVideoId(Encryption.encode(rs.getString("videoid")));
				playlist.setMaincategoryname(rs.getString("maincategoryname"));
				playlist.setMaincategory(Encryption.encode(rs.getString("maincategoryid")));
				playlist.setCountVideos(rs.getInt("conutvideo"));
				playlists.add(playlist);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public int countPlaylists(String mainCategoryId) {
		try {
			String mID = "";
			if(!mainCategoryId.equals("empty")){
				mID = Encryption.decode(mainCategoryId);
			}
			con = dataSource.getConnection();
			String sql = "SELECT COUNT(A.playlistid)"
					+ " FROM tblplaylist A"
					+ " INNER JOIN tblmaincategory MC ON A.maincategory = MC.maincategoryid"
					+ " WHERE A.status = true  " + ((mID=="") ? "" : " AND maincategory= "+ mID+ " ");
			PreparedStatement ps=con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				return rs.getInt(1); 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	@Override
	public List<PlaylistDTO> listPlaylistDTOByMainCategoryWithPagin(String mainCategoryId, Pagination pagin) {
		try {
			String mID = "";
			if(!mainCategoryId.equals("empty")){
				mID = Encryption.decode(mainCategoryId);
			}
			con = dataSource.getConnection();
			ArrayList<PlaylistDTO> playlists =new ArrayList<PlaylistDTO>();
			PlaylistDTO playlist = null;
			ResultSet rs = null;
			String sql = " SELECT A.playlistid, A.playlistname, A.description, A.thumbnailurl,(SELECT videoid from tblplaylistdetail where playlistid=A.playlistid and index=(select min(index) from tblplaylistdetail where playlistid=A.playlistid) ) as videoid "
					   + " FROM tblplaylist A "
					   + " INNER JOIN tblmaincategory MC ON A.maincategory = MC.maincategoryid "
					   + " WHERE A.status = true " + ((mID=="") ? "" : " AND maincategory= "+ mID+ " ")
					   + " ORDER BY 1 DESC "
					   + " LIMIT ? OFFSET ?" ;
			/*String sql = "SELECT"
							+ "	AAA.playlistid,"
							+ "	AAA.playlistname,"
							+ "	AAA.thumbnailurl,"
							+ "	BBB.videoid,"
							+ " AAA.maincategory "
						+ "	FROM"
						+ "	("
						+ "		SELECT"
						+ "			playlistid,"
						+ "			playlistname,"
						+ "			thumbnailurl,"
						+ "			description, maincategory"
						+ "		FROM"
						+ "			tblplaylist"
						+ "		WHERE"
						+ "			status = TRUE"
						+ "		ORDER BY"
						+ "			playlistid DESC"
						+ "		LIMIT ? OFFSET ? "
						+ "	) AAA,"
						+ "	("
						+ "		SELECT"
						+ "			playlistid,"
						+ "			MIN (videoid) videoid"
						+ "		FROM"
						+ "			tblplaylistdetail"
						+ "		GROUP BY"
						+ "			playlistid"
						+ "	) BBB"
						+ "	WHERE"
						+ "		AAA.playlistid = BBB.playlistid " + ((mID=="") ? "" : " AND AAA.maincategory= "+ mID+ " ");*/
			
			PreparedStatement ps = con.prepareStatement(sql);
			System.out.println("SQL ======> "+ sql);
			ps.setInt(1, pagin.getItem());
			ps.setInt(2, pagin.offset());
			rs = ps.executeQuery();
			while(rs.next()){
				playlist =new PlaylistDTO();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
//				playlist.setDescription(rs.getString("description"));
//				System.out.println(rs.getString("videoid"));
				playlist.setVideoId(Encryption.encode(rs.getString("videoid")));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlists.add(playlist);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	
	
	
}
