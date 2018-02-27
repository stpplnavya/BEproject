package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.Security.Authenticator;
import daos.FeatureDao;
import models.Feature;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureController extends Controller {

    private final static Logger.ALogger LOGGER = Logger.of(FeatureController.class);
    private Map<Integer, Feature> feature = new HashMap<>();

    private FeatureDao featureDao;

    @javax.inject.Inject
    public FeatureController (FeatureDao featureDao) {
        this.featureDao = featureDao;

    }

    @Transactional
    @Authenticator
    public Result createForm() {

        //Logger.

        JsonNode jsonNode = request().body().asJson();

        final String templename = jsonNode.get("templename").asText();
        final String place = jsonNode.get("place").asText();
        final String timings = jsonNode.get("timings").asText();
        final Integer visitors = Integer.valueOf(jsonNode.get("visitors").asText());
        final String feature = jsonNode.get("feature").asText();

        Feature form = new Feature();
        form.setTemplename(templename);
        form.setPlace(place);
        form.setTimings(timings);
        form.setVisitors(visitors);

        LOGGER.debug("user id before: {}", form.getId());

        featureDao.persist(form);

        LOGGER.debug("user id after: {}", form.getId());
        // store in DB
        // return user id
        return created(form.getId().toString());
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
    public Result getAllForms() {

        final List<Feature> features = featureDao.findAllForms();
        final JsonNode jsonNode = Json.toJson(features);
        return (ok(jsonNode));
    }

}
