package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.mail.smtp.SMTPMessage;
import controllers.Security.Authenticator;
import controllers.Security.IsAdmin;
import daos.UserDao;
import models.TemporaryStorage;
import models.User;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


public class UserController extends Controller {

    private final static Logger.ALogger LOGGER = Logger.of(UserController.class);
    private Map<Integer, User> users = new HashMap<>();

    TemporaryStorage map = new TemporaryStorage();

    private UserDao userDao;

    @Inject
    public UserController (UserDao userDao) {
        this.userDao = userDao;
    }

    @Transactional
    public Result createUser() throws NoSuchAlgorithmException {

        final JsonNode jsonNode = request().body().asJson();
        final String username = jsonNode.get("username").asText();
        final String password = jsonNode.get("password").asText();
        final String email = jsonNode.get("email").asText();

        if (null == username) {
            return badRequest("Missing user name");
        }
        if (null == password) {
            return badRequest("Missing password");
        }
        if (null == email) {
            return badRequest("Missing email");
        }

        User user = userDao.findByName(username);

        if (null == user) {

            user = new User();
            String salt = Utils.generateSalt();
            user.setUsername(username);
            user.setSalt(salt);
            String hashedPassword = Utils.generateHashedPassword(password,salt,10);
            user.setPassword(hashedPassword);
            user.setRole(User.Role.User);
            user.setEmail(email);
            user = userDao.persist(user);

            return created(String.valueOf(user.getId()));
        }
        else
            return status(409,"username already exists");
    }

    @Transactional
    public Result login() throws NoSuchAlgorithmException {

        final JsonNode jsonNode = request().body().asJson();
        final String username = jsonNode.get("username").asText();
        final String password = jsonNode.get("password").asText();

        if (null == username) {
            return badRequest("Missing user name");
        }
        if (null == password) {
            return badRequest("Missing password");
        }

        User user = userDao.findByName(username);
        if (null == user) {
            return status(401, "No user with given username");
        }

        String salt = user.getSalt();
        String hashPwd = Utils.generateHashedPassword(password,salt,10);
        Logger.debug("Hashed password : "+hashPwd);
        Logger.debug("DB password : "+user.getPassword());
        Logger.debug("User Role : "+user.getRole());
        if (hashPwd.equals(user.getPassword())) {

            String token = Utils.generateToken();
            String reftoken = Utils.generateToken();
            Long threshold = Utils.generateThreshold();

            user.setToken(token);
            user.setRefToken(reftoken);
            user.setThreshold(threshold);
            userDao.persist(user);

            ObjectNode result1 = Json.newObject();
            result1.put("access_token", token);
            result1.put("expiry_time", threshold);
            result1.put("refresh_token", reftoken);

            return ok(result1);
       }

        else
            return ok("Invalid password");
    }

    @Transactional
    public Result verifyRefreshToken(String reftoken) {

       User user = userDao.findByRefreshToken(reftoken);
        if (null == user) {

            return status(401, "No refresh token specified");
        }

        else {
            String newtoken = Utils.generateToken();
            Long newthreshold = Utils.generateThreshold();

            user.setToken(newtoken);
            user.setThreshold(newthreshold);
            userDao.persist(user);

            ObjectNode result2 = Json.newObject();
            result2.put("access_token", newtoken);
            result2.put("expiry_time", newthreshold);

            return ok(result2);
        }
    }

    @Transactional
    @Authenticator
    public Result getCurrentUser() {

        LOGGER.debug("Get current user");
        final User user = (User) ctx().args.get("user");
        LOGGER.debug("User: {}", user);

        final JsonNode json = Json.toJson(user);
        return ok(json);
    }

    @Transactional
    @Authenticator
    public Result deleteUser(){

        final JsonNode jsonNode = request().body().asJson();
        final String username = jsonNode.get("username").asText();
        if (null == username) {
            return badRequest("Missing user name");
        }

        final User user = userDao.deleteByName(username);
        if(null==user){
            return notFound("user with the following username not found"+username);
        }
        if (user.getUsername() == null) {
            return noContent();
        }
        return status(409,"cannot delete user as he has surveys associated");
    }

    @Transactional
    @Authenticator
    public Result changePassword() throws NoSuchAlgorithmException {

        final JsonNode jsonNode = request().body().asJson();
        final String username = jsonNode.get("username").asText();
        final String old_password = jsonNode.get("old_password").asText();
        final String new_password=jsonNode.get("new_password").asText();
        if (null == username) {
            return badRequest("Missing user name");
        }

        if(null == old_password){
            return badRequest("Missing password");
        }

        if(null == new_password){
            return badRequest("Missing password");
        }

        final User user = userDao.findByName(username);
        String password=user.getPassword();
        String old_salt=user.getSalt();
        String hashedPassword = Utils.generateHashedPassword(old_password,old_salt,10);
        if(password.equals(hashedPassword)) {

            String hashedPassword1 = Utils.generateHashedPassword(new_password, old_salt, 10);
            user.setPassword(hashedPassword1);
            userDao.persist(user);
        }
        else{
            return badRequest("wrong password");
        }

        return ok("changed password");
    }

