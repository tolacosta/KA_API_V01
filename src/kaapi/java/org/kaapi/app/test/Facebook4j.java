package org.kaapi.app.test;

import java.net.MalformedURLException;
import java.net.URL;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.PostUpdate;
import facebook4j.auth.AccessToken;

public class Facebook4j {
	
	
	
	public static void main(String[] args) throws FacebookException, MalformedURLException {
		Facebook facebook = new FacebookFactory().getInstance();
		
		facebook.setOAuthAppId("278987345822711", "fcf40089e0d424bc8a7cbbef9f0e5865");
		facebook.setOAuthPermissions("email, manage_pages, public_profile,publish_actions");
		facebook.setOAuthAccessToken(new AccessToken("EAAD9vM2lAZCcBANFKP1HN6CRJ24Icam1jfVrw4L5ziD0MCsI0AO7B6ZAWm8aWk8KKDzdFXTq7TXWLL7ncXis5OxAmvZBBZCiqV8fis454ZAZAD8inHZARxt7Ht2z8IZAbhTDxOKZAUbK7YDv8ZBNMLFr9wUJovZA2UvHll3MriIamdZAnwZDZD", null));
		
		String name = "ធនាគារ​ជាតិ​តម្រូវឲ្យ​​គ្រឹះស្ថាន​ធនាគារ​ និង​ហិរញ្ញវត្ថុ​ចូល​​សមាជិក​​ប្រព័ន្ធ​​២ ដើម្បី​ជំរុញ​ការទូ​ទាត់​រាយ ដើម្បី​លើក​កម្ពស់​ការ​អភិវឌ្ឍន៍​វិស័យ​ធនាគារ​និង​ហិរញ្ញវត្ថុ​រួម​ទាំង​សេដ្ឋកិច្ច​ជាតិ ​ធនា";
		String caption = "KHMER ACADEMY | ALL KHMER NEWS";
		String description = "ដើម្បី​លើក​កម្ពស់​ការ​អភិវឌ្ឍន៍​វិស័យ​ធនាគារ​និង​ហិរញ្ញវត្ថុ​រួម​ទាំង​សេដ្ឋកិច្ច​ជាតិ ​ធនាគារ​ជាតិ​នៃ​កម្ពុជា​តម្រូវ​ឲ្យ​គ្រឹះស្ថាន​ធនាគារ​និង​ហិរញ្ញវត្ថុ​ចូល​ជា​សមាជិក​ប្រព័ន្ធ FAST";
		if(name.length() > 100){
			name = name.substring(0, 100);
		}
		PostUpdate post = new PostUpdate(new URL("http://news.khmeracademy.org/"))
                .picture(new URL("http://cdn02.sabay.com/cdn/news.sabay.com.kh/wp-content/uploads/2016/05/%E2%80%8BSamsung-The-Triangle-promotion-285x170.jpg"))
                .name(name)
                .caption(caption)
                .description(description);
		facebook.postFeed(post);
//		facebook.postStatusMessage(post);
//		facebook.postStatusMessage("Testing facebook post By JAVA11.");
	}

}
