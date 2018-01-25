package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import daos.UserDao;
import models.User;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;

public class UserController extends Controller {

    private UserDao userDao;

    @Inject
    public UserController (UserDao userDao) {
        this.userDao = userDao;
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

        User user = new User(username,password);
        user.setPassword(password);
        user.setUsername(username);
        //user.setRole(role);

        user=userDao.persist(user);

        return created(String.valueOf(user.getId()));

    }

    @Transactional
    public Result deleteUser(){

        final JsonNode jsonNode = request().body().asJson();
        final String username = jsonNode.get("username").asText();

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