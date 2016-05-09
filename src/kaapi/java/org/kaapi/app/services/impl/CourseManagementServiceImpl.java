package org.kaapi.app.services.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.kaapi.app.entities.CourseVideoManagement;
import org.kaapi.app.entities.Pagination;
import org.kaapi.app.entities.Playlist;
import org.kaapi.app.forms.FrmUpdatePlaylist;
import org.kaapi.app.services.CourseManagementService;
import org.kaapi.app.utilities.Encryption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

@Repository
@PropertySource(
		value={"classpath:applications.properties"}
)
public class CourseManagementServiceImpl implements CourseManagementService{

	@Autowired
	DataSource dataSource;
	
	@Autowired
	private Environment environment;
	
	@Override
	public ArrayList<Playlist> listCourses(String playlistName , String mainCategoryId,Pagination pagination) {
		if(mainCategoryId.equals("empty")){
			mainCategoryId = "";
		}else{
			mainCategoryId = Encryption.decode(mainCategoryId);
		}
		String sql = "SELECT A.playlistid, A.playlistname, A.description, A.userid, B.email, B.username, A.bgimage, A.color, A.thumbnailurl, A.status ,MC.maincategoryid "
				   + ",(SELECT videoid from tblplaylistdetail where playlistid=A.playlistid and index=(select min(index) from tblplaylistdetail where playlistid=A.playlistid) ) as videoid " 
				   + ",MC.maincategoryname "
				   + " ,( select COUNT(videoid) FROM tblplaylistdetail where playlistid = A.playlistid GROUP BY playlistid ) as conutvideo "
				   + " FROM tblplaylist A "
				   + " INNER JOIN tbluser B ON A.userid = B.userid "
				   + " INNER JOIN tblmaincategory MC ON A.maincategory = MC.maincategoryid "
				   + " WHERE A.maincategory <> 0 AND   LOWER(A.playlistname) LIKE  LOWER(?) " + ((mainCategoryId=="") ? "" : " AND A.maincategory= "+ mainCategoryId+ " ")
				   + " ORDER BY A.playlistid DESC "
				   + " offset ? limit ?";
		try (Connection cnn = dataSource.getConnection(); PreparedStatement ps = cnn.prepareStatement(sql);) {
			ArrayList<Playlist> playlists =new ArrayList<Playlist>();
			ps.setString(1,"%"+playlistName+"%");
			System.out.println(playlistName);
			ps.setInt(2,pagination.offset());
			ps.setInt(3, pagination.getItem());
			Playlist playlist = null;
			ResultSet rs = null;
			rs = ps.executeQuery();
			while(rs.next()){
				playlist =new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setDescription(rs.getString("description"));
				playlist.setUsername(rs.getString("username"));
				playlist.setUserId(rs.getString("userid"));
				playlist.setBgImage(rs.getString("bgimage"));
				playlist.setColor(rs.getString("color"));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setStatus(rs.getBoolean("status"));
				playlist.setVideoId(Encryption.encode(rs.getString("videoid")));
				playlist.setCountVideos(rs.getInt("conutvideo"));
				playlist.setMaincategoryname(rs.getString("maincategoryname"));
				playlist.setMaincategory(Encryption.encode(rs.getString("maincategoryid")));
				playlists.add(playlist);
			}
			return playlists;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public int countCourse(String playlistName,String mainCategoryId) {
		if(mainCategoryId.equals("empty")){
			mainCategoryId = "";
		}else{
			mainCategoryId = Encryption.decode(mainCategoryId);
		}
		String sql = "SELECT COUNT(playlistid) FROM TBLPLAYLIST where   maincategory <> 0 AND   LOWER(playlistname) LIKE  LOWER(?)   " + ((mainCategoryId=="") ? "" : " AND maincategory= "+ mainCategoryId+ " ");	
		try (Connection cnn = dataSource.getConnection(); PreparedStatement ps = cnn.prepareStatement(sql);) {
			ps.setString(1, "%"+playlistName+"%");
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				return rs.getInt(1); 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	
	@Override
	public ArrayList<CourseVideoManagement> listVideosInCourse(String curseId, Pagination pagination, String videoTitle) {
		String sql = "SELECT PL.playlistid, PL.playlistname, PL.description playlist_description, PL.thumbnailurl, PL.status playlist_status,"
				+ " V.videoid , V.videoname, V.description video_description, V.youtubeurl, V.fileurl, V.postdate, V.userid, V.viewcount,"
				+ " U.USERNAME, CC.CATEGORYNAMES, COUNT(DISTINCT C.VIDEOID) COUNTCOMMENTS, COUNT(DISTINCT VP.*) COUNTVOTEPLUS, COUNT(DISTINCT VM.*) COUNTVOTEMINUS, PD.INDEX ,V.status video_status"
				+ " , mc.maincategoryname FROM TBLVIDEO V LEFT JOIN TBLUSER U ON V.USERID=U.USERID"
				+ " LEFT JOIN (SELECT CV.videoid, string_agg(CT.categoryname, ', ') CATEGORYNAMES FROM TBLCATEGORY CT LEFT JOIN TBLCATEGORYVIDEO CV ON CT.categoryid=CV.categoryid GROUP BY CV.videoid) CC ON V.videoid=CC.videoid"
				+ " LEFT JOIN TBLCOMMENT C ON V.VIDEOID=C.VIDEOID"
				+ " LEFT JOIN (SELECT * FROM TBLVOTE WHERE VOTETYPE=1) VP ON V.VIDEOID=VP.VIDEOID"
				+ " LEFT JOIN (SELECT * FROM TBLVOTE WHERE VOTETYPE=-1) VM ON V.VIDEOID=VM.VIDEOID"
				+ " INNER JOIN TBLPLAYLISTDETAIL PD ON PD.VIDEOID=V.VIDEOID "
				+ " INNER JOIN tblplaylist PL ON PD.PLAYLISTID = PL.playlistid"
				+ " LEFT JOIN tblmaincategory mc ON mc.maincategoryid = PL.maincategory"
				+ " WHERE PD.PLAYLISTID=? and LOWER(V.videoname) LIKE LOWER(?)"
				+ " GROUP BY V.VIDEOID, U.USERNAME, CC.CATEGORYNAMES, PD.INDEX , PL.playlistid , mc.maincategoryname"
				+ " ORDER BY PD.INDEX" 
				+ " offset ? limit ?";
		try (Connection cnn = dataSource.getConnection(); PreparedStatement ps = cnn.prepareStatement(sql);) {
			ArrayList<CourseVideoManagement> cArr =new ArrayList<CourseVideoManagement>();
			ps.setInt(1,Integer.parseInt(Encryption.decode(curseId)));
			ps.setInt(3,pagination.offset());
			ps.setInt(4, pagination.getItem());
			ps.setString(2, "%"+videoTitle+"%");
			CourseVideoManagement c = null;
			ResultSet rs = null;
			rs = ps.executeQuery();
			while(rs.next()){
				c = new CourseVideoManagement();
				c.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				c.setPlaylistName(rs.getString("playlistname"));
				c.setPlaylistDescription(rs.getString("playlist_description"));
				c.setPlaylistThumbnailUrl(rs.getString("thumbnailurl"));
				c.setPlaylistStatus(rs.getBoolean("playlist_status"));
				c.setVideoId(Encryption.encode(rs.getString("videoid")));
				c.setVideoName(rs.getString("videoname"));
				c.setVideoDescription(rs.getString("video_description"));
				c.setYoutubeUrl(rs.getString("youtubeurl"));
				c.setFileUrl(rs.getString("fileurl"));
				c.setPostDate(rs.getDate("postdate"));
				c.setUserId(Encryption.encode(rs.getString("userid")));
				c.setViewCount(rs.getInt("viewcount"));
				c.setUsername(rs.getString("username"));
				c.setCategoryName(rs.getString("categorynames"));
				c.setCountComment(rs.getInt("countcomments"));
				c.setCountVotePlus(rs.getInt("countvoteplus"));
				c.setCountVotePlus(rs.getInt("countvoteminus"));
				c.setIndex(rs.getInt("index"));
				c.setVideoStatus(rs.getBoolean("video_status"));
				c.setMainCategoryName(rs.getString("maincategoryname"));
				cArr.add(c);
			}
			return cArr;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int countVideosInCourse(String courseId , String videoTitle) {
		String sql = "SELECT COUNT(playlistid) FROM tblplaylistdetail WHERE playlistid=?";	
		try (Connection cnn = dataSource.getConnection(); PreparedStatement ps = cnn.prepareStatement(sql);) {
			ps.setInt(1,Integer.parseInt(Encryption.decode(courseId)));
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				return rs.getInt(1); 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	@Override
	public Playlist getCourse(String courseId) {
		String sql = "Select playlistid, playlistname, description ,thumbnailurl, maincategory, bgimage, color, status from tblplaylist where playlistid=?;";
		try (Connection cnn = dataSource.getConnection(); PreparedStatement ps = cnn.prepareStatement(sql);) {
			ps.setInt(1,Integer.parseInt(Encryption.decode(courseId)));
			Playlist playlist = null;
			ResultSet rs = null;
			rs = ps.executeQuery();
			if(rs.next()){
				playlist =new Playlist();
				playlist.setPlaylistId(Encryption.encode(rs.getString("playlistid")));
				playlist.setPlaylistName(rs.getString("playlistname"));
				playlist.setDescription(rs.getString("description"));
				playlist.setBgImage(rs.getString("bgimage"));
				playlist.setColor(rs.getString("color"));
				playlist.setThumbnailUrl(rs.getString("thumbnailurl"));
				playlist.setStatus(rs.getBoolean("status"));
				playlist.setMaincategory(Encryption.encode(rs.getString("maincategory")));
				return playlist;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean updateCourse(FrmUpdatePlaylist playlist) {
		String sql = "UPDATE tblplaylist SET playlistname=?,description=?,thumbnailurl=?,maincategory=?,bgimage=?,color=?,status=? WHERE playlistid=?";
		try(
				Connection cnn = dataSource.getConnection();
				PreparedStatement ps  =  cnn.prepareStatement(sql);
		){
				ps.setString(1, playlist.getPlaylistName());
				ps.setString(2, playlist.getDescription());
				ps.setString(3, playlist.getThumbnailUrl());
				ps.setInt(4, Integer.parseInt(Encryption.decode(playlist.getMaincategory())));
				ps.setString(5, playlist.getBgImage());
				ps.setString(6, playlist.getColor());
				ps.setBoolean(7, playlist.isStatus());
				ps.setInt(8, Integer.parseInt(Encryption.decode(playlist.getPlaylistId())));
				if(ps.executeUpdate() > 0 ) return true;
		}catch(SQLException e){
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean updateStatus(String courseId, boolean value) {
		String sql = "UPDATE tblplaylist SET status=? WHERE playlistid=?";
		try(
				Connection cnn = dataSource.getConnection();
				PreparedStatement ps  =  cnn.prepareStatement(sql);
		){
				ps.setBoolean(1, value);
				ps.setInt(2, Integer.parseInt(Encryption.decode(courseId)));
				if(ps.executeUpdate() > 0 ) return true;
		}catch(SQLException e){
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public boolean addCourse(FrmUpdatePlaylist playlist) {
		String sql = "INSERT INTO TBLPLAYLIST VALUES(nextval('seq_playlist'), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try(
				Connection cnn = dataSource.getConnection();
				PreparedStatement ps  =  cnn.prepareStatement(sql);
		){
				ps.setString(1, playlist.getPlaylistName());
				ps.setString(2, playlist.getDescription());
				ps.setInt(3, Integer.parseInt(Encryption.decode(playlist.getUserId())));
				if(playlist.getThumbnailUrl().equalsIgnoreCase("default-playlist.jpg")){
					ps.setString(4, environment.getProperty("KA.path")+"/resources/upload/file/playlist/thumbnail/"+playlist.getThumbnailUrl());
				}else{
					ps.setString(7, playlist.getThumbnailUrl());
				}
				ps.setBoolean(5,true);
				ps.setInt(6, Integer.parseInt(Encryption.decode(playlist.getMaincategory())));
				if(playlist.getBgImage().equalsIgnoreCase("default-bgimage.jpg")){
					ps.setString(7, environment.getProperty("KA.path")+"/resources/upload/file/maincategory/"+playlist.getBgImage());
				}else{
					ps.setString(7, playlist.getBgImage());
				}
				ps.setString(8, playlist.getColor());
				ps.setBoolean(9, playlist.isStatus());
				if(ps.executeUpdate() > 0 ) return true;
		}catch(SQLException e){
			e.printStackTrace();
		}
		return false;
	}

	
}