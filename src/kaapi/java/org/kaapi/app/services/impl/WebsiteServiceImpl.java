package org.kaapi.app.services.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.kaapi.app.entities.Pagination;
import org.kaapi.app.entities.WebSiteCategory;
import org.kaapi.app.entities.Website;
import org.kaapi.app.services.WebsiteService;
import org.kaapi.app.utilities.Encryption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WebsiteServiceImpl implements WebsiteService {

	@Autowired
	DataSource dataSource;
	
	/*@Autowired
	private Environment environment;*/
	
	@Override
	public ArrayList<Website> findWebsitebyCategoryId(Pagination pagin,String cateId) {
		if(cateId.equals("empty")){
			cateId = "";
		}else{
			cateId = Encryption.decode(cateId);
		}
		String sql = " SELECT * FROM website.tbl_website WHERE status='1' AND CAST(category_id as VARCHAR) LIKE ? offset ? limit ?";
		try (Connection cnn = dataSource.getConnection(); PreparedStatement ps = cnn.prepareStatement(sql);) {
			ArrayList<Website> websites =new ArrayList<Website>();
			ps.setString(1,"%"+cateId+"%");
			System.out.println("Category id "  + cateId);
			ps.setInt(2,pagin.offset());
			ps.setInt(3, pagin.getItem());
			Website website = null;
			ResultSet rs = null;
			rs = ps.executeQuery();
			while(rs.next()){
				website = new Website();
				website.setId(Encryption.encode(rs.getString("id")));
				website.setTitle(rs.getString("title"));
				website.setLink(rs.getString("link"));
				website.setLogoUrl(rs.getString("logo_url"));
				website.setViewed(rs.getInt("viewed"));
				website.setIndex(rs.getInt("index"));
				website.setCreatedDate(rs.getString("created_date"));
				website.setCategoryId(Encryption.encode(rs.getString("category_id")));
				websites.add(website);
			}
			return websites;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public int countWebsitebyCategoryId(String cateid) {
		if(cateid.equals("empty")){
			cateid = "";
		}else{
			cateid = Encryption.decode(cateid);
		}
		String sql = "SELECT COUNT(*) FROM website.tbl_website WHERE status='1' AND CAST(category_id as VARCHAR) LIKE ?";
		try (Connection cnn = dataSource.getConnection(); PreparedStatement ps = cnn.prepareStatement(sql);) {
			ps.setString(1, "%"+cateid+"%");
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
	public ArrayList<WebSiteCategory> findAllCategory() {
		String sql = " SELECT * FROM website.tbl_category WHERE status='1'";
		try (Connection cnn = dataSource.getConnection(); PreparedStatement ps = cnn.prepareStatement(sql);) {
			ArrayList<WebSiteCategory> cates =new ArrayList<WebSiteCategory>();
			WebSiteCategory cate = null;
			ResultSet rs = null;
			rs = ps.executeQuery();
			while(rs.next()){
				cate = new WebSiteCategory();
				cate.setId(Encryption.encode(rs.getString("id")));
				cate.setName(rs.getString("name"));
				cate.setIndex(rs.getInt("index"));
				cate.setCreatedDate(rs.getString("created_date"));
				cates.add(cate);
			}
			return cates;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean view(String id) {
		String sql = "UPDATE website.tbl_website SET viewed=viewed+1  WHERE id=?";
		try(
				Connection cnn = dataSource.getConnection();
				PreparedStatement ps  =  cnn.prepareStatement(sql);
		){
				ps.setInt(1, Integer.parseInt(Encryption.decode(id)));
				if(ps.executeUpdate() > 0 ) return true;
		}catch(SQLException e){
			e.printStackTrace();
		}
		return false;
	}
}
