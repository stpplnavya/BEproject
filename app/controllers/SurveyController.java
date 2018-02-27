package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Security.Authenticator;
import controllers.Security.IsAdmin;
import daos.SurveyDao;
import daos.UserDao;
import models.Survey;
import models.User;
import play.Logger;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyController extends Controller {

    private final static Logger.ALogger LOGGER = Logger.of(SurveyController.class);
    private Map<Integer, Survey> survey = new HashMap<>();

    private JPAApi jpaApi;
    private SurveyDao surveyDao;
    private UserDao userDao;

    @javax.inject.Inject
    public SurveyController (SurveyDao surveyDao, UserDao userDao,JPAApi jpaApi) {
        this.surveyDao = surveyDao;
        this.userDao= userDao;
        this.jpaApi=jpaApi;
    }

    public User persist(User user) {

        jpaApi.em().persist(user);
        return user;
    }

    @Transactional
    @Authenticator
    @IsAdmin
    public Result createSurvey() {

        User user = (User) ctx().args.get("user");

        //if (user.getRole() != User.Role.Admin) {
            //return forbidden();
        //}

        JsonNode jsonNode = request().body().asJson();
        final String name = jsonNode.get("name").asText();
        final String description = jsonNode.get("description").asText();
        final String state = jsonNode.get("state").asText();

        Survey survey = new Survey();
        survey.setName(name);
        survey.setDescription(description);
        survey.setState(state);
        survey.setAdmin(user);

        LOGGER.debug("user id before: {}", survey.getId());

        survey = surveyDao.persist(survey);

        if (null == user.getSurveys()) {
            List<Survey> surveys= new ArrayList<>();
            surveys.add(survey);
            user.setSurveys(surveys);
        } else {
            user.getSurveys().add(survey);
        }

        // Write updated user back to database
        user= userDao.persist(user);


        LOGGER.debug("user id after: {}", survey.getId());
        // store in DB
        // return user id
        return created(survey.getId().toString());
    }

    @Transactional
    @Authenticator
    @IsAdmin
    public Result deleteSurvey() {
        final JsonNode jsonNode = request().body().asJson();
        final String name = jsonNode.get("name").asText();

        if (null == name) {
            return badRequest("Missing user name");
        }

        final Survey survey = surveyDao.deleteByName(name);

        if(null==survey){
            return notFound("user with the following username nt found"+name);
        }

        return noContent();
    }

    @Transactional
    @Authenticator
    public Result updateSurvey(String name) {

        final JsonNode jsonNode = request().body().asJson();
        final String state = jsonNode.get("state").asText();

        if(null == state) {
            return badRequest("Missing user name of state");
        }
        final Survey survey = surveyDao.findByName(name);
        survey.setState(state);
        surveyDao.persist(survey);

        ObjectNode result1 = Json.newObject();
        result1.put("name", name);
        result1.put("state", state);

        return ok(result1);
    }

    @Transactional
    @Authenticator
    public Result getAllSurveys() {

        final List<Survey> surveys = surveyDao.findAllSurveys();
        final JsonNode jsonNode = Json.toJson(surveys);
        return (ok(jsonNode));
    }

}
