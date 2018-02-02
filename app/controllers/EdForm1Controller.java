package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.Security.Authenticator;
import daos.UserDao;
import models.EdForm1;
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

public class EdForm1Controller extends Controller {

    private final static Logger.ALogger LOGGER = Logger.of(EdForm1Controller.class);
    private Map<Integer, EdForm1> Form = new HashMap<>();

    private JPAApi jpaApi;
    private UserDao userDao;

    @javax.inject.Inject
    public EdForm1Controller (UserDao userDao, JPAApi jpaApi) {
        this.userDao = userDao;
        this.jpaApi =  jpaApi;
    }
    private Integer index = 0;



    @Transactional
    @Authenticator
    public Result createForm() {

        JsonNode jsonNode = request().body().asJson();

        final String templename = jsonNode.get("templename").asText();
        final String place = jsonNode.get("place").asText();
        final String timings = jsonNode.get("timings").asText();
        final Integer visitors = Integer.valueOf(jsonNode.get("visitors").asText());
        final String feature = jsonNode.get("feature").asText();


        EdForm1 form = new EdForm1();
        form.setTemplename(templename);
        form.setPlace(place);
        form.setTimings(timings);
        form.setVisitors(visitors);
        form.setFeature(feature);


        LOGGER.debug("user id before: {}", form.getId());

        jpaApi.em().persist(form);

        LOGGER.debug("user id after: {}", form.getId());
        // store in DB
        // return user id
        return created(form.getId().toString());

    }


    @Transactional
    public Result getForm() {

        final Collection<EdForm1> forms = this.Form.values();
        TypedQuery<EdForm1> query = jpaApi.em().createQuery("SELECT u FROM EdForm1 u", EdForm1.class);
        List<EdForm1> forms1= query.getResultList();

        final JsonNode jsonNode = Json.toJson(forms1);
        return (ok(jsonNode));
    }

}
