package org.springsecurity.example.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@SequenceGenerator(
    name = "company_seq_gen",
    sequenceName = "company_seq",
    initialValue = 1010
)
@Table(name = "company")
public class Company {

    private long id;
    private String name;
    private List<Order> orders;

    @Id
    @GeneratedValue(generator = "company_seq_gen")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(mappedBy = "customer")
    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
