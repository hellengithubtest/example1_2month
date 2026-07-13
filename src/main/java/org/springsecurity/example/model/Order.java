package org.springsecurity.example.model;

import jakarta.persistence.*;

@Entity
@SequenceGenerator(
    name = "order_seq_gen",
    sequenceName = "order_seq",
    initialValue = 2010
)
@Table(name = "orders")
public class Order {

    private long id;
    private double amount;
    private Company customer;

    @Id
    @GeneratedValue(generator = "order_seq_gen")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @ManyToOne
    public Company getCustomer() {
        return customer;
    }

    public void setCustomer(Company customer) {
        this.customer = customer;
    }
}