    @Transactional
    @Authenticator
    @IsAdmin
    public Result updateRole(){

        final JsonNode jsonNode = request().body().asJson();

        final String username = jsonNode.get("username").asText();
        if (null == username) {
            return badRequest("Missing user name");
        }

        final User user= userDao.findByName(username);

        if(user.getRole().equals(User.Role.Admin)){

            return status(409,"cannot update role");
        }
        user.setRole(User.Role.Admin);
        userDao.persist(user);

        return ok("role updated");
    }

    @Transactional
    @Authenticator
    public Result roleUpdation(){

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");

        final User user = (User) ctx().args.get("user");

        String email = user.getEmail();
        String username=user.getUsername();
        Integer id=user.getId();
        String recipient="anketrac2018@gmail.com";

        Logger.debug("sender mail: " +email);

        Session session = Session.getDefaultInstance(props,new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication("anketracnew123@gmail.com","anketrac123");
            }
        });

        try {

            SMTPMessage message = new SMTPMessage(session);
            message.setFrom(new InternetAddress("anketracnew123@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse( recipient ));

            message.setSubject("Make "+email+" as admin");
            message.setText("I, <"+email+"> want to change from user to admin.So, check my details and make me admin.My details are\n username: "+username+"\n id : "+id
            +" \n www.rolechange.eu:8080/changeRole/!Token="+ user.getToken());

            message.setNotifyOptions(SMTPMessage.NOTIFY_SUCCESS);

            Transport.send(message);

        }
        catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return ok();
    }


    @Transactional
    public Result forgotPassword(String email) {

        User user=userDao.findByEmail(email);
        Logger.debug("Username: "+user.getUsername());
        if(user == null){

            return badRequest();
        }

        String randomToken = Utils.generateToken();
        Long timeStamp = Utils.generateThreshold();

        Logger.debug("Random Token: "+randomToken);
        Logger.debug("timeStamp : "+ timeStamp);

        F.Tuple<User, Long> tuple = new F.Tuple(user, timeStamp);

        map.addMap(randomToken, tuple);


        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");


        String sender = "anketrac2018@gmail.com";

        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(sender, "anketrac123");
            }
        });

        try {

            String url = "http://localhost:3000/forgotpassword/";

            SMTPMessage message = new SMTPMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(Message.RecipientType.TO,

                    InternetAddress.parse(email));

            message.setSubject("Forgot Password");
            message.setText("To change password, click here:\n" + url + randomToken);
            message.setNotifyOptions(SMTPMessage.NOTIFY_SUCCESS);

            Transport.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return ok();
    }

    @Transactional
    public Result resetPassword() throws NoSuchAlgorithmException{

        final JsonNode jsonNode = request().body().asJson();
        final String newPassword = jsonNode.get("newPassword").asText();
        final String userToken = jsonNode.get("id").asText();

        ConcurrentHashMap<String, F.Tuple<User, Long>> result = map.getMap();

        F.Tuple tuple = result.get(userToken);
        LOGGER.debug("tuple value"+tuple);

        if (null == tuple) {
            return forbidden();
        }

        User user = (User) tuple._1;
        Long timestamp = (Long) tuple._2;

        Long currentTime = new Timestamp(System.currentTimeMillis()).getTime();

        Logger.debug("Current time: "+currentTime);
        Logger.debug("timestamp: "+timestamp );

        if(currentTime > timestamp) {
            return badRequest("the link is no longer valid");
        }

        String username=user.getUsername();
        Logger.debug("Username in tuple: "+username);
        String salt = Utils.generateSalt();

        String hashedPassword = Utils.generateHashedPassword(newPassword, salt, 10);
        User user1=userDao.findByName(username);
        user1.setPassword(hashedPassword);
        user1.setSalt(salt);
        userDao.persist(user1);
        Logger.debug("new password: " +hashedPassword );

        return  ok("Successfully reset the password");
    }

    @Transactional
    @Authenticator
    public Result getAllUsers(){

        final List<User> users = userDao.findAllUsers();
        final JsonNode jsonNode = Json.toJson(users);

        return ok(jsonNode);
    }

    @Transactional
    @Authenticator
    public Result logout() {

        session().clear();
        User user = (User) ctx().args.get("user");
        user.setToken(null);
        user.setRefToken(null);
        user.setThreshold(null);

        return ok("Logged out successfully");
    }
}
