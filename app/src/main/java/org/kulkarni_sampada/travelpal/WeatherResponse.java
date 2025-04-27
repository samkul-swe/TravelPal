package org.kulkarni_sampada.travelpal;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponse {
    public List<Weather> weather; // List of Weather objects
}

class WeatherResponseList {
    public List<WeatherResponse> list;
}

class Weather {
    @SerializedName("main")
    public String main;
    @SerializedName("description")
    public String description;
}
