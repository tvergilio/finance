package uk.ac.leedsbeckett.finance.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Invoice {

    private @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;
    private Double amount;
    private LocalDateTime dueDate;
    private Status status;

    public Invoice() {
    }

    public Invoice(Double amount, LocalDateTime dueDate, Status status) {
        this.amount = amount;
        this.dueDate = dueDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invoice)) return false;
        Invoice invoice = (Invoice) o;
        return id.equals(invoice.id) && amount.equals(invoice.amount) && dueDate.equals(invoice.dueDate) && status.equals(invoice.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, dueDate, status);
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", amount=" + amount +
                ", dueDate=" + dueDate +
                ", status=" + status +
                '}';
    }
}
