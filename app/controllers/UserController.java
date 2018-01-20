package controllers;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.User;
import play.Logger;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UserController extends Controller {

    private final static Logger.ALogger LOGGER = Logger.of(UserController.class);

    private Map<Integer, User> Users = new HashMap<>();
    private JPAApi jpaApi;
    private Integer index = 0;

    @Inject
    public UserController(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

    @Transactional
    public Result createUser() {

        JsonNode jsonNode = request().body().asJson();

        final String username = jsonNode.get("username").asText();
        final String password = jsonNode.get("password").asText();


        if (null == username) {
            return badRequest("Missing Username");
        }

        if (null == password) {
            return badRequest("Missing password");
        }


        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        LOGGER.debug("user id before: {}", user.getId());

        jpaApi.em().persist(user);

        LOGGER.debug("user id after: {}", user.getId());
        // store in DB
        // return user id
        return created(user.getId().toString());

    }

    // Auhenticate

    /*@Transactional
    public Result authenticate() {
        JsonNode jsonNode = request().body().asJson();
        final String username = jsonNode.get("username").asText();
        final String password = jsonNode.get("password").asText();
        if (null == username) {
            return badRequest("Missing Username");
        }
        if (null == password) {
            return badRequest("Missing password");
        }
        jpaApi.em().createQuery("SELECT username from User u", User.class);
        //LOGGER.info(" return value in authenticate", )
        return null;
    }*/
    @Transactional
    public Result getAllUsers() {

        final Collection<User> users = this.Users.values();
        TypedQuery<User> query = jpaApi.em().createQuery("SELECT u FROM User u", User.class);
        List<User>  users1= query.getResultList();

        final JsonNode jsonNode = Json.toJson(users1);
        return (ok(jsonNode));
    }


}
