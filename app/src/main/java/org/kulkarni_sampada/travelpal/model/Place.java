package org.kulkarni_sampada.travelpal.model;

import java.io.Serializable;

public class Place implements Serializable {
    private String id;
    private String name;
    private String time;
    private String date;
    private String description;
    private String price;

    public Place() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Place{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", time='" + time + '\'' +
                ", date='" + date + '\'' +
                ", description='" + description + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}

