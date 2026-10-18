package com.turfmatch.app.ui.detail;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.turfmatch.app.R;
import com.turfmatch.app.model.GameModel;
import com.turfmatch.app.model.PlayerInfo;
import com.turfmatch.app.repository.FirebaseGameRepository;

import java.util.Locale;
import java.util.Map;

// CO3 - Game Detail Activity handling Cost Split, Host Approval/Rejection, Payment Toggles & Deletion
public class GameDetailActivity extends AppCompatActivity {
    private String gameId;
    private FirebaseGameRepository repository;
    private FirebaseAuth mAuth;

    private TextView tvTitle, tvSport, tvTurf, tvDateTime, tvLevel, tvCostSplit;
    private Button btnJoinGame, btnDeleteGame;
    private LinearLayout layoutHostSection, layoutPendingRequests, layoutConfirmedRoster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);

        repository = new FirebaseGameRepository();
        mAuth = FirebaseAuth.getInstance();

        gameId = getIntent().getStringExtra("gameId");

        tvTitle = findViewById(R.id.tvTitle);
        tvSport = findViewById(R.id.tvSport);
        tvTurf = findViewById(R.id.tvTurf);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvLevel = findViewById(R.id.tvLevel);
        tvCostSplit = findViewById(R.id.tvCostSplit);
        btnJoinGame = findViewById(R.id.btnJoinGame);
        btnDeleteGame = findViewById(R.id.btnDeleteGame);
        layoutHostSection = findViewById(R.id.layoutHostSection);
        layoutPendingRequests = findViewById(R.id.layoutPendingRequests);
        layoutConfirmedRoster = findViewById(R.id.layoutConfirmedRoster);

        if (gameId != null) {
            loadGameDetailsRealtime();
        }

        btnJoinGame.setOnClickListener(v -> requestJoin());

        btnDeleteGame.setOnClickListener(v -> deleteMatch());
    }

    private void loadGameDetailsRealtime() {
        FirebaseDatabase.getInstance().getReference().child("games").child(gameId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GameModel game = snapshot.getValue(GameModel.class);
                        if (game != null) {
                            // Opportunistic expiration check
                            repository.archiveIfExpired(game, success -> {});

                            tvTitle.setText(game.getTitle());
                            tvSport.setText(String.format(Locale.getDefault(), "Sport: %s", game.getSport()));
                            tvTurf.setText(String.format(Locale.getDefault(), "Turf: %s", game.getTurfName()));
                            tvDateTime.setText(String.format(Locale.getDefault(), "Date & Time: %s %s", game.getDate(), game.getTime()));
                            tvLevel.setText(String.format(Locale.getDefault(), "Competitive Level: %s", game.getCompetitiveLevel()));

                            // Compute Cost Split
                            Map<String, PlayerInfo> confirmed = game.getConfirmedRoster();
                            int confirmedCount = (confirmed != null) ? confirmed.size() : 0;
                            double totalCost = game.getHourlyRate() * game.getDurationHours();
                            double perPlayerCost = confirmedCount > 0 ? totalCost / confirmedCount : totalCost;

                            tvCostSplit.setText(String.format(Locale.getDefault(), 
                                    "Total Cost: $%.2f | Per Player: $%.2f (Confirmed: %d/%d)", 
                                    totalCost, perPlayerCost, confirmedCount, game.getMaxPlayers()));

                            String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

                            // Check host view
                            if (currentUserId.equals(game.getHostId())) {
                                layoutHostSection.setVisibility(View.VISIBLE);
                                btnJoinGame.setVisibility(View.GONE);
                                populateHostManagement(game);
                            } else {
                                layoutHostSection.setVisibility(View.GONE);
                                btnJoinGame.setVisibility(View.VISIBLE);

                                // Check join status
                                if (confirmed != null && confirmed.containsKey(currentUserId)) {
                                    btnJoinGame.setText("Joined (Confirmed ✓)");
                                    btnJoinGame.setEnabled(false);
                                } else if (game.getPendingRequests() != null && game.getPendingRequests().containsKey(currentUserId)) {
                                    btnJoinGame.setText("Join Request Pending...");
                                    btnJoinGame.setEnabled(false);
                                } else {
                                    btnJoinGame.setText("Request to Join Match");
                                    btnJoinGame.setEnabled(true);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(GameDetailActivity.this, "Failed to load game details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void requestJoin() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        String userName = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "Player";
        if (userId.isEmpty()) {
            Toast.makeText(this, "Please log in to join matches", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.requestJoinGame(gameId, userId, userName, success -> {
            if (success) {
                Toast.makeText(this, "Join request sent to host!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to send join request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateHostManagement(GameModel game) {
        // Pending Requests
        layoutPendingRequests.removeAllViews();
        Map<String, PlayerInfo> pending = game.getPendingRequests();
        if (pending == null || pending.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No pending join requests.");
            tvEmpty.setTextColor(Color.GRAY);
            layoutPendingRequests.addView(tvEmpty);
        } else {
            for (Map.Entry<String, PlayerInfo> entry : pending.entrySet()) {
                String userId = entry.getKey();
                PlayerInfo player = entry.getValue();

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, 8, 0, 8);

                TextView tvName = new TextView(this);
                tvName.setText(player != null ? player.getName() : userId);
                tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                tvName.setTextColor(Color.BLACK);

                Button btnAccept = new Button(this);
                btnAccept.setText("Accept");
                btnAccept.setTextSize(12f);
                btnAccept.setBackgroundColor(Color.parseColor("#2E7D32"));
                btnAccept.setTextColor(Color.WHITE);
                btnAccept.setOnClickListener(v -> {
                    repository.acceptJoinRequest(gameId, userId, (success, msg) -> {
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    });
                });

                Button btnReject = new Button(this);
                btnReject.setText("Reject");
                btnReject.setTextSize(12f);
                btnReject.setBackgroundColor(Color.parseColor("#D32F2F"));
                btnReject.setTextColor(Color.WHITE);
                btnReject.setOnClickListener(v -> {
                    repository.rejectJoinRequest(gameId, userId, success -> {
                        Toast.makeText(this, success ? "Request rejected" : "Failed to reject", Toast.LENGTH_SHORT).show();
                    });
                });

                row.addView(tvName);
                row.addView(btnAccept);
                row.addView(btnReject);
                layoutPendingRequests.addView(row);
            }
        }

        // Confirmed Roster & Payment Toggles
        layoutConfirmedRoster.removeAllViews();
        Map<String, PlayerInfo> confirmed = game.getConfirmedRoster();
        if (confirmed == null || confirmed.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No confirmed players yet.");
            tvEmpty.setTextColor(Color.GRAY);
            layoutConfirmedRoster.addView(tvEmpty);
        } else {
            for (Map.Entry<String, PlayerInfo> entry : confirmed.entrySet()) {
                String userId = entry.getKey();
                PlayerInfo player = entry.getValue();

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, 8, 0, 8);

                TextView tvName = new TextView(this);
                boolean isPaid = player != null && player.isPaid();
                String displayName = (player != null ? player.getName() : userId) + (isPaid ? " (Paid ✓)" : " (Unpaid ✗)");
                tvName.setText(displayName);
                tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                tvName.setTextColor(isPaid ? Color.parseColor("#2E7D32") : Color.parseColor("#D32F2F"));

                CheckBox cbPaid = new CheckBox(this);
                cbPaid.setText("Paid");
                cbPaid.setChecked(isPaid);
                cbPaid.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    repository.updateRosterPaymentStatus(gameId, userId, isChecked, success -> {
                        if (!success) {
                            Toast.makeText(this, "Failed to update payment status", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

                row.addView(tvName);
                row.addView(cbPaid);
                layoutConfirmedRoster.addView(row);
            }
        }
    }

    private void deleteMatch() {
        repository.deleteGame(gameId, success -> {
            if (success) {
                Toast.makeText(this, "Match cancelled successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to delete match", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
