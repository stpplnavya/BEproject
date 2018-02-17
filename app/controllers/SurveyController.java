package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.Security.Authenticator;
import daos.SurveyDao;
import models.Survey;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyController extends Controller {

    private final static Logger.ALogger LOGGER = Logger.of(SurveyController.class);
    private Map<Integer, Survey> survey = new HashMap<>();

    private SurveyDao surveyDao;

    @javax.inject.Inject
    public SurveyController (SurveyDao surveyDao) {
        this.surveyDao = surveyDao;
    }

    @Transactional
    @Authenticator
    public Result createSurvey() {

        JsonNode jsonNode = request().body().asJson();

        final String name = jsonNode.get("name").asText();
        final String description = jsonNode.get("description").asText();
        final String state = jsonNode.get("state").asText();

        Survey survey = new Survey();
        survey.setName(name);
        survey.setDescription(description);
        survey.setState(state);

        LOGGER.debug("user id before: {}", survey.getId());

        surveyDao.persist(survey);

        LOGGER.debug("user id after: {}", survey.getId());
        // store in DB
        // return user id
        return created(survey.getId().toString());
    }

    @Transactional
    @Authenticator
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
    public Result getAllSurveys() {

        final List<Survey> surveys = surveyDao.findAllSurveys();
        final JsonNode jsonNode = Json.toJson(surveys);
        return (ok(jsonNode));
    }

}
