package com.turfmatch.app.ui.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.turfmatch.app.R;
import com.turfmatch.app.auth.LoginActivity;
import com.turfmatch.app.repository.FirebaseGameRepository;

import java.util.HashMap;
import java.util.Map;

// CO2, CO3 & CO5 - Profile Activity with Camera Implicit Intent, Firebase Read & Update
public class ProfileActivity extends AppCompatActivity {
    private ImageView ivProfile;
    private TextView tvUserName, tvUserEmail, tvUserBio;
    private LinearLayout editProfileContainer;
    private EditText etEditName, etEditBio;
    private Button btnEditProfile;
    private Bitmap profileBitmap;
    private FirebaseGameRepository repository;
    private String uid;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String KEY_PROFILE_BITMAP = "profileBitmap";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        repository = new FirebaseGameRepository();

        ivProfile = findViewById(R.id.ivProfile);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserBio = findViewById(R.id.tvUserBio);
        editProfileContainer = findViewById(R.id.editProfileContainer);
        etEditName = findViewById(R.id.etEditName);
        etEditBio = findViewById(R.id.etEditBio);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        Button btnSaveProfile = findViewById(R.id.btnSaveProfile);
        Button btnCancelEdit = findViewById(R.id.btnCancelEdit);
        Button btnCapturePhoto = findViewById(R.id.btnCapturePhoto);
        Button btnViewHistory = findViewById(R.id.btnViewHistory);
        Button btnLogout = findViewById(R.id.btnLogout);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
            tvUserEmail.setText(user.getEmail());
        }

        loadProfile();

        if (savedInstanceState != null) {
            profileBitmap = savedInstanceState.getParcelable(KEY_PROFILE_BITMAP);
            if (profileBitmap != null) {
                ivProfile.setImageBitmap(profileBitmap);
            }
        }

        btnCapturePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            }
        });

        btnEditProfile.setOnClickListener(v -> {
            etEditName.setText(tvUserName.getText());
            etEditBio.setText(tvUserBio.getText().toString().equals("No bio yet.") ? "" : tvUserBio.getText());
            editProfileContainer.setVisibility(View.VISIBLE);
            btnEditProfile.setVisibility(View.GONE);
        });

        btnCancelEdit.setOnClickListener(v -> {
            editProfileContainer.setVisibility(View.GONE);
            btnEditProfile.setVisibility(View.VISIBLE);
        });

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        btnViewHistory.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, MatchHistoryActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadProfile() {
        if (uid == null) return;
        repository.getUserProfile(uid, profile -> {
            if (profile != null && profile.getName() != null) {
                tvUserName.setText(profile.getName());
            } else {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                tvUserName.setText(user != null && user.getDisplayName() != null
                        ? user.getDisplayName() : "TurfMatch Player");
            }
            String bio = profile != null ? profile.getBio() : null;
            tvUserBio.setText(bio != null && !bio.isEmpty() ? bio : "No bio yet.");
        });
    }

    private void saveProfile() {
        String name = etEditName.getText().toString().trim();
        String bio = etEditBio.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Name can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (uid == null) {
            Toast.makeText(this, "You must be logged in to update your profile", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("bio", bio);

        repository.updateUserProfile(uid, updates, success -> {
            if (success) {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                editProfileContainer.setVisibility(View.GONE);
                btnEditProfile.setVisibility(View.VISIBLE);
                loadProfile();
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            // Fallback launch even if resolveActivity is strict in Android 11+
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                profileBitmap = (Bitmap) data.getExtras().get("data");
                ivProfile.setImageBitmap(profileBitmap);
                Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (profileBitmap != null) {
            outState.putParcelable(KEY_PROFILE_BITMAP, (Parcelable) profileBitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission required to take photo", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
