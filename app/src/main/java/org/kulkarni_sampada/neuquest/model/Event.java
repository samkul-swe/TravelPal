package org.kulkarni_sampada.neuquest.model;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;

import java.io.Serializable;

public class Event implements Serializable {
    private String title;
    private String startTime;
    private String endTime;
    private String startDate;
    private String endDate;
    private String description;
    private String price;
    private String location;
    private String registerLink;
    private String image;

    public Event() {}

    public Event(String title, String startTime, String startDate, String endTime, String endDate, String description, String price, String location, String image, String registerLink) {
        this.title = title;
        this.startTime = startTime;
        this.startDate = startDate;
        this.endTime = endTime;
        this.endDate = endDate;
        this.description = description;
        this.price = price;
        this.location = location;
        this.image = image;
        this.registerLink = registerLink;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setRegisterLink(String registerLink) {
        this.registerLink = registerLink;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Uri getImage() {

        Task<Uri> urlTask = FirebaseStorage.getInstance().getReference().child(this.image).getDownloadUrl();
        while (!urlTask.isSuccessful());
        return urlTask.getResult();
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getPrice() {
        return price;
    }

    public String getLocation() {
        return location;
    }

    public String getRegisterLink() {
        return registerLink;
    }
}

