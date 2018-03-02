/*package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.mail.smtp.SMTPMessage;
import play.mvc.Controller;
import play.mvc.Result;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.util.Properties;

public class MailerServices extends Controller {

    public Result sendEmail(String recipient){

        final JsonNode jsonNode = request().body().asJson();
        final String username = jsonNode.get("username").asText();

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");

        Session session = Session.getDefaultInstance(props,new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication("anketrac2018@gmail.com","anketrac123");
            }
        });

        try {

            SMTPMessage message = new SMTPMessage(session);
            message.setFrom(new InternetAddress("anketrac2018@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse( recipient ));

            message.setSubject("Changing to Admin");
            message.setText("I want to change from user to admin so check my details and make me admin and my username is "+username);
            message.setNotifyOptions(SMTPMessage.NOTIFY_SUCCESS);

            Transport.send(message);

        }
        catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return ok();
    }

}*/
