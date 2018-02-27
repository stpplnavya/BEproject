package daos;

import models.Survey;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.TypedQuery;
import java.util.List;

public class SurveyDao {

    private JPAApi jpaApi;

    @Inject
    public SurveyDao(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

    public Survey persist(Survey survey) {

        jpaApi.em().persist(survey);
        return survey;
    }

    public Survey findById(Integer id) {

        final Survey survey = jpaApi.em().find(Survey.class, id);
        return survey;
    }

    public Survey findByName(String surveyname) {

        TypedQuery<Survey> query = jpaApi.em().createQuery("select s from Survey s where name='" + surveyname + "'", Survey.class);
        final List<Survey> surveys = query.getResultList();

        if (surveys.isEmpty()) {
            return null;
        }

        return surveys.get(0);
    }

    public Survey deleteByName(String name) {
        final Survey survey = findByName(name);
        if (null == survey) {
            return null;
        }
        jpaApi.em().remove(survey);
        return survey;
    }

    public List<Survey> findAllSurveys() {

        TypedQuery<Survey> query = jpaApi.em().createQuery("SELECT s FROM Survey s", Survey.class);
        List<Survey> surveys = query.getResultList();

        return surveys;
    }
}
