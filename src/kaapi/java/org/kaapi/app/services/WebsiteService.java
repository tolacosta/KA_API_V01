package org.kaapi.app.services;

import java.util.ArrayList;

import org.kaapi.app.entities.Pagination;
import org.kaapi.app.entities.WebSiteCategory;
import org.kaapi.app.entities.Website;

public interface WebsiteService {

	public ArrayList<Website> findWebsitebyCategoryId(Pagination pagin, String cateId);
	public ArrayList<WebSiteCategory> findAllCategory();
	public int countWebsitebyCategoryId(String cateid);
	public boolean view(String id);
	
}
