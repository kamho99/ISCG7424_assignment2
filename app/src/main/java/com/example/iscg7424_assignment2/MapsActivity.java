package com.example.iscg7424_assignment2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends AppCompatActivity {

    Spinner spinner_box;
    Button button_weather;
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;
    TextView Text_weather;
    TextView Text_temp;
    RequestQueue weather_Q;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        spinner_box = findViewById(R.id.spin_box);
        button_weather = findViewById(R.id.btn_weather);
        Text_weather = findViewById(R.id.txt_weather);
        Text_temp = findViewById(R.id.txt_temp);


        // Obtain the SupportMapFragment
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);

        //get map permission
        if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //if permission graded
            getCurrentLocation();
        } else {
            //if permission not graded, ask for permission
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        //button on click
        button_weather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWeather();
            }
        });

        //navigation bar
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setSelectedItemId(R.id.navigation_map);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_map:
                        return true;
                    case R.id.navigation_home:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

    }

    private void getCurrentLocation() {
        client = LocationServices.getFusedLocationProviderClient(this);
        @SuppressLint("MissingPermission")
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location != null) {
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions options = new MarkerOptions().position(point).title("you are here");
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 10));
                            googleMap.addMarker(options);
                        }
                    });
                }
            }
        });

    }

    private void getWeather() {
        weather_Q = Volley.newRequestQueue(MapsActivity.this);
        client = LocationServices.getFusedLocationProviderClient(this);
        @SuppressLint("MissingPermission")
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location != null) {
                    String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + location.getLatitude()
                            + "&lon=" + location.getLongitude() + "&units=metric&appid=a035e9b73f7acc164a03ef81f29ebb25";
                    JSONObject jsonObject = null;
                    JsonObjectRequest request = new JsonObjectRequest (Request.Method.GET, url, jsonObject, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray weatherArray = response.getJSONArray("weather");
                                JSONObject weather = weatherArray.getJSONObject(0);
                                String description = weather.getString("description");
                                Text_weather.setText("current status: " + description);
                                JSONObject temp = response.getJSONObject("main");
                                int temperature = temp.getInt("temp");
                                int max_temp = temp.getInt("temp_max");
                                int min_temp = temp.getInt("temp_min");
                                Text_temp.setText("current temperature: " + temperature
                                        + "°C\nMaximum temp: " + max_temp
                                        + "°C\nMinimum temp: " + min_temp + "°C");
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Text_weather.setText("error Json");
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Text_weather.setText("error no response");
                            Log.d("Error", "error no response", error);
                        }
                    });
                    weather_Q.add(request);
                }
            }
        });

    }

    //try again when permission graded
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }
}