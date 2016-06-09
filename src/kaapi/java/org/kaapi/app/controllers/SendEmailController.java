package org.kaapi.app.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.kaapi.app.entities.SendMail;
import org.kaapi.app.services.UserService;
import org.kaapi.app.utilities.SendMailTLS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class SendEmailController {

	@Autowired
	private UserService userService;
	
	@RequestMapping(value="/send", method= RequestMethod.POST, headers= "Accept=application/json")
	public ResponseEntity<Map<String, Object>> sendEmail(@RequestBody SendMail sendMail){
		Map<String, Object> map= new HashMap<String, Object>();
		try{
			String token = UUID.randomUUID().toString();
			sendMail.setBody( "   <h1>Activate Your Khmer Academy Account</h1> <h4>Thanks for joining the KhmerAcademy! Please Activate Your Account, so you can login and start learning with Khmer Academy.</h4>" 
					+ " <h4><a href='http://www.khmeracademy.org/confirmemail?code="+token+"'>"+"Please click here to Activate Your Account</a></h4>"
					+ " <h4>If the link does not work for you, please copy and paste this link into your browser:</h4><a href='http://www.khmeracademy.org/confirmemail?code="+token+"'>http://www.khmeracademy.org/confirmemail?code="+token+"</a>");
			sendMail.setSubject("Please Activate Your Khmer Academy Account");
			new SendMailTLS().sendEmailToUserByEmail(sendMail);	
			userService.insertHistoryResetPassWord(token,sendMail.getSendTo(),"signup-resend");
			userService.updateResendCountEmail(sendMail.getSendTo());
		    map.put("STATUS", true);
		}catch(Exception e){
			e.printStackTrace();
			map.put("STATUS", false);
			map.put("MESSAGE", "ERROR OCCURRING!");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
		
	}
}
