package models;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;

@Entity
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    @Basic
    private String name;

    @Basic
    private String description;

    @Basic
    private String state;

    @Basic
    @JsonBackReference
    @ManyToOne
    private User admin;

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
