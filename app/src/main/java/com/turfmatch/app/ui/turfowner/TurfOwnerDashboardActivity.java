package com.turfmatch.app.ui.turfowner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.turfmatch.app.R;
import com.turfmatch.app.auth.LoginActivity;
import com.turfmatch.app.model.GameModel;
import com.turfmatch.app.model.PlayerInfo;
import com.turfmatch.app.repository.FirebaseGameRepository;

import java.util.List;
import java.util.Locale;
import java.util.Map;

// CO3 - Turf Owner Portal for Match Booking Confirmations & Player Fee Contribution Tracking
public class TurfOwnerDashboardActivity extends AppCompatActivity {
    private TextView tvTurfNameHeader;
    private LinearLayout layoutPendingBookings, layoutConfirmedBookings;
    private FirebaseGameRepository repository;
    private FirebaseAuth mAuth;
    private String ownedTurfName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turf_owner_dashboard);

        repository = new FirebaseGameRepository();
        mAuth = FirebaseAuth.getInstance();

        tvTurfNameHeader = findViewById(R.id.tvTurfNameHeader);
        layoutPendingBookings = findViewById(R.id.layoutPendingBookings);
        layoutConfirmedBookings = findViewById(R.id.layoutConfirmedBookings);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(TurfOwnerDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            repository.getUserProfile(currentUser.getUid(), profile -> {
                if (profile != null) {
                    ownedTurfName = profile.getOwnedTurfName();
                    tvTurfNameHeader.setText(String.format(Locale.getDefault(), "Managed Turf: %s",
                            ownedTurfName.isEmpty() ? "All Turfs (Owner Portal)" : ownedTurfName));
                    loadTurfBookings();
                }
            });
        }
    }

    private void loadTurfBookings() {
        repository.getTurfOwnerBookings(ownedTurfName, games -> {
            populatePendingBookings(games);
            populateConfirmedBookings(games);
        });
    }

    private void populatePendingBookings(List<GameModel> games) {
        layoutPendingBookings.removeAllViews();
        int pendingCount = 0;

        for (GameModel game : games) {
            if (!game.isTurfApproved()) {
                pendingCount++;
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(24, 24, 24, 24);
                card.setBackgroundColor(Color.WHITE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 16);
                card.setLayoutParams(params);

                TextView tvTitle = new TextView(this);
                tvTitle.setText(String.format(Locale.getDefault(), "%s (%s)", game.getTitle(), game.getSport()));
                tvTitle.setTextSize(16f);
                tvTitle.setTextColor(Color.BLACK);

                TextView tvDetails = new TextView(this);
                double totalCost = game.getHourlyRate() * game.getDurationHours();
                tvDetails.setText(String.format(Locale.getDefault(), "Turf: %s | Date: %s %s | Total Rental: $%.2f",
                        game.getTurfName(), game.getDate(), game.getTime(), totalCost));
                tvDetails.setTextSize(14f);
                tvDetails.setTextColor(Color.GRAY);

                LinearLayout btnLayout = new LinearLayout(this);
                btnLayout.setOrientation(LinearLayout.HORIZONTAL);
                btnLayout.setPadding(0, 12, 0, 0);

                Button btnConfirm = new Button(this);
                btnConfirm.setText("Confirm Booking");
                btnConfirm.setBackgroundColor(Color.parseColor("#2E7D32"));
                btnConfirm.setTextColor(Color.WHITE);
                btnConfirm.setOnClickListener(v -> {
                    String ownerUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
                    repository.confirmTurfBooking(game.getId(), ownerUid, success -> {
                        Toast.makeText(this, success ? "Booking confirmed!" : "Failed to confirm", Toast.LENGTH_SHORT).show();
                    });
                });

                Button btnDecline = new Button(this);
                btnDecline.setText("Decline");
                btnDecline.setBackgroundColor(Color.parseColor("#D32F2F"));
                btnDecline.setTextColor(Color.WHITE);
                btnDecline.setOnClickListener(v -> {
                    repository.rejectTurfBooking(game.getId(), success -> {
                        Toast.makeText(this, success ? "Booking declined" : "Failed to decline", Toast.LENGTH_SHORT).show();
                    });
                });

                btnLayout.addView(btnConfirm);
                btnLayout.addView(btnDecline);

                card.addView(tvTitle);
                card.addView(tvDetails);
                card.addView(btnLayout);

                layoutPendingBookings.addView(card);
            }
        }

        if (pendingCount == 0) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No pending match booking requests.");
            tvEmpty.setTextColor(Color.GRAY);
            layoutPendingBookings.addView(tvEmpty);
        }
    }

    private void populateConfirmedBookings(List<GameModel> games) {
        layoutConfirmedBookings.removeAllViews();
        int confirmedCount = 0;

        for (GameModel game : games) {
            if (game.isTurfApproved()) {
                confirmedCount++;
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(24, 24, 24, 24);
                card.setBackgroundColor(Color.WHITE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 16);
                card.setLayoutParams(params);

                TextView tvTitle = new TextView(this);
                tvTitle.setText(String.format(Locale.getDefault(), "✓ %s (%s)", game.getTitle(), game.getSport()));
                tvTitle.setTextSize(16f);
                tvTitle.setTextColor(Color.parseColor("#2E7D32"));

                TextView tvDetails = new TextView(this);
                Map<String, PlayerInfo> roster = game.getConfirmedRoster();
                int rosterSize = roster != null ? roster.size() : 0;
                double totalCost = game.getHourlyRate() * game.getDurationHours();
                double perPlayerCost = rosterSize > 0 ? totalCost / rosterSize : totalCost;

                tvDetails.setText(String.format(Locale.getDefault(), "Date: %s %s | Total: $%.2f | Per Player: $%.2f",
                        game.getDate(), game.getTime(), totalCost, perPlayerCost));
                tvDetails.setTextSize(14f);
                tvDetails.setTextColor(Color.BLACK);

                TextView tvRosterHeader = new TextView(this);
                tvRosterHeader.setText("Player Fee Contributions (Mark Paid/Unpaid):");
                tvRosterHeader.setTextSize(14f);
                tvRosterHeader.setPadding(0, 8, 0, 4);
                tvRosterHeader.setTextColor(Color.DKGRAY);

                card.addView(tvTitle);
                card.addView(tvDetails);
                card.addView(tvRosterHeader);

                if (roster == null || roster.isEmpty()) {
                    TextView tvNoPlayers = new TextView(this);
                    tvNoPlayers.setText("No confirmed players yet.");
                    tvNoPlayers.setTextColor(Color.GRAY);
                    card.addView(tvNoPlayers);
                } else {
                    for (Map.Entry<String, PlayerInfo> entry : roster.entrySet()) {
                        String userId = entry.getKey();
                        PlayerInfo player = entry.getValue();

                        LinearLayout playerRow = new LinearLayout(this);
                        playerRow.setOrientation(LinearLayout.HORIZONTAL);
                        playerRow.setPadding(0, 4, 0, 4);

                        TextView tvPlayerName = new TextView(this);
                        boolean isPaid = player != null && player.isPaid();
                        tvPlayerName.setText(String.format(Locale.getDefault(), "%s - %s",
                                player != null ? player.getName() : userId, isPaid ? "PAID ✓" : "UNPAID ✗"));
                        tvPlayerName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                        tvPlayerName.setTextColor(isPaid ? Color.parseColor("#2E7D32") : Color.parseColor("#D32F2F"));

                        CheckBox cbPaid = new CheckBox(this);
                        cbPaid.setText("Paid");
                        cbPaid.setChecked(isPaid);
                        cbPaid.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            repository.updateRosterPaymentStatus(game.getId(), userId, isChecked, success -> {
                                if (!success) {
                                    Toast.makeText(this, "Failed to update payment", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });

                        playerRow.addView(tvPlayerName);
                        playerRow.addView(cbPaid);
                        card.addView(playerRow);
                    }
                }

                layoutConfirmedBookings.addView(card);
            }
        }

        if (confirmedCount == 0) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No confirmed match bookings yet.");
            tvEmpty.setTextColor(Color.GRAY);
            layoutConfirmedBookings.addView(tvEmpty);
        }
    }
}
