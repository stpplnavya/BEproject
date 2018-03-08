package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.mail.smtp.SMTPMessage;
import daos.SurveyDao;
import daos.UserDao;
import models.User;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.util.Properties;
import controllers.Security.Authenticator;

public class MailerServices extends Controller {



    private UserDao userDao;


    @javax.inject.Inject
    public MailerServices (UserDao userDao) {
        this.userDao= userDao;

    }

    @Transactional
    @Authenticator
    public Result roleUpdation(){

        //final JsonNode jsonNode = request().body().asJson();
        //final String username = jsonNode.get("username").asText();

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");

        final User user = (User) ctx().args.get("user");
        String sender=user.getEmail();
        String receiever="anketrac2018@gmail.com";

        Session session = Session.getDefaultInstance(props,new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(sender,"sahithisony03");
            }
        });

        try {

            SMTPMessage message = new SMTPMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(receiever));

            message.setSubject("Changing to Admin");
            message.setText("I want to change from user to admin so check my details and make me admin ");
            message.setNotifyOptions(SMTPMessage.NOTIFY_SUCCESS);

            Transport.send(message);

        }
        catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return ok();
    }

    //public Result forgetPassword(String recipient){


    }

