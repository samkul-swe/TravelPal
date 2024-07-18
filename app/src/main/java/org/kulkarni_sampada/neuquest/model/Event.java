package org.kulkarni_sampada.neuquest.model;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;

import java.io.Serializable;

public class Event implements Serializable {
    private final String title;
    private String startTime;
    private String endTime;
    private String startDate;
    private String endDate;
    private final String description;
    private String price;
    private String location;
    private String registerLink;
    private final String image;

    public Event(String title, String description, String image) {
        this.title = title;
        this.description = description;
        this.image = image;
    }

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

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Uri getImage() {

        Task<Uri> urlTask = FirebaseStorage.getInstance().getReference().child(image).getDownloadUrl();
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

