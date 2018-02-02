package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Security.Authenticator;
import controllers.Security.IsAdmin;
import models.User;
import play.Logger;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import javax.persistence.TypedQuery;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class LoginController extends Controller{

    private final static Logger.ALogger LOGGER = Logger.of(LoginController.class);
     private JPAApi jpaApi;

    @Inject
    public LoginController(JPAApi jpaApi) {
        this.jpaApi=jpaApi;
    }


    @Transactional
    public Result login() {


        final JsonNode jsonNode = request().body().asJson();
        final String username = jsonNode.get("username").asText();
        final String password = jsonNode.get("password").asText();


        if (null == username) {
            return badRequest("Missing user name");
        }
        if (null == password) {
            return badRequest("Missing password");
        }

        TypedQuery<User> query = jpaApi.em().createQuery("select u from User u where username='" + username + "'", User.class);
        Logger.debug(String.valueOf(query));
        final List<User> Result = query.getResultList();

        if (Result.isEmpty()) {
            return unauthorized();
        }

        if (Result.size() > 1) {
            return internalServerError();
        }

        final User user = Result.get(0);


        Logger.debug(user.getPassword());

        if (password.equals(user.getPassword())) {

            String token = generateToken();
            long generatedTime = Calendar.getInstance().getTimeInMillis();
            long threshold=generatedTime+3600;

            Logger.debug(token);

            user.setToken(token);
            user.setGeneratedTime(generatedTime);
            user.setThreshold(threshold);
            jpaApi.em().persist(user);


            ObjectNode result = Json.newObject();
            result.put("access_token", token);
            result.put("expiry_time", threshold);

            return ok(result);

        }
        else
            return ok("Invalid password");


    }

    static private String generateToken() {

        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();

        while (salt.length() < 18) { // length of the random string.

            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));

        }

        String saltStr = salt.toString();
        Logger.debug(saltStr);
        return saltStr;

    }



    @Authenticator
    public Result getCurrentUser() {

        LOGGER.debug("Get current user");

        final User user = (User) ctx().args.get("user");

        LOGGER.debug("User: {}", user);

        final JsonNode json = Json.toJson(user);
        return ok(json);
    }

    @Authenticator
    @IsAdmin
    public Result deleteUser(String id) {
        return TODO;
    }

    @Authenticator
    public Result updateUser(String id) {
        return TODO;
    }


}
