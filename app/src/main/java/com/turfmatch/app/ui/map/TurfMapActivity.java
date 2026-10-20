package com.turfmatch.app.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.turfmatch.app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// CO2 & CO3 - Google Maps Turf Locator with 10km Radius Filtering around User Location
public class TurfMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final float TEN_KM_IN_METERS = 10000f; // 10 km radius filter

    // Default reference location: Borivali Railway Station, Mumbai 400092
    private static final double BORIVALI_LAT = 19.2294;
    private static final double BORIVALI_LNG = 72.8574;

    private static class TurfItem {
        String name;
        String address;
        double lat;
        double lng;

        TurfItem(String name, String address, double lat, double lng) {
            this.name = name;
            this.address = address;
            this.lat = lat;
            this.lng = lng;
        }
    }

    private final List<TurfItem> masterTurfList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turf_map);

        initMasterTurfList();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initMasterTurfList() {
        masterTurfList.add(new TurfItem("Borivali Station Turf", "Borivali Station East, Mumbai", 19.2294, 72.8574));
        masterTurfList.add(new TurfItem("Gorai Sports Arena", "Gorai, Borivali West", 19.2370, 72.8430));
        masterTurfList.add(new TurfItem("Shimpoli Football Turf", "Shimpoli, Borivali West", 19.2240, 72.8480));
        masterTurfList.add(new TurfItem("IC Colony Turf Grounds", "IC Colony, Borivali West", 19.2450, 72.8520));
        masterTurfList.add(new TurfItem("Eksar Football Ground", "Eksar Road, Borivali West", 19.2350, 72.8500));
        masterTurfList.add(new TurfItem("Thakur Village Turf Arena", "Thakur Village, Kandivali East", 19.2110, 72.8720));
        masterTurfList.add(new TurfItem("Kandivali Sports Complex", "Kandivali West, Mumbai", 19.2060, 72.8520));
        masterTurfList.add(new TurfItem("Charkop Football Turf", "Charkop, Kandivali West", 19.2150, 72.8300));
        masterTurfList.add(new TurfItem("Dahisar Sports Club Turf", "Dahisar East, Mumbai", 19.2550, 72.8630));
        masterTurfList.add(new TurfItem("Malad Mindspace Turf", "Mindspace, Malad West", 19.1860, 72.8360));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Default display centered at Borivali Station
        filterAndDisplayTurfsWithin10Km(BORIVALI_LAT, BORIVALI_LNG);

        enableUserLocation();

        // Handle InfoWindow click to launch Google Maps Directions intent
        mMap.setOnInfoWindowClickListener(marker -> {
            LatLng position = marker.getPosition();
            String uri = "geo:" + position.latitude + "," + position.longitude + "?q=" + Uri.encode(marker.getTitle());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            try {
                startActivity(intent);
            } catch (Exception e) {
                // Fallback if Google Maps app is not installed
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
            }
        });
    }

    private void filterAndDisplayTurfsWithin10Km(double userLat, double userLng) {
        if (mMap == null) return;
        mMap.clear();

        LatLng userPoint = new LatLng(userLat, userLng);
        int count = 0;

        // Draw 10 km radius circle around user location
        mMap.addCircle(new CircleOptions()
                .center(userPoint)
                .radius(TEN_KM_IN_METERS)
                .strokeColor(Color.parseColor("#2E7D32"))
                .strokeWidth(3f)
                .fillColor(Color.parseColor("#112E7D32")));

        float[] distanceResults = new float[1];

        for (TurfItem turf : masterTurfList) {
            Location.distanceBetween(userLat, userLng, turf.lat, turf.lng, distanceResults);
            float distanceInMeters = distanceResults[0];

            if (distanceInMeters <= TEN_KM_IN_METERS) {
                count++;
                float distanceInKm = distanceInMeters / 1000f;
                String snippet = String.format(Locale.getDefault(), "%.1f km away • %s • Tap for Directions", distanceInKm, turf.address);

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(turf.lat, turf.lng))
                        .title(turf.name)
                        .snippet(snippet));
            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userPoint, 13f));
        Toast.makeText(this, String.format(Locale.getDefault(), "Found %d turfs within 10 km radius", count), Toast.LENGTH_SHORT).show();
    }

    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    filterAndDisplayTurfsWithin10Km(location.getLatitude(), location.getLongitude());
                }
            });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
