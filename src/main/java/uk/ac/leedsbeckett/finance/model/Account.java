package uk.ac.leedsbeckett.finance.model;

import java.util.List;
import java.util.Objects;

import javax.persistence.*;

@Entity
public class Account {

    private @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    Long id;
    private String studentId;
    @OneToMany
    private List<Invoice> invoiceList;

    public Account() {
    }

    public Account(String studentId, List<Invoice> invoiceList) {
        this.studentId = studentId;
        this.invoiceList = invoiceList;
    }

    public Long getId() {
        return this.id;
    }

    public String getStudentId() {
        return this.studentId;
    }

    public List<Invoice> getInvoiceList() {
        return this.invoiceList;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setInvoiceList(List<Invoice> invoiceList) {
        this.invoiceList = invoiceList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return id.equals(account.id) && studentId.equals(account.studentId) && Objects.equals(invoiceList, account.invoiceList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, studentId, invoiceList);
    }

    @Override
    public String toString() {
        return "User{" + "id=" + this.id + ", studentId='" + this.studentId + '\'' + ", invoiceList='" + this.invoiceList + '\'' + '}';
    }
}