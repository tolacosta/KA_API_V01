package org.kaapi.app.controllers.elearning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaapi.app.entities.Category;
import org.kaapi.app.entities.Pagination;
import org.kaapi.app.entities.Playlist;
import org.kaapi.app.entities.PlaylistDetail;
import org.kaapi.app.entities.Video;
import org.kaapi.app.entities.Website;
import org.kaapi.app.forms.ForumCommentDTO;
import org.kaapi.app.forms.FrmCreatePlaylist;
import org.kaapi.app.forms.FrmUpdatePlaylist;
import org.kaapi.app.forms.PlaylistDTO;
import org.kaapi.app.services.ForumCommentService;
import org.kaapi.app.services.MainCategoryService;
import org.kaapi.app.services.PlayListServics;
import org.kaapi.app.services.TutorialService;
import org.kaapi.app.services.WebsiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/elearning/playlist/")
public class PlayListControllers {
	
	@Autowired
	PlayListServics playlistservice;
	
	@Autowired
	MainCategoryService maincategoryService;
	
	@Autowired
	TutorialService tutorialService;
	
	@Autowired
	ForumCommentService forumCommentService;
	
	@Autowired
	private WebsiteService websiteService;
	
	
	//Toggle playlist: 
		@RequestMapping(method = RequestMethod.PUT, value = "/togglePlaylist/{pid}", headers = "Accept=application/json")
		public ResponseEntity<Map<String, Object>> togglePlaylist(@PathVariable("pid") String pid) {
			Map<String, Object> map = new HashMap<String, Object>();
			try{
				if(playlistservice.togglePlaylist(pid)) {
					map.put("STATUS", true);
					map.put("MESSAGE", "OPERATION SUCCESS!");
				}else{
					map.put("STATUS", false);
					map.put("MESSAGE", "OPERATION FAIL");
				}
			}catch(Exception e){
				map.put("MESSAGE", "OPERATION FAIL");
				map.put("STATUS", false);
			}
			return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);	
		}
	
	
	/*
	 * action get listallplaylist
	 * we want to listallplaylist
	 */
	@RequestMapping(value="/userplaylistall/{uid}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> UserPlayListAll(
												@PathVariable("uid") String uid
												){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			ArrayList<Playlist>  dto= playlistservice.UserPlayList(uid);
			if(!dto.isEmpty()){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("RES_DATA", dto);
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
			e.printStackTrace();
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	/*
	 * action get listallplaylist
	 * we want to listallplaylist
	 */
	@RequestMapping(value="/userplaylist/{uid}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> UserPlayList(
												@PathVariable("uid") String uid,
												@RequestParam(value ="page", required = false, defaultValue = "1") int page,
												@RequestParam(value ="item" , required = false , defaultValue = "10") int item){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			Pagination pagin = new Pagination();
			pagin.setItem(item);
			pagin.setPage(page);
			pagin.setTotalCount(playlistservice.countUserPlaylist(uid));
			pagin.setTotalPages(pagin.totalPages());
			
			ArrayList<Playlist>  dto= playlistservice.UserPlayList(uid, pagin);
			if(!dto.isEmpty()){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("RES_DATA", dto);
				map.put("PAGINATION", pagin);
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	
	/*getplaylist by id
	 */
	@RequestMapping(value="/getplaylistbyplaylistid/{playlistid}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> GetPlayListByPlayListId(@PathVariable("playlistid") String pid){
		Map<String, Object> map= new HashMap<String, Object>();
		
		try{
			Playlist  dto= playlistservice.get(pid);
			if(dto !=null){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("USERPLAYLIST", dto);
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}

	/*listuserplaylist
	 */
	@RequestMapping(value="/listuserplaylist/{userid}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> listUserPlayList(@PathVariable("userid") String uid){
		Map<String, Object> map= new HashMap<String, Object>();
		
		try{
			ArrayList<Playlist>  dto= playlistservice.listUserPlayList(uid);
			ArrayList<PlaylistDetail> playDetail = playlistservice.listplaylistdetail(uid); 
			if(!dto.isEmpty()){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("USERPLAYLIST", dto);
				map.put("PLAYLISTDETAIL", playDetail);
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	
	/*
	 * list playlist by maincategory
	 * we want to list all the course base on maincategory
	 */
	@RequestMapping(value="/listplaylistbymaincategory/{categoryid}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> listPlaylistByMainCategory(@PathVariable("categoryid") String cid){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			ArrayList<Playlist> dto= playlistservice.listPlayListByMainCategory(cid);
			if(!dto.isEmpty()){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("RES_DATA", dto);
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	/*
	 * search category
	 * we want to search the course in play
	 */
	@RequestMapping(value="/searchplaylist/{searchkey}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> searchPlaylist(@PathVariable("searchkey") String key, 
					@RequestParam(value ="page", required = false, defaultValue = "1") int page,
					@RequestParam(value ="item" , required = false , defaultValue = "10") int item){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			Pagination pagin = new Pagination();
			pagin.setItem(item);
			pagin.setPage(page);
			pagin.setTotalCount(playlistservice.countSearchPlayList(key));
			pagin.setTotalPages(pagin.totalPages());
			
			
			ArrayList<Playlist> dto= playlistservice.searchPlayList(key, pagin);
			if(!dto.isEmpty()){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("RES_DATA", dto);
				map.put("PAGINATION", pagin);
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	/*
	 * searchPlaylistMobile
	 * we want to search only status tru for mobile application
	 */
	@RequestMapping(value="/searchcourse", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> searchCourse(@RequestParam("keyword") String key, 
					@RequestParam(value ="page", required = false, defaultValue = "1") int page,
					@RequestParam(value ="item" , required = false , defaultValue = "10") int item){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			Pagination pagin = new Pagination();
			pagin.setItem(item);
			pagin.setPage(page);
			pagin.setTotalCount(playlistservice.countSearchPlayListMobile(key));
			pagin.setTotalPages(pagin.totalPages());
			
			
			ArrayList<Playlist> dto= playlistservice.searchPlayListMobile(key, pagin);
			if(!dto.isEmpty()){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("RES_DATA", dto);
				map.put("PAGINATION", pagin);
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	/*
	 * action get List all playlist for elearning home page
	 * 
	 */
	@RequestMapping(value="/listmianplaylist", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> listMainPlayList(){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			
			ArrayList<Playlist> maincategory= playlistservice.litsMainElearning();
			ArrayList<Playlist> playlist= playlistservice.listMainPlaylist();
			
		
			if(!maincategory.isEmpty()){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("MAINCATEGORY", maincategory);
				map.put("PLAYLIST", playlist);
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	/*
	 * action get listallplaylist
	 * we want to listallplaylist
	 */
	@RequestMapping(value="/listallplaylist", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> listAllPlayList(@RequestParam(value ="page", required = false, defaultValue = "1") int page,
																@RequestParam(value ="item" , required = false , defaultValue = "10") int item){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			Pagination pagin = new Pagination();
			pagin.setItem(item);
			pagin.setPage(page);
			pagin.setTotalCount(playlistservice.countPlayList());
			pagin.setTotalPages(pagin.totalPages());
			
			ArrayList<Playlist>  dto= playlistservice.listAllPlaylist(pagin);
			if(!dto.isEmpty()){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("RES_DATA", dto);
				map.put("PAGINATION", pagin);
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	

	//
	/*actionaddvideoToplayistdetail ->well
	 * we want to add new playlist detail 
	 * we need playlist ID and video ID
	 */
	@RequestMapping(value="/addvideotoplaylistDetail/{playlistid}/{videoid}", method= RequestMethod.POST, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> addVideoToPlayListDetail(@PathVariable("playlistid") String pid,
																		@PathVariable("videoid") String vid){
		Map<String, Object> map= new HashMap<String, Object>();
		try{		
			if(playlistservice.addVideoToPlst(pid, vid)){
				if(playlistservice.countvideos(pid) == 1){
					playlistservice.updateThumbnail(vid, pid);
				}
				map.put("STATUS", true);
				map.put("MESSAGE", "INSERT SUCCESSFULLY");
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "INSERT UNSUCCESSFULLY");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);	
	}
	
	
	/*action create playlist ->well
	 * we want to add new playlist to table playlist in database
	 * we need (playlistName, description, userId, thumbnailUrl, publicView, maincategory,bgImage,color)
	 *  and status will auto = true
	 */
	@RequestMapping(value="/createplaylist", method= RequestMethod.POST, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> createPlayList(@RequestBody FrmCreatePlaylist playlist){
		Map<String, Object> map= new HashMap<String, Object>();
		try{	
			if(playlistservice.insert(playlist)){
				map.put("STATUS", true);
				map.put("MESSAGE", "INSERT SUCCESSFULLY");
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "INSERT UNSUCCESSFULLY");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);	
		
	}
	
	
	/*action delete playlist ->well
	 * we want to delete playlist
	 * we need only playlist ID
	 */
	@RequestMapping(value="/deleteplaylist/{playlistid}", method= RequestMethod.DELETE, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> deletePlayList(@PathVariable("playlistid") String pid){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			if(playlistservice.delete(pid)){
				map.put("STATUS", true);
				map.put("MESSAGE", "DELETE SUCCESSFULLY");
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "DELETE UNSUCCESSFULLY");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);	
		
	}
	
	
	/*action delete video from playlist ->well
	 * 
	 * we want to delete playlistdetail
	 * we need playlist id and video id
	 */
	@RequestMapping(value="/deletevideofromplaylistdetail/{playlistid}/{videoid}", method= RequestMethod.DELETE, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> deleteViedoFromPlayListDetail(@PathVariable("playlistid") String pid,
																			@PathVariable("videoid") String vid){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			if(playlistservice.deleteVideoFromPlaylist(pid, vid)){
				if(playlistservice.countvideos(pid) == 0){
					playlistservice.updateThumbnailToDefault(pid);
				}
				map.put("STATUS", true);
				map.put("MESSAGE", "DELETE SUCCESSFULLY");
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "DELETE UNSUCCESSFULLY");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);	
		
	}
	
	
	
	
	/*
	 * action get playlist ->well
	 * we want to list playlist
	 * change from getplaylist to listVideoInPlaylist
	 */
	@RequestMapping(value="/listVideoInPlaylist/{playlistid}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> listVideoInPlaylist(	@PathVariable("playlistid") String pid,
															@RequestParam(value ="page", required = false, defaultValue = "1") int page,
															@RequestParam(value ="item" , required = false , defaultValue = "10") int item){
		
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			
			Pagination pagin = new Pagination();
			pagin.setItem(item);
			pagin.setPage(page);
			pagin.setTotalCount(playlistservice.countvideos(pid));
			pagin.setTotalPages(pagin.totalPages());
			
			ArrayList<Video>  dto= playlistservice.listVideoInPlaylist(pid, pagin);
			if(!dto.isEmpty()){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("RES_DATA", dto);
				map.put("PAGINATION", pagin);
				
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	/*
	 * action get playlist ->well
	 * we want to list playlist
	 * change from getplaylist to listVideoInPlaylist
	 */
	@RequestMapping(value="/listAllVideoInPlaylist/{playlistid}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> listAllVideoInPlaylist(	@PathVariable("playlistid") String pid){
		
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			
			
			ArrayList<Video>  dto= playlistservice.listVideoInPlaylist(pid);
			if(!dto.isEmpty()){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("RES_DATA", dto);
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	
	/*
	 * action get play list for update ->well
	 */
	@RequestMapping(value="/getplaylistforupdate/{playlistid}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> getPlayListForUpdate(@PathVariable("playlistid") String pid){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			Playlist playlist = playlistservice.getPlaylistForUpdate(pid);
			if(playlist != null){
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
			map.put("STATUS", true);
			map.put("MESSAGE", "RECORD FOUND");
			map.put("RES_DATA", playlist);
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String,Object>>(map,HttpStatus.OK);
		
	}
	
	
	
	/*action list play list ->well
	 * this method need user id and playlistname from session
	 * not sure about this 
	 */
	@RequestMapping(value="/listplayList/{userid}/{playlistname}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> listPlayList(@PathVariable("userid") String uid,
															@PathVariable("playlistname") String name,
															@RequestParam(value="page", required = false, defaultValue = "1") int page,
															@RequestParam(value="item", required = false, defaultValue = "10") int item){
		
		
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			
			Playlist playlist =new Playlist();
			playlist.setUserId(uid);
			playlist.setPlaylistName(name);
			
			
		
			Pagination pagin = new Pagination();
			pagin.setItem(item);
			pagin.setPage(page);
			pagin.setTotalCount(playlistservice.countUserPlaylist(uid, name));
			pagin.setTotalPages(pagin.totalPages());
			
			ArrayList<Playlist>  dto= playlistservice.list(pagin, playlist);
			if(!dto.isEmpty()){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("RES_DATA", dto);
				map.put("PAGINATION", pagin);
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	/*action list play list Name ->
	 * we want to list playlist name 
	 * we neen only (playlistName,userId)
	 */
	@RequestMapping(value="/listPlayStatusByListName/{playlistname}/{userid}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> listPlayStatusByListName(@PathVariable("playlistname") String listName,
																@PathVariable("userid") String uid){
		Map<String, Object> map= new HashMap<String, Object>();
		Playlist playlist =new Playlist();
		playlist.setPlaylistName(listName);
		playlist.setUserId(uid);
		try{
			Playlist  dto= playlistservice.listplaylistname(playlist);
			if(dto != null){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("RES_DATA", dto);
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	/*action update play list ->well
	 * we need (playlistid,playlistName, description, userId, thumbnailUrl, publicView, maincategory,bgImage,color,status)
	 *  
	 */
	@RequestMapping(value="/updatePlayList", method= RequestMethod.PUT, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> updatePlayList(@RequestBody FrmUpdatePlaylist playlist){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			if(playlistservice.update(playlist)){
				map.put("STATUS", true);
				map.put("MESSAGE", "UPDATE SUCCESSFULLY");
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "UPDATE UNSUCCESSFULLY");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);	
		
	}
	
	@RequestMapping(value="/playlists/recents/{userid}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> listPlayStatusByListName(@PathVariable String userid){
		Map<String, Object> map= new HashMap<String, Object>();
		List<Playlist> playlists = new ArrayList<Playlist>();
		//List<Playlist> playlistsHighSchool = new ArrayList<Playlist>();
		try{
			playlists = playlistservice.listRecentPlaylists("");
			//playlistsHighSchool = playlistservice.listRecentPlaylists("25");
			if(!playlists.isEmpty()){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("RES_DATA", playlists);
				map.put("HIGH_SCHOOL", playlistservice.listRecentPlaylists("25"));
				map.put("COMPUTER_SCIENCE" , playlistservice.listRecentPlaylists("1"));
				map.put("LANGUAGES" , playlistservice.listRecentPlaylists("23"));
				if(!userid.equals("null")){
//					map.put("RECOMMENDED_VIDEOS", playlistservice.recommendedVideos(userid));
					map.put("RECOMMENDED_COURSE" , playlistservice.recommendedCourses(userid));
				}
//				System.out.println("userid " + userid);
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	@RequestMapping(value="/filterplaylist/u/{userid}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> listPlaylistByUseridPlaylistNameMainCategoryName(
												@RequestParam(value = "playlistname" ,required = false, defaultValue = "") String playlistname,
												@RequestParam(value = "maincategoryname",required = false, defaultValue = "") String maincategoryname,
												@PathVariable("userid") String userid,
												@RequestParam(value ="page", required = false, defaultValue = "1") int page,
												@RequestParam(value ="item" , required = false , defaultValue = "10") int item){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
//			System.out.println(userid);
			
			Pagination pagin = new Pagination();
			Playlist dto = new Playlist();
			dto.setUserId(userid);
			dto.setMaincategoryname(maincategoryname);
			dto.setPlaylistName(playlistname);
			
			pagin.setItem(item);
			pagin.setPage(page);
			pagin.setTotalCount(playlistservice.countPlaylistByUseridPlaylistNameMainCategoryName(dto));
			pagin.setTotalPages(pagin.totalPages());
			
			ArrayList<Playlist>  arr= playlistservice.listPlaylistByUseridPlaylistNameMainCategoryName(dto, pagin);
			if(!arr.isEmpty()){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("RES_DATA", arr);
				map.put("PAGINATION", pagin);
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			e.printStackTrace();
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	@RequestMapping(value="/recommend/{userid}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> recommendedVideosAndCourses(@PathVariable("userid") String userid){
		Map<String, Object> map= new HashMap<String, Object>();
		List<Playlist> playlists = new ArrayList<Playlist>();
		try{
			if(!playlists.isEmpty()){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("RES_DATA", playlists);
				map.put("RECOMMENDED_VIDEOS", playlistservice.recommendedCourses(userid));
				map.put("RECOMMENDED_COURSE" , playlistservice.recommendedVideos(userid));
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	@RequestMapping(value="/playlists/index_new_1/{userid}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> listPlayMainCategorye(@PathVariable String userid){
		Map<String, Object> map= new HashMap<String, Object>();
//		Map<String, Object> courseMap= new HashMap<String, Object>();
//		List<String> mID = new ArrayList<String>();

		List<Playlist> playlists = new ArrayList<Playlist>();
		//List<Playlist> playlistsHighSchool = new ArrayList<Playlist>();
		try{
			playlists = playlistservice.listRecentPlaylists("");
//			List<MainCategory> mainCategory = maincategoryService.listMainCategory("");
			map.put("STATUS", true);
			map.put("MESSAGE", "RECORD FOUND");
			map.put("RESP_DATA", playlists);
			map.put("POPULAR_VIDEOS", playlistservice.mostViewedVideos());
			/*for(int i=0;i<mainCategory.size();i++){
				courseMap.put(mainCategory.get(i).getMainCategoryName(), playlistservice.listPlaylistsByMainCategory(mainCategory.get(i).getMainCategoryId()));
				mID.add(mainCategory.get(i).getMainCategoryId());
			}
			map.put("MAIN_CATEGORY_ID", mID);
			map.put("COURSES", courseMap);*/
			if(!userid.equals("null")){
				map.put("RECOMMENDED_VIDEOS", playlistservice.recommendedVideos(userid));
				map.put("RECOMMENDED_COURSE" , playlistservice.recommendedCourses(userid));
			}
//			System.out.println("userid " + userid);
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	
	@RequestMapping(value="/list_playlist_by_maincategoryid/{mainCategoryID}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> listPlayListByMaincategory(@PathVariable("mainCategoryID") String mainCategoryID){
		Map<String, Object> map= new HashMap<String, Object>();
		List<Playlist> playlists = new ArrayList<Playlist>();
		try{
			playlists = playlistservice.listPlaylistsByMainCategory(mainCategoryID);
			if(playlists != null){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("RESP_DATA", playlists);
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
	}
		
	
	@RequestMapping(value="/byMainCategoryWithPagin/{mainCategoryId}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> listPlaylistsByMainCategoryWithPagin(
												@PathVariable String mainCategoryId,
												@RequestParam(value ="page", required = false, defaultValue = "1") int page,
												@RequestParam(value ="item" , required = false , defaultValue = "10") int item){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			Pagination pagin = new Pagination();
			pagin.setItem(item);
			pagin.setPage(page);
			
			pagin.setTotalCount(playlistservice.countPlaylists(mainCategoryId));
			pagin.setTotalPages(pagin.totalPages());
			
			ArrayList<PlaylistDTO>  arr= (ArrayList<PlaylistDTO>) playlistservice.listPlaylistDTOByMainCategoryWithPagin(mainCategoryId, pagin);
			if(arr != null){
				map.put("STATUS", true);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("RES_DATA", arr);
				map.put("PAGINATION", pagin);
			}else{
				map.put("STATUS", false);
				map.put("MESSAGE", "RECORD NOT FOUND!");
			}
		}catch(Exception e){
			e.printStackTrace();
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	@RequestMapping(value="/main_page", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> listPlaylistsByMainCategoryWithPagin(){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			
			Pagination pagin = new Pagination();
			pagin.setItem(10);
			pagin.setPage(1);
			
			/*pagin.setTotalCount(playlistservice.countPlaylists("empty"));
			pagin.setTotalPages(pagin.totalPages());*/
			
			ArrayList<PlaylistDTO>  courses= (ArrayList<PlaylistDTO>) playlistservice.listPlaylistDTOByMainCategoryWithPagin("empty", pagin);
			if(!courses.isEmpty()){
				map.put("LIST_COURSE", courses);
			}else{
				map.put("STATUS_COURSE", false);
			}
			
			pagin.setItem(12);
			ArrayList<Category> categories = tutorialService.listTutorial(pagin);
			if(!categories.isEmpty()){
				map.put("LIST_CATEGORIES", categories);
			}else{
				map.put("STATUS_CATEGORIES", false);
			}
			
			pagin.setItem(10);
			List<ForumCommentDTO> question = forumCommentService.listCommentDTO(pagin);
			if(!categories.isEmpty()){
				map.put("LIST_QUESTION", question);
			}else{
				map.put("STATUS_QUESTION", false);
			}
			
			pagin.setItem(12);
			ArrayList<Website> website = websiteService.findWebsitebyCategoryId(pagin, "empty");
			if(!website.isEmpty()){
				map.put("LIST_WEBSTIE", website);
			}else{
				map.put("STATUS_WEBSITE", false);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
}
