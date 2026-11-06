package io.github.mickablondo.aturkey.example;

import io.github.mickablondo.aturkey.AturKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class Customer {
    @Id
    @GeneratedValue
    private Long id;

    @Version
    private long version;

    @AturKey
    @Column(nullable = false, unique = true)
    private String email;

    private String name;

    protected Customer() {
    }

    public Customer(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
