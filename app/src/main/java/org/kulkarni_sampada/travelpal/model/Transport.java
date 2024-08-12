package org.kulkarni_sampada.travelpal.model;

import java.io.Serializable;

public class Transport implements Serializable {
    private String id;
    private String type;
    private String mode;
    private String time;
    private String cost;

    Transport() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "Transport{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", mode='" + mode + '\'' +
                ", time='" + time + '\'' +
                ", cost='" + cost + '\'' +
                '}';
    }
}
