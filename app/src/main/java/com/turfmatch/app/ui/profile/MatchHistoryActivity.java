package com.turfmatch.app.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.turfmatch.app.R;
import com.turfmatch.app.model.GameModel;
import com.turfmatch.app.repository.FirebaseGameRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// CO2 & CO5 - Real Firebase-backed Match History, displayed + exported as CSV/TXT
public class MatchHistoryActivity extends AppCompatActivity {
    private ListView listViewHistory;
    private TextView tvEmptyState;
    private FirebaseGameRepository repository;
    private List<GameModel> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_history);

        listViewHistory = findViewById(R.id.listViewHistory);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        Button btnExportCsv = findViewById(R.id.btnExportCsv);
        Button btnExportTxt = findViewById(R.id.btnExportTxt);

        repository = new FirebaseGameRepository();

        btnExportCsv.setOnClickListener(v -> exportHistory("match_history.csv", true));
        btnExportTxt.setOnClickListener(v -> exportHistory("booking_receipts.txt", false));

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            showEmptyState("Log in to see your match history.");
            return;
        }

        repository.getUserMatchHistory(userId, games -> {
            historyList = games;
            if (historyList.isEmpty()) {
                showEmptyState("You haven't joined any matches yet.");
            } else {
                tvEmptyState.setVisibility(View.GONE);
                listViewHistory.setVisibility(View.VISIBLE);
                List<String> rows = new ArrayList<>();
                for (GameModel g : historyList) {
                    rows.add(g.getDate() + " at " + g.getTime() + "  •  " + g.getSport() + "  •  "
                            + g.getTurfName() + "  •  " + g.getCompetitiveLevel() + "  •  " + statusFor(g));
                }
                listViewHistory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows));
            }
        });
    }

    private void showEmptyState(String message) {
        tvEmptyState.setText(message);
        tvEmptyState.setVisibility(View.VISIBLE);
        listViewHistory.setVisibility(View.GONE);
    }

    private String statusFor(GameModel game) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            Date matchDateTime = sdf.parse(game.getDate() + " " + game.getTime());
            return matchDateTime != null && matchDateTime.before(new Date()) ? "Completed" : "Upcoming";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private void exportHistory(String fileName, boolean csv) {
        if (historyList.isEmpty()) {
            Toast.makeText(this, "No match history to export yet", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder content = new StringBuilder();
        if (csv) {
            content.append("Date,Sport,Turf Name,Competitive Level,Status\n");
            for (GameModel g : historyList) {
                content.append(String.format("%s,%s,%s,%s,%s\n",
                        g.getDate(), g.getSport(), g.getTurfName(), g.getCompetitiveLevel(), statusFor(g)));
            }
        } else {
            content.append("=== TURFMATCH BOOKING RECEIPTS ===\n\n");
            int i = 1;
            for (GameModel g : historyList) {
                content.append(String.format("%d. Date: %s | Sport: %s | Turf: %s | Level: %s | Status: %s\n",
                        i++, g.getDate(), g.getSport(), g.getTurfName(), g.getCompetitiveLevel(), statusFor(g)));
            }
        }

        exportFile(fileName, content.toString());
    }

    private void exportFile(String fileName, String content) {
        try {
            File exportDir = new File(getExternalFilesDir(null), "TurfMatchExports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            File file = new File(exportDir, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();

            Toast.makeText(this, "Exported successfully to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
