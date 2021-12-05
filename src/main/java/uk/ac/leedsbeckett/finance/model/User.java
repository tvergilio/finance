package uk.ac.leedsbeckett.finance.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "FINANCE_USER")
public class User {

    private @Id @GeneratedValue(strategy= GenerationType.IDENTITY) Long id;
    private String name;
    private String role;

    public User() {
    }

    public User(String name, String role) {
        this.name = name;
        this.role = role;
    }
}