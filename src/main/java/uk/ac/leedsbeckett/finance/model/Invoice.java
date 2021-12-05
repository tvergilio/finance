package uk.ac.leedsbeckett.finance.model;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class Invoice {

    private @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;
    private Double amount;
    private LocalDateTime dueDate;
    private Status status;
    @ManyToOne
    @JoinColumn(name="account_fk",referencedColumnName="id")
    @ToString.Exclude
    private Account account;

    public Invoice() {
    }

    public Invoice(Double amount, LocalDateTime dueDate, Status status, Account account) {
        this.amount = amount;
        this.dueDate = dueDate;
        this.status = status;
        this.account = account;
    }

}
