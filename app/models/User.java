package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.List;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Basic
    private String username;

    @Basic
    private String password;

    @Basic
    private String email;

    @Basic
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Role role;

    @Basic
    private String token;

    @Basic
    private Long threshold;

    @Basic
    private String salt;

    @Basic
    private String reftoken;

    @JsonIgnore
    @OneToMany(mappedBy = "admin")
    private List<Survey> surveys;

    public List<Survey> getSurveys() {
        return surveys;
    }

    public void setSurveys(List<Survey> surveys) {
        this.surveys = surveys;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonIgnore
    public String getReftoken() {
        return reftoken;
    }

    public void setReftoken(String reftoken) {
        this.reftoken = reftoken;
    }

    public enum Role {
        Admin,
        User
    }

    public User (String username){

        this.username=username;
    }

     public void setRefToken(String refToken) {
        this.reftoken = refToken;
    }

    @JsonIgnore
    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @JsonIgnore
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

    @JsonIgnore
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

    @JsonIgnore
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