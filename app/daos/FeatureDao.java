package daos;

import models.Feature;
import play.Logger;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.TypedQuery;
import java.util.List;

public class FeatureDao {
    private JPAApi jpaApi;

    @Inject
    public FeatureDao(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

    public Feature persist(Feature form) {

        jpaApi.em().persist(form);
        return form;
    }

    public Feature findById(Integer id) {

        final Feature form = jpaApi.em().find(Feature.class, id);
        return form;
    }

    public Feature findByName(String templename) {

        TypedQuery<Feature> query = jpaApi.em().createQuery("select e from Feature e where templename='" + templename + "'", Feature.class);
        Logger.debug("templename"+templename);
        final List<Feature> forms = query.getResultList();

        if (forms.isEmpty()) {
            return null;
        }

        return forms.get(0);
    }

    public Feature deleteByName(String templename) {

        final Feature feature = findByName(templename);
        if (null == feature) {
            return null;
        }
        jpaApi.em().remove(feature);
        return feature;
    }

    public List<Feature> findAllForms() {

        TypedQuery<Feature> query = jpaApi.em().createQuery("SELECT e FROM Feature e", Feature.class);
        List<Feature> forms = query.getResultList();

        return forms;
    }
}
