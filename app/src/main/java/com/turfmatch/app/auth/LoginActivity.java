package com.turfmatch.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.turfmatch.app.R;
import com.turfmatch.app.repository.FirebaseGameRepository;
import com.turfmatch.app.ui.main.MainActivity;
import com.turfmatch.app.ui.turfowner.TurfOwnerDashboardActivity;

// CO3 - Firebase Authentication Login Activity with Dual Role Routing
public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;
    private FirebaseGameRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        repository = new FirebaseGameRepository();

        // Check if already logged in -> Route based on UserProfile role
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            routeUserToDashboard(currentUser.getUid());
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvToSignup = findViewById(R.id.tvToSignup);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                routeUserToDashboard(user.getUid());
                            }
                        } else {
                            Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        tvToSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    private void routeUserToDashboard(String uid) {
        repository.getUserProfile(uid, profile -> {
            if (profile != null && profile.isTurfOwner()) {
                Toast.makeText(this, "Welcome Turf Owner!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, TurfOwnerDashboardActivity.class));
            } else {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
            finish();
        });
    }
}
