package com.turfmatch.app.repository;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.turfmatch.app.model.GameModel;
import com.turfmatch.app.model.PlayerInfo;
import com.turfmatch.app.model.UserProfile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// CO3 - Firebase DB Repository Helper Class with Cost Split, Host Approval & Archival support
public class FirebaseGameRepository {
    private final DatabaseReference databaseRef;

    public FirebaseGameRepository() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    // CREATE: Insert User Profile
    public void createUserProfile(UserProfile userProfile, OnCompleteCallback callback) {
        databaseRef.child("users").child(userProfile.getUid()).setValue(userProfile)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    // CREATE: Insert Game
    public void createGame(GameModel game, OnCompleteCallback callback) {
        String gameId = databaseRef.child("games").push().getKey();
        if (gameId != null) {
            game.setId(gameId);
            databaseRef.child("games").child(gameId).setValue(game)
                    .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
        } else {
            callback.onComplete(false);
        }
    }

    // READ: Load Active (Non-archived) Games Feed & Check Expiration
    public void getGames(OnGamesLoadedCallback callback) {
        databaseRef.child("games").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<GameModel> games = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    GameModel game = ds.getValue(GameModel.class);
                    if (game != null && !game.isArchived()) {
                        // Opportunistic expiration check
                        archiveIfExpired(game, success -> {});
                        games.add(game);
                    }
                }
                callback.onGamesLoaded(games);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onGamesLoaded(new ArrayList<>());
            }
        });
    }

    // REQUEST JOIN: Add user to pendingRequests
    public void requestJoinGame(String gameId, String userId, String playerName, OnCompleteCallback callback) {
        DatabaseReference gameRef = databaseRef.child("games").child(gameId);
        gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GameModel game = snapshot.getValue(GameModel.class);
                if (game != null) {
                    Map<String, PlayerInfo> confirmed = game.getConfirmedRoster();
                    Map<String, PlayerInfo> pending = game.getPendingRequests();

                    if (confirmed != null && confirmed.containsKey(userId)) {
                        callback.onComplete(true); // Already confirmed
                        return;
                    }
                    if (pending != null && pending.containsKey(userId)) {
                        callback.onComplete(true); // Already requested
                        return;
                    }

                    PlayerInfo playerInfo = new PlayerInfo(userId, playerName != null ? playerName : "Player", false);
                    databaseRef.child("games").child(gameId).child("pendingRequests").child(userId).setValue(playerInfo)
                            .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
                } else {
                    callback.onComplete(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onComplete(false);
            }
        });
    }

    // ACCEPT JOIN REQUEST: Move user from pendingRequests to confirmedRoster (if under maxPlayers)
    public void acceptJoinRequest(String gameId, String userId, OnAcceptResultCallback callback) {
        DatabaseReference gameRef = databaseRef.child("games").child(gameId);
        gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GameModel game = snapshot.getValue(GameModel.class);
                if (game != null) {
                    Map<String, PlayerInfo> confirmed = game.getConfirmedRoster();
                    if (confirmed == null) confirmed = new HashMap<>();

                    if (confirmed.size() >= game.getMaxPlayers()) {
                        callback.onResult(false, "Game is full!");
                        return;
                    }

                    Map<String, PlayerInfo> pending = game.getPendingRequests();
                    PlayerInfo player = (pending != null) ? pending.get(userId) : null;
                    if (player == null) {
                        player = new PlayerInfo(userId, "Player", false);
                    }

                    confirmed.put(userId, player);
                    if (pending != null) {
                        pending.remove(userId);
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("confirmedRoster", confirmed);
                    updates.put("pendingRequests", pending);

                    gameRef.updateChildren(updates).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onResult(true, "Request accepted");
                        } else {
                            callback.onResult(false, "Failed to accept request");
                        }
                    });
                } else {
                    callback.onResult(false, "Game not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResult(false, error.getMessage());
            }
        });
    }

    // REJECT JOIN REQUEST: Remove user from pendingRequests
    public void rejectJoinRequest(String gameId, String userId, OnCompleteCallback callback) {
        databaseRef.child("games").child(gameId).child("pendingRequests").child(userId).removeValue()
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    // UPDATE ROSTER PAYMENT STATUS: Partial update for paid boolean
    public void updateRosterPaymentStatus(String gameId, String userId, boolean paid, OnCompleteCallback callback) {
        databaseRef.child("games").child(gameId).child("confirmedRoster").child(userId).child("paid").setValue(paid)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    // ARCHIVE IF EXPIRED: Check date/time against current device time, archive and write to matchHistory
    public void archiveIfExpired(GameModel game, OnCompleteCallback callback) {
        if (game == null || game.isArchived() || game.getId() == null) {
            if (callback != null) callback.onComplete(false);
            return;
        }

        try {
            String dateTimeStr = game.getDate() + " " + game.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date gameDateTime = sdf.parse(dateTimeStr);

            if (gameDateTime != null && new Date().after(gameDateTime)) {
                // Expired! Mark live game as archived
                databaseRef.child("games").child(game.getId()).child("archived").setValue(true);

                // Compute final cost per player
                Map<String, PlayerInfo> roster = game.getConfirmedRoster();
                int rosterSize = (roster != null && !roster.isEmpty()) ? roster.size() : 1;
                double totalCost = game.getHourlyRate() * game.getDurationHours();
                double perPlayerCost = totalCost / rosterSize;

                if (roster != null) {
                    for (Map.Entry<String, PlayerInfo> entry : roster.entrySet()) {
                        String userId = entry.getKey();
                        PlayerInfo player = entry.getValue();

                        Map<String, Object> historyRecord = new HashMap<>();
                        historyRecord.put("gameId", game.getId());
                        historyRecord.put("title", game.getTitle());
                        historyRecord.put("turfName", game.getTurfName());
                        historyRecord.put("sport", game.getSport());
                        historyRecord.put("date", game.getDate());
                        historyRecord.put("time", game.getTime());
                        historyRecord.put("competitiveLevel", game.getCompetitiveLevel());
                        historyRecord.put("perPlayerCost", perPlayerCost);
                        historyRecord.put("paid", player != null && player.isPaid());

                        databaseRef.child("matchHistory").child(userId).child(game.getId()).setValue(historyRecord);
                    }
                }
                if (callback != null) callback.onComplete(true);
                return;
            }
        } catch (Exception e) {
            // Parsing error fallback
        }
        if (callback != null) callback.onComplete(false);
    }

    // DELETE: Delete/Cancel Hosted Game
    public void deleteGame(String gameId, OnCompleteCallback callback) {
        databaseRef.child("games").child(gameId).removeValue()
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    // READ: Load User Match History from confirmedRoster or matchHistory node
    public void getUserMatchHistory(String userId, OnGamesLoadedCallback callback) {
        databaseRef.child("games").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<GameModel> history = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    GameModel game = ds.getValue(GameModel.class);
                    if (game != null && game.getConfirmedRoster() != null && game.getConfirmedRoster().containsKey(userId)) {
                        history.add(game);
                    }
                }
                Collections.sort(history, (a, b) -> {
                    String dateA = a.getDate() != null ? a.getDate() : "";
                    String dateB = b.getDate() != null ? b.getDate() : "";
                    return dateB.compareTo(dateA);
                });
                callback.onGamesLoaded(history);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onGamesLoaded(new ArrayList<>());
            }
        });
    }

    // READ: Load User Profile
    public void getUserProfile(String uid, OnUserProfileLoadedCallback callback) {
        databaseRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onUserProfileLoaded(snapshot.getValue(UserProfile.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onUserProfileLoaded(null);
            }
        });
    }

    // UPDATE: Partial update of profile fields
    public void updateUserProfile(String uid, Map<String, Object> updates, OnCompleteCallback callback) {
        databaseRef.child("users").child(uid).updateChildren(updates)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    // Callbacks
    public interface OnCompleteCallback {
        void onComplete(boolean success);
    }

    public interface OnAcceptResultCallback {
        void onResult(boolean success, String message);
    }

    public interface OnGamesLoadedCallback {
        void onGamesLoaded(List<GameModel> games);
    }

    public interface OnUserProfileLoadedCallback {
        void onUserProfileLoaded(UserProfile profile);
    }
}
