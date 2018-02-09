package models;

import javax.persistence.*;

@Entity
public class User {

    private final static play.Logger.ALogger LOGGER = play.Logger.of(User.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    @Basic
    String username;

    @Basic
    String password;

    public enum Role {
        Admin,
        User
    }

    @Basic
    Role role;

    @Basic
    String token;

    @Basic
    Long threshold;

    @Basic
    String salt;

    @Basic
    String reftoken;

    public String getRefToken() {
        return reftoken;
    }

    public void setRefToken(String refToken) {
        this.reftoken = refToken;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Long getThreshold() {
        return threshold;
    }

    public void setThreshold(Long threshold) {
        this.threshold = threshold;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public User() {

    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {
       this.username = username;
    }

    public Integer getId() {

        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}