package controllers;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.List;

public class UserController extends Controller {

    private final static Logger.ALogger LOGGER = Logger.of(LoginController.class);

    private JPAApi jpaApi;
    private UserDao userDao;

    @Inject
    public UserController (UserDao userDao, JPAApi jpaApi) {
        this.userDao = userDao;
        this.jpaApi =  jpaApi;
    }

    @Transactional
    public Result createUser() {

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
        TypedQuery<User> query = jpaApi.em().createQuery("select u from User u where username='" + username + "'", User.class);

        List<User> Result = query.getResultList();

        if(Result.isEmpty()) {

            User user = new User(username, password);
            user.setPassword(password);
            user.setUsername(username);
            //user.setRole(role);

            user = userDao.persist(user);

            return created(user.getId().toString());
        }
        else
            return status(409,"username already exists");

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