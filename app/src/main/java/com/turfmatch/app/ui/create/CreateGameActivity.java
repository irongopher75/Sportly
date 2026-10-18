package com.turfmatch.app.ui.create;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.turfmatch.app.R;
import com.turfmatch.app.model.GameModel;
import com.turfmatch.app.repository.FirebaseGameRepository;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

// CO1 - Match Creation UI with Hourly Rate, Duration, Max Players, Spinner, RadioGroup, Date/Time Pickers & Firebase Insert
public class CreateGameActivity extends AppCompatActivity {
    private EditText etMatchTitle, etTurfName, etHourlyRate, etDurationHours, etMaxPlayers;
    private Spinner spinnerSport;
    private RadioGroup rgLevel;
    private TextView tvDateTimeSelected;

    private String selectedDate = "";
    private String selectedTime = "";
    private Calendar selectedDateTime;
    private FirebaseGameRepository repository;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);

        repository = new FirebaseGameRepository();
        mAuth = FirebaseAuth.getInstance();

        etMatchTitle = findViewById(R.id.etMatchTitle);
        etTurfName = findViewById(R.id.etTurfName);
        etHourlyRate = findViewById(R.id.etHourlyRate);
        etDurationHours = findViewById(R.id.etDurationHours);
        etMaxPlayers = findViewById(R.id.etMaxPlayers);
        spinnerSport = findViewById(R.id.spinnerSport);
        rgLevel = findViewById(R.id.rgLevel);
        tvDateTimeSelected = findViewById(R.id.tvDateTimeSelected);
        Button btnPickDate = findViewById(R.id.btnPickDate);
        Button btnPickTime = findViewById(R.id.btnPickTime);
        Button btnSubmitGame = findViewById(R.id.btnSubmitGame);

        // Setup Spinner for Sports selection
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sports_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSport.setAdapter(adapter);

        btnPickDate.setOnClickListener(v -> showDatePickerDialog());
        btnPickTime.setOnClickListener(v -> showTimePickerDialog());

        btnSubmitGame.setOnClickListener(v -> publishGame());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year1, month1 + 1, dayOfMonth);
            if (selectedDateTime == null) selectedDateTime = Calendar.getInstance();
            selectedDateTime.set(year1, month1, dayOfMonth);
            updateDateTimeDisplay();
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
            if (selectedDateTime == null) selectedDateTime = Calendar.getInstance();
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDateTime.set(Calendar.MINUTE, minute1);
            updateDateTimeDisplay();
        }, hour, minute, true);
        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        tvDateTimeSelected.setText(String.format(Locale.getDefault(), "Date & Time: %s %s", selectedDate, selectedTime));
    }

    private void publishGame() {
        String title = etMatchTitle.getText().toString().trim();
        String turfName = etTurfName.getText().toString().trim();
        String sport = spinnerSport.getSelectedItem().toString();
        String rateStr = etHourlyRate.getText().toString().trim();
        String durationStr = etDurationHours.getText().toString().trim();
        String maxPlayersStr = etMaxPlayers.getText().toString().trim();

        if (title.isEmpty() || turfName.isEmpty() || rateStr.isEmpty() || durationStr.isEmpty() || maxPlayersStr.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and pick date & time", Toast.LENGTH_SHORT).show();
            return;
        }

        double hourlyRate;
        double durationHours;
        int maxPlayers;
        try {
            hourlyRate = Double.parseDouble(rateStr);
            durationHours = Double.parseDouble(durationStr);
            maxPlayers = Integer.parseInt(maxPlayersStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numeric values for rate, duration, and max players", Toast.LENGTH_SHORT).show();
            return;
        }

        if (maxPlayers <= 0) {
            Toast.makeText(this, "Max players must be a positive number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDateTime != null && selectedDateTime.before(Calendar.getInstance())) {
            Toast.makeText(this, "Match date & time can't be in the past", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = rgLevel.getCheckedRadioButtonId();
        String level = "Casual";
        if (selectedId == R.id.rbAmateur) {
            level = "Amateur";
        } else if (selectedId == R.id.rbCompetitive) {
            level = "Highly Competitive";
        }

        String hostId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "guest";

        GameModel game = new GameModel(null, title, sport, turfName, selectedDate, selectedTime, level, hostId,
                hourlyRate, durationHours, maxPlayers, new HashMap<>(), new HashMap<>());
        
        repository.createGame(game, success -> {
            if (success) {
                Toast.makeText(this, "Match published successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to publish match", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
