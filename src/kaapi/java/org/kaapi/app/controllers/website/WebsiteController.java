package org.kaapi.app.controllers.website;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaapi.app.entities.Category;
import org.kaapi.app.entities.Pagination;
import org.kaapi.app.entities.WebSiteCategory;
import org.kaapi.app.entities.Website;
import org.kaapi.app.forms.ForumCommentDTO;
import org.kaapi.app.forms.PlaylistDTO;
import org.kaapi.app.services.WebsiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/website")
public class WebsiteController {
	
	@Autowired
	private WebsiteService websiteService;
	
	@RequestMapping(value="/findAllWebsiteByCategoryId/{cateid}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> listPlaylistsByMainCategoryWithPagin(
			@PathVariable String cateid,
			@RequestParam(value ="page", required = false, defaultValue = "1") int page,
			@RequestParam(value ="item" , required = false , defaultValue = "10") int item){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			Pagination pagin = new Pagination();
			pagin.setItem(item);
			pagin.setPage(page);
			pagin.setTotalCount(websiteService.countWebsitebyCategoryId(cateid));
			pagin.setTotalPages(pagin.totalPages());
			
			ArrayList<Website> website = websiteService.findWebsitebyCategoryId(pagin, cateid);
			if(!website.isEmpty()){
				map.put("LIST_WEBSTIE", website);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("PAGINATION", pagin);
				map.put("STATUS_WEBSITE", true);
			}else{
				map.put("STATUS_WEBSITE", false);
				map.put("MESSAGE", "RECORD NOT FOUND");
			}
			
		}catch(Exception e){
			e.printStackTrace();
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	
	@RequestMapping(value="/findAllCategory", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> findAllCategory(){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			ArrayList<WebSiteCategory> category = websiteService.findAllCategory();
			if(!category.isEmpty()){
				map.put("LIST_CATEGORY", category);
				map.put("MESSAGE", "RECORD FOUND");
				map.put("STATUS_CATEGORY", true);
			}else{
				map.put("STATUS_CATEGORY", false);
				map.put("MESSAGE", "RECORD NOT FOUND");
			}
			
		}catch(Exception e){
			e.printStackTrace();
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	
	@RequestMapping(value="/view/{id}", method= RequestMethod.GET, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> view(@PathVariable String id){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			if(websiteService.view(id)){
				map.put("STATUS", true);
			}else{
				map.put("STATUS", false);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
	
	

}
