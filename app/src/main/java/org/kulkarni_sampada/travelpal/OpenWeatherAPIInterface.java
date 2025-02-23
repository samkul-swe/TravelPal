package org.kulkarni_sampada.travelpal;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenWeatherAPIInterface {
    @GET("data/2.5/weather?")
    Call<WeatherResponse> getWeather(
            @Query("lat") String latitude,
            @Query("lon") String longitude,
            @Query("appid") String apiKey
    );

    @GET("geo/1.0/direct?")
    Call<List<Location>> getLocation(@Query("q") String location, @Query("limit") int limit, @Query("appid") String apiKey);
}