package com.turfmatch.app.ui.main;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.turfmatch.app.R;
import com.turfmatch.app.receiver.NetworkReceiver;
import com.turfmatch.app.ui.create.CreateGameActivity;
import com.turfmatch.app.ui.home.HomeFragment;
import com.turfmatch.app.ui.map.TurfMapActivity;
import com.turfmatch.app.ui.profile.ProfileActivity;

// Main Activity coordinating navigation between Feed, Create Game, Map, and Profile
public class MainActivity extends AppCompatActivity {
    private final NetworkReceiver networkReceiver = new NetworkReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new HomeFragment())
                    .commit();
        }

        Button btnCreateGame = findViewById(R.id.btnCreateGame);
        Button btnMap = findViewById(R.id.btnMap);
        Button btnProfile = findViewById(R.id.btnProfile);

        btnCreateGame.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CreateGameActivity.class));
        });

        btnMap.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TurfMapActivity.class));
        });

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }
}
