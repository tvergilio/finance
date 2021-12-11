package uk.ac.leedsbeckett.finance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
public class Invoice {

    private @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;
    private Double amount;
    private LocalDate dueDate;
    private Type type;
    private Status status;
    @ManyToOne
    @JoinColumn(name="account_fk",referencedColumnName="id")
    @ToString.Exclude
    private Account account;

    @JsonProperty
    public String getStudentId() {
        return account.getStudentId();
    }

    @JsonProperty
    public void setAccount(Account account) {
        this.account = account;
    }

    @JsonIgnore
    public Account getAccount() {
        return this.account;
    }

    public Invoice() {
    }

    public Invoice(Double amount, LocalDate dueDate, Type type, Status status, Account account) {
        this.amount = amount;
        this.dueDate = dueDate;
        this.type = type;
        this.status = status;
        this.account = account;
    }

}
