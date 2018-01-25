package daos;

import models.User;
import play.db.jpa.JPAApi;
import play.mvc.Result;

import javax.inject.Inject;
import javax.persistence.TypedQuery;
import java.util.List;

public class UserDao {

    private JPAApi jpaApi;

    @Inject
    public UserDao(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

    public User persist(User user) {

        jpaApi.em().persist(user);

        return user;
    }

    public User deleteUser(String username) {

        final User user = findById(username);
        if (null == user) {
            return null;
        }

        jpaApi.em().remove(user);

        return user;
    }

    public User findById(String username) {

        final User user = jpaApi.em().find(User.class, username);
        return user;

    }

    public Result findByName(String username) {

        return null;
    }

    public List<User> findAll() {

        TypedQuery<User> query = jpaApi.em().createQuery("SELECT u FROM User u", User.class);
        List<User> users1 = query.getResultList();

        return users1;
    }

}
