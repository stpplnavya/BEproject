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


    public User findByToken(String token) {

        TypedQuery<User> query = jpaApi.em().createQuery("SELECT u FROM User u where token = :token", User.class);
        query.setParameter("token",token);
        List<User> users = query.getResultList();
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);


    }

    public User findByRefreshToken(String reftoken) {

        TypedQuery<User> query = jpaApi.em().createQuery("SELECT u FROM User u where reftoken = :reftoken", User.class);
        query.setParameter("reftoken",reftoken);
        List<User> users = query.getResultList();
        if (users.isEmpty()) {
                      return null;
        }

        return users.get(0);

    }



    public User findByName(String username) {

        TypedQuery<User> query = jpaApi.em().createQuery("select u from User u where username='" + username + "'", User.class);
        final List<User> users = query.getResultList();

        if (users.isEmpty()) {
              return null;
        }

        return users.get(0);
    }

    public List<User> findAll() {

        TypedQuery<User> query = jpaApi.em().createQuery("SELECT u FROM User u", User.class);
        List<User> users = query.getResultList();

        return users;
    }


}
