/*package controllers;

import com.sun.mail.smtp.SMTPMessage;
import play.mvc.Controller;
import play.mvc.Result;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.util.Properties;

public class MailerService extends Controller {

    public Result sendEmail(String recipient){

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
                return new javax.mail.PasswordAuthentication("sahithi1903@gmail.com","sahithisony03");
            }
        });

        try {

            SMTPMessage message = new SMTPMessage(session);
            message.setFrom(new InternetAddress("sahithi1903e@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse( recipient ));

            message.setSubject("Testing Subject");
            message.setText("Hi this is from mailer program");
            message.setNotifyOptions(SMTPMessage.NOTIFY_SUCCESS);

            Transport.send(message);

        }
        catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return ok();
    }

}*/
