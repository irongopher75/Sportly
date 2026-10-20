package com.turfmatch.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.turfmatch.app.R;
import com.turfmatch.app.model.UserProfile;
import com.turfmatch.app.repository.FirebaseGameRepository;
import com.turfmatch.app.ui.main.MainActivity;
import com.turfmatch.app.ui.turfowner.TurfOwnerDashboardActivity;

// CO3 - Firebase Authentication Signup Activity supporting Player vs Turf Owner Dual Roles
public class SignupActivity extends AppCompatActivity {
    private EditText etName, etEmail, etPassword, etOwnedTurfName;
    private RadioGroup rgRole;
    private FirebaseAuth mAuth;
    private FirebaseGameRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        repository = new FirebaseGameRepository();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etOwnedTurfName = findViewById(R.id.etOwnedTurfName);
        rgRole = findViewById(R.id.rgRole);
        Button btnSignup = findViewById(R.id.btnSignup);
        TextView tvToLogin = findViewById(R.id.tvToLogin);

        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbTurfOwner) {
                etOwnedTurfName.setVisibility(View.VISIBLE);
            } else {
                etOwnedTurfName.setVisibility(View.GONE);
            }
        });

        btnSignup.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            boolean isTurfOwner = rgRole.getCheckedRadioButtonId() == R.id.rbTurfOwner;
            String userType = isTurfOwner ? "TURF_OWNER" : "PLAYER";
            String ownedTurfName = isTurfOwner ? etOwnedTurfName.getText().toString().trim() : "";

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isTurfOwner && ownedTurfName.isEmpty()) {
                Toast.makeText(this, "Please enter your Turf Name", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                UserProfile profile = new UserProfile(firebaseUser.getUid(), name, email, "", userType, ownedTurfName);
                                repository.createUserProfile(profile, success -> {
                                    if (success) {
                                        Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                                        if (isTurfOwner) {
                                            startActivity(new Intent(SignupActivity.this, TurfOwnerDashboardActivity.class));
                                        } else {
                                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                        }
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Failed to save user profile", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(this, "Signup Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        tvToLogin.setOnClickListener(v -> finish());
    }
}
