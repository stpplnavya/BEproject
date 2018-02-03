package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Security.Authenticator;
import daos.UserDao;
import models.User;
import play.Logger;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import javax.persistence.TypedQuery;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class UserController extends Controller {

    private final static Logger.ALogger LOGGER = Logger.of(UserController.class);

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
      //  final String role = jsonNode.get("role").asText();

        if (null == username) {
            return badRequest("Missing user name");
        }
        if (null == password) {
            return badRequest("Missing password");
        }


      //  if (null == role) {
      //      return badRequest("Missing role");
      //  }
        //user.setRole(role);


        TypedQuery<User> query = jpaApi.em().createQuery("select u from User u where username='" + username + "'", User.class);

        List<User> Result = query.getResultList();

        if(Result.isEmpty()) {

            String salt = Utils.generateSalt();
            User user = new User();
            user.setUsername(username);
            user.setSalt(salt);

            String hashedPassword = Utils.generateHashedPassword(password,salt,10);
            user.setPassword(hashedPassword);



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

        List<User> result = userDao.findByName(username);




        final User user = result.get(0);

        Logger.debug(user.getPassword());
        String salt = user.getSalt();
        String hashPwd = Utils.generateHashedPassword(password,salt,10);
        Logger.debug("Hashed password : "+hashPwd);

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


        List<User> refkey = userDao.findByRefreshToken(reftoken);

        if(reftoken.equals(refkey.get(0)))
        {
            String newtoken = Utils.generateToken();
            Long newthreshold = Utils.generateThreshold();
            User user = new User();
            user.setToken(newtoken);
            user.setThreshold(newthreshold);
            userDao.persist(user);

            ObjectNode result2 = Json.newObject();
            result2.put("access_token", newtoken);
            result2.put("expiry_time", newthreshold);

            return ok(result2);
        }

        else return status(401,"Invalid Refresh token ! ");

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
    public Result deleteUser(){

        final JsonNode jsonNode = request().body().asJson();
        final String username = jsonNode.get("username").asText();

        Logger.debug(username);

        if (null == username) {
            return badRequest("Missing user name");
        }

        final User user = userDao.deleteUser(username);

        if(null==user){
            return notFound("user with the following username nt found"+username);
        }

        return noContent();
    }

    @Transactional
    public Result updateUser(String username){
        return TODO;

    }


    @Transactional
    public Result getAllUsers(){

        final List<User> users = userDao.findAll();

        final JsonNode jsonNode = Json.toJson(users);

        return ok(jsonNode);

    }

}