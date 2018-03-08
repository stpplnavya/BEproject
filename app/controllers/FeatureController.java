package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.Security.Authenticator;
import daos.FeatureDao;
import daos.SurveyDao;
import models.Feature;
import models.Survey;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureController extends Controller {

    private final static Logger.ALogger LOGGER = Logger.of(FeatureController.class);
    private Map<Integer, Feature> feature = new HashMap<>();

    private FeatureDao featureDao;
    private SurveyDao surveyDao;
    @javax.inject.Inject
    public FeatureController (FeatureDao featureDao, SurveyDao surveyDao) {
        this.featureDao = featureDao;
        this.surveyDao = surveyDao;
    }

    @Transactional
    @Authenticator
    public Result createForm() {

        JsonNode jsonNode = request().body().asJson();
        final Integer surveyId = jsonNode.get("survey_id").asInt();
        final String templename = jsonNode.get("templename").asText();
        final String place = jsonNode.get("place").asText();
        final String timings = jsonNode.get("timings").asText();
        final Integer visitors = Integer.valueOf(jsonNode.get("visitors").asText());

        Survey survey1 = surveyDao.findById(surveyId);

        Feature form = new Feature();
        form.setTemplename(templename);
        form.setPlace(place);
        form.setTimings(timings);
        form.setVisitors(visitors);
        form.setSurvey(survey1);

        LOGGER.debug("Form id before: {}", form.getId());

        form = featureDao.persist(form);

        if (null == survey1.getFeatures()) {
            List<Feature> features= new ArrayList<>();
            features.add(form);
            survey1.setFeatures(features);
        } else {
            survey1.getFeatures().add(form);
        }

        surveyDao.persist(survey1);
        LOGGER.debug("Form id after: {}", form.getId());

        return created(form.getId().toString());
    }


    @Transactional
    @Authenticator
    public Result getFeaturesOfSurvey(Integer id) {

        if(null == id){
            return badRequest();
        }
        final Survey survey = surveyDao.findById(id);
        List<Feature> features = survey.getFeatures();
        final JsonNode json = Json.toJson(features);
        return ok(json);
    }

    @Transactional
    @Authenticator
    public Result deleteForm() {
        final JsonNode jsonNode = request().body().asJson();
        final String templename = jsonNode.get("templename").asText();

        if (null == templename) {
            return badRequest("Missing user name");
        }

        final Feature feature = featureDao.deleteByName(templename);

        if(null==feature){
            return notFound("No features with given name found! ");
        }

        return noContent();
    }

    @Transactional
    @Authenticator
    public Result updateForm(String templename){

        final JsonNode jsonNode = request().body().asJson();
        final String place = jsonNode.get("place").asText();

        if(null == place){
            return badRequest("Missing data to be updated");
        }

        final Feature feature = featureDao.findByName(templename);

        feature.setPlace(place);
        featureDao.persist(feature);

        return ok("updated the change you made");

    }

    @Transactional
    @Authenticator
    public Result getAllForms() {

        final List<Feature> features = featureDao.findAllForms();
        final JsonNode jsonNode = Json.toJson(features);
        return (ok(jsonNode));
    }

}
