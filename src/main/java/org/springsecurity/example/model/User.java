package org.springsecurity.example.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@SequenceGenerator(
    name = "user_seq_gen",
    sequenceName = "user_seq",
    initialValue = 3010
)
@Table(name = "users")
public class User {

    private long id;
    private String login;
    private String password;
    private List<String> roles;
    private List<Company> companies;

    @Id
    @GeneratedValue(generator = "user_seq_gen")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(nullable = false, unique = true)
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Column(nullable = false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @ElementCollection
    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @JoinTable(name = "user_company")
    @ManyToMany
    public List<Company> getCompanies() {
        return companies;
    }

    public void setCompanies(List<Company> companies) {
        this.companies = companies;
    }
}
