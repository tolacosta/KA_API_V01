package org.kaapi.app.utilities;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.kaapi.app.entities.SendMail;

public class SendMailTLS {

	public void sendMaile(String address,String type,String msg){

		/*final String username = "khmeracademy.tola@gmail.com";
		final String password = "Apple0123456789";

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");*/
		
		
		final String username = "sendonly@khmeracademy.org";
		final String password = "qwe123";

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.starttls.enable", "false");
//		props.put("mail.debug", "false");
		props.put("mail.smtp.host", "mail.khmeracademy.org");
//		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("noreply@khmeracademy.org"));
//			message.setFrom(new InternetAddress("sendonly@khmeracademy.org"));
			message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(address));
//			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("misoh049@gmail.com,tolapheng99@gmail.com"));
			if(type.equals("reset")){
					message.setSubject("Khmer Academy - Reset your password ");
			}else if(type.equals("fbSignUp")){
				message.setSubject("Khmer Academy - Account information");
		    }
			else{
				message.setSubject("Khmer Academy - Please confirm your email address");
			}
			message.setContent(msg, "text/html; charset=utf-8");
//			message.setText(msg);
			Transport.send(message);
//			System.out.println("Done");
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean sendEmailToUserByEmail(SendMail sendMail){
		final String username = "sendonly@khmeracademy.org";
		final String password = "qwe123";

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.host", "mail.khmeracademy.org");
		
		/*final String username = "khmeracademy.tola@gmail.com";
		final String password = "Apple0123456789";
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");*/
		
		Session session = Session.getInstance(props,new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		try {
			Message message = new MimeMessage(session);
			//FROM
			message.setFrom(new InternetAddress("noreply@khmeracademy.org"));
			// TO
			message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(sendMail.getSendTo()));
			// SUBJECT
			message.setSubject(sendMail.getSubject());
			// MESSAGE
			message.setContent(sendMail.getBody(), "text/html; charset=utf-8");
			
			Transport.send(message);
			
		} catch (MessagingException e) {
			return false;
		}
		return true;
	}
	
	
}