package uk.ac.leedsbeckett.finance.model;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

@Entity
@Data
public class Account {

    private @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    Long id;
    private String studentId;
    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true)
    @ToString.Exclude
    private List<Invoice> invoiceList = new ArrayList<>();

    public Account() {
    }

    public Account(String studentId) {
        this.studentId = studentId;
    }
 }