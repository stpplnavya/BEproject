package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.Security.Authenticator;
import controllers.Security.IsAdmin;
import daos.SurveyDao;
import daos.UserDao;
import javassist.NotFoundException;
import models.Survey;
import models.User;
import play.Logger;
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

    private SurveyDao surveyDao;
    private UserDao userDao;

    @javax.inject.Inject
    public SurveyController (SurveyDao surveyDao, UserDao userDao) {
        this.surveyDao = surveyDao;
        this.userDao= userDao;
    }

    @Transactional
    @Authenticator
    @IsAdmin
    public Result createSurvey() {

        JsonNode jsonNode = request().body().asJson();
        final Integer userId = jsonNode.get("user_id").asInt();
        final String name = jsonNode.get("name").asText();
        final String description = jsonNode.get("description").asText();
        final String state = jsonNode.get("state").asText();

        User user = userDao.findById(userId);

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

        userDao.persist(user);
        LOGGER.debug("user id after: {}", survey.getId());
        return created(survey.getId().toString());
    }

    @Transactional
    @Authenticator
    public Result getSurveyById(Integer id) throws NotFoundException {

        if (null == id) {
            return badRequest();
        }

        final Survey survey = surveyDao.findById(id);
        final JsonNode json = Json.toJson(survey);
        return ok(json);
    }

    @Transactional
    @Authenticator
    public Result getSurveysOfUser() {

        final User user = (User) ctx().args.get("user");
        List<Survey> surveys = user.getSurveys();
        LOGGER.debug("Suverys {}", surveys.size());

        final JsonNode json = Json.toJson(surveys);
        return ok(json);
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
    public Result updateSurveyById(Integer id) throws NotFoundException {

        if (null == id) {
            return badRequest();
        }

        final JsonNode json = request().body().asJson();
        if (null == json) {
            Logger.error("Unable to get json from request");
            return badRequest();
        }

        final Survey survey = Json.fromJson(json, Survey.class);
        if (null == survey) {
            Logger.error("Unable to parse json to Book object");
            return badRequest();
        }

        if (null == survey.getName()) {
            return badRequest();
        }

        if (null == survey.getDescription()) {
            return badRequest();
        }

        if (null == survey.getState()) {
            return badRequest();
        }

        surveyDao.update(survey);
        return ok(json);
    }




    @Transactional
    @Authenticator
    public Result getAllSurveys() {

        final List<Survey> surveys = surveyDao.findSAllSurveys();
        Logger.debug("Result : "+ surveys);
        final JsonNode jsonNode = Json.toJson(surveys);
        return (ok(jsonNode));
    }

}
