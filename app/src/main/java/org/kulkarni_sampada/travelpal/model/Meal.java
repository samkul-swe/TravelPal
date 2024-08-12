package org.kulkarni_sampada.travelpal.model;

import java.io.Serializable;

public class Meal implements Serializable {
    private String id;
    private String name;
    private String price;
    private String cuisine;

    public Meal() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    @Override
    public String toString() {
        return "Meal{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price='" + price + '\'' +
                ", cuisine='" + cuisine + '\'' +
                '}';
    }
}
