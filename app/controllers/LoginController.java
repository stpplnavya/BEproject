package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.User;
import play.Logger;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Random;

public class LoginController extends Controller {

    private final static Logger.ALogger LOGGER = Logger.of(LoginController.class);
     private JPAApi jpaApi;

    @Inject
    public LoginController(JPAApi jpaApi) {
        this.jpaApi=jpaApi;
    }


    @Transactional
    public Result verify() {


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

        List<User> Result = query.getResultList();

        for (User p : Result) {

            System.out.print(p.getPassword());// Object[] array=users.toArray();


            if (password.equals(p.getPassword())) {

                final JsonNode jsonNode1 = Json.toJson(Result);

                return ok("Successful");
            }
            else
                return ok("Invalid parameters");
        }
        LOGGER.debug("");
        String token = generateToken();
        jpaApi.em().persist(token);
        final JsonNode jsonNode1 = Json.toJson(token);
        return ok("Your Access token : "+jsonNode1);
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
        return String.valueOf(ok(saltStr));

    }

}
