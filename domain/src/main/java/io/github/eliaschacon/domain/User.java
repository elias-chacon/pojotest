package io.github.eliaschacon.domain;

import io.github.eliaschacon.test.TestGenerator;

@TestGenerator
public class User {
    private Integer id;

    private String name;

    private String address;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String street, int number, String neighbour, String city) {
        this.address = String.format("%d %s, %s. %s", number, street, neighbour, city);
    }

    public String getAddress() {
        return address;
    }
}
