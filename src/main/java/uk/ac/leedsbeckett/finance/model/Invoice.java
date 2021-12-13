package uk.ac.leedsbeckett.finance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.RandomStringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Locale;

@Entity
@Data
public class Invoice {

    private @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;
    @Column(unique = true)
    @NotBlank(message = "{reference.required}")
    @Size(min = 8, max = 8, message = "{reference.size}")
    @Pattern(regexp = "[A-Z0-9]*", message = "{reference.format}")
    private String reference;
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

    public Invoice(Double amount, LocalDate dueDate, Type type, Account account) {
        this.amount = amount;
        this.dueDate = dueDate;
        this.type = type;
        this.account = account;
        populateReference();
    }

    public void populateReference() {
        if (this.reference == null) {
            this.reference = RandomStringUtils.random(8, true, true).toUpperCase(Locale.UK);
        }
    }

}
