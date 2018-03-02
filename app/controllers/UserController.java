package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Security.Authenticator;
import controllers.Security.IsAdmin;
import daos.UserDao;
import models.User;
import play.Logger;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserController extends Controller {

    private final static Logger.ALogger LOGGER = Logger.of(UserController.class);
    private Map<Integer, User> users = new HashMap<>();

    private JPAApi jpaApi;
    private UserDao userDao;

    @Inject
    public UserController (UserDao userDao, JPAApi jpaApi) {
        this.userDao = userDao;
        this.jpaApi =  jpaApi;
    }

    @Transactional
    public Result createUser() throws NoSuchAlgorithmException {

        final JsonNode jsonNode = request().body().asJson();
        final String username = jsonNode.get("username").asText();
        final String password = jsonNode.get("password").asText();
        //final String role = jsonNode.get("role").asText();

        if (null == username) {
            return badRequest("Missing user name");
        }
        if (null == password) {
            return badRequest("Missing password");
        }

        //if (null == role) {
          //    return badRequest("Missing role");
        //}
         //user.setRole(role);

        User user = userDao.findByName(username);

        if (null == user) {

            user = new User();
            String salt = Utils.generateSalt();
            user.setUsername(username);
            user.setSalt(salt);
            String hashedPassword = Utils.generateHashedPassword(password,salt,10);
            user.setPassword(hashedPassword);
            user.setRole(User.Role.User);
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
        Logger.debug("User Role"+user.getRole());
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
            return notFound("user with the following username nt found"+username);
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
    public Result getAllUsers(){

        final List<User> users = userDao.findAllUsers();

        final JsonNode jsonNode = Json.toJson(users);

        return ok(jsonNode);
    }
}
