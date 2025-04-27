package org.kulkarni_sampada.travelpal;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.gms.location.FusedLocationProviderClient;

public class PlaceRecommendationActivity extends AppCompatActivity {

    private EditText editTextLocation;
    private EditText editTextDate;
    private TextView textViewWeather;
    private OpenWeatherAPIInterface openWeatherAPIInterface;
    private FusedLocationProviderClient fusedLocationClient;
    private final String apiKey = BuildConfig.apikey;
    String latitude, longitude;
    int diffInDays;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_recommendation);

        editTextLocation = findViewById(R.id.editTextLocation);
        editTextDate = findViewById(R.id.editTextDate);
        Button buttonSubmit = findViewById(R.id.buttonSubmit);
        Button buttonGetLocation = findViewById(R.id.buttonGetLocation);
        textViewWeather = findViewById(R.id.textViewWeather);
        openWeatherAPIInterface = OpenWeatherAPIClient.getClient().create(OpenWeatherAPIInterface.class);

        buttonGetLocation.setOnClickListener(v -> getCurrentLocation());

        buttonSubmit.setOnClickListener(v -> {
            String location = editTextLocation.getText().toString().trim();
            if (location.isEmpty()) {
                Toast.makeText(this, "Please enter a location or get your current location", Toast.LENGTH_SHORT).show();
                return;
            }
            getLocation(location);
        });
    }

    private int calculateDateDifference(String inputDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date storedDate = dateFormat.parse(inputDate);
            System.out.println(storedDate);
            if (storedDate != null) {
                Date today = new Date();
                System.out.println(today);
                long diffInMillis = today.getTime() - storedDate.getTime();
                long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
                return (int) diffInDays;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format. Please use yyyy-MM-dd.", Toast.LENGTH_SHORT).show();
        }
        return 0;
    }

    private void getLocation(String location) {
        Call<List<Location>> call = openWeatherAPIInterface.getLocation(location,1, apiKey);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Location>> call, @NonNull Response<List<Location>> response) {
                List<Location> locationList = response.body();
                if (locationList == null || locationList.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "No location found", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (Location location : locationList) {
                    latitude = location.lat;
                    longitude = location.lon;
                }

                // Get the date input from the EditText
                String inputDate = editTextDate.getText().toString();
                if (inputDate.isEmpty()) {
                    Toast.makeText(PlaceRecommendationActivity.this, "Please enter a date.", Toast.LENGTH_SHORT).show();
                } else {
                    diffInDays = calculateDateDifference(inputDate);
                    if (latitude!=null && longitude!=null) {
                        System.out.println("Difference " + diffInDays);
                        if (diffInDays > 0) {
                            getForecast(latitude,longitude, diffInDays);
                        } else {
                            getWeather(latitude,longitude);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Location>> call, @NonNull Throwable throwable) {
                call.cancel();
            }
        });
    }

    private void getForecast(String latitude,String longitude, long diffInDays) {
        Call<WeatherResponseList> call = openWeatherAPIInterface.getForecast(latitude,longitude,(int) diffInDays, apiKey);
        call.enqueue(new Callback<>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<WeatherResponseList> call, @NonNull Response<WeatherResponseList> response) {
                WeatherResponseList weatherResponseList = response.body();
                assert weatherResponseList != null;
                List<WeatherResponse> weatherList = weatherResponseList.list;
                assert weatherList != null;
                List<Weather> weathers = weatherList.get((int) (diffInDays-1)).weather;

                for (Weather weather : weathers) {
                    textViewWeather.setText(weather.main + " " + weather.description);
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponseList> call, @NonNull Throwable throwable) {
                call.cancel();
            }
        });

    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        android.location.Location location = task.getResult();
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        getWeather(Double.toString(latitude), Double.toString(longitude));
                    } else {
                        Toast.makeText(PlaceRecommendationActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getWeather(String latitude, String longitude) {
        Call<WeatherResponse> call = openWeatherAPIInterface.getWeather(latitude,longitude, apiKey);
        call.enqueue(new Callback<>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                WeatherResponse weatherResponse = response.body();
                assert weatherResponse != null;
                List<Weather> weatherList = weatherResponse.weather;

                for (Weather weather : weatherList) {
                    textViewWeather.setText(weather.main + " " + weather.description);
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable throwable) {
                call.cancel();
            }
        });
    }
}