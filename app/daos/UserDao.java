package daos;

import models.User;
import play.Logger;
import play.db.jpa.JPAApi;

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


    public List<User> findByToken(String token) {

        TypedQuery<User> query = jpaApi.em().createQuery("SELECT u FROM User u where token = :token", User.class);
        Logger.debug("Query result : " + query);
        query.setParameter("token",token);
        List<User> result1 = query.getResultList();

        return result1;


    }

    public List<User> findByRefreshToken(String reftoken) {

        TypedQuery<User> query = jpaApi.em().createQuery("SELECT u FROM User u where reftoken = :reftoken", User.class);
        Logger.debug("Query result : " + query);
        query.setParameter("reftoken",reftoken);
        List<User> result1 = query.getResultList();

        return result1;


    }



    public List<User> findByName(String username) {

        TypedQuery<User> query = jpaApi.em().createQuery("select u from User u where username='" + username + "'", User.class);
        Logger.debug(String.valueOf(query));
        final List<User> Result = query.getResultList();

        return Result;
    }

    public List<User> findAll() {

        TypedQuery<User> query = jpaApi.em().createQuery("SELECT u FROM User u", User.class);
        List<User> users1 = query.getResultList();

        return users1;
    }


}
