package com.example.airqualitymonitor;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.airqualitymonitor.model.Area;
import com.example.airqualitymonitor.model.Reading;
import com.example.airqualitymonitor.sensor.Sensor;
import com.example.airqualitymonitor.service.AirQualityMonitor;
import com.example.airqualitymonitor.service.ReportService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.core.splashscreen.SplashScreen;

public class MainActivity extends AppCompatActivity {

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final AirQualityMonitor airQualityMonitor =
            new AirQualityMonitor();

    private final ReportService reportService =
            new ReportService();

    private TextView aqiValue, aqiStatus, healthAdvice, lastUpdated;
    private Spinner areaSpinner;

    private DatabaseReference databaseReference;

    private final Map<String, Integer> areaReadings =
            new HashMap<>();

    private final String[] areas = {
            "Kukatpally",
            "Ameerpet",
            "Gachibowli",
            "Miyapur",
            "Secunderabad"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        registerMonitoringAreas();

        aqiValue = findViewById(R.id.aqiValue);
        aqiStatus = findViewById(R.id.aqiStatus);
        healthAdvice = findViewById(R.id.healthAdvice);
        lastUpdated = findViewById(R.id.lastUpdated);
        areaSpinner = findViewById(R.id.areaSpinner);

        Button refreshButton = findViewById(R.id.refreshButton);
        Button historyButton = findViewById(R.id.historyButton);

        databaseReference = FirebaseDatabase
                .getInstance(
                        "https://airqualitymonitor-a8df9-default-rtdb.firebaseio.com/"
                )
                .getReference("airQualityReadings");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                areas
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        areaSpinner.setAdapter(adapter);

        areaSpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id) {

                        displayReading(areas[position]);
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                }
        );

        // Sign in anonymously before accessing Firebase.
        FirebaseAuth.getInstance()
                .signInAnonymously()
                .addOnSuccessListener(authResult -> {
                    loadReadings();

                    Toast.makeText(
                            MainActivity.this,
                            "Firebase connected successfully!",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(error ->
                        Toast.makeText(
                                MainActivity.this,
                                "Authentication failed: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );

        // Generate readings and save the report.
        refreshButton.setOnClickListener(v -> {

            String selectedArea =
                    areaSpinner.getSelectedItem().toString();

            refreshButton.setEnabled(false);

            executor.execute(() -> {

                // Generate readings using the sensor threads.
                airQualityMonitor.generateReadings();

                ArrayList<Reading> readings =
                        airQualityMonitor.getReadings();

                // Save the report in the app's private storage first.
                File reportFile = new File(
                        getFilesDir(),
                        "air_quality_report.txt"
                );

                boolean reportSaved = reportService.saveReportToFile(
                        readings,
                        reportFile.getAbsolutePath()
                );

                Log.d(
                        "AIR_REPORT",
                        "Report path: " + reportFile.getAbsolutePath()
                );

                Log.d(
                        "AIR_REPORT",
                        "Report saved: " + reportSaved
                );

                // Update the UI on the main thread.
                runOnUiThread(() -> {

                    refreshButton.setEnabled(true);

                    if (readings.isEmpty()) {
                        Toast.makeText(
                                MainActivity.this,
                                "No readings were generated.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    for (Reading reading : readings) {

                        String areaName = reading.getAreaName();
                        int aqi = reading.getAqi();

                        Map<String, Object> data = new HashMap<>();

                        data.put("aqi", aqi);

                        data.put(
                                "timestamp",
                                new SimpleDateFormat(
                                        "yyyy-MM-dd HH:mm:ss",
                                        Locale.getDefault()
                                ).format(new Date())
                        );

                        data.put("type", "simulated");

                        databaseReference.child(areaName)
                                .push()
                                .setValue(data)
                                .addOnSuccessListener(unused -> {

                                    areaReadings.put(areaName, aqi);

                                    if (areaName.equals(selectedArea)) {
                                        displayReading(areaName);
                                    }
                                })
                                .addOnFailureListener(error ->
                                        Toast.makeText(
                                                MainActivity.this,
                                                "Firebase save failed: "
                                                        + error.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show()
                                );
                    }

                    Toast.makeText(
                            MainActivity.this,
                            reportSaved
                                    ? "Readings generated! Report saved."
                                    : "Readings generated, but report saving failed.",
                            Toast.LENGTH_LONG
                    ).show();
                });
            });
        });

        // Display the selected area's reading history.
        historyButton.setOnClickListener(v -> {

            String selectedArea =
                    areaSpinner.getSelectedItem().toString();

            showReadingHistory(selectedArea);
        });
    }

    // Register areas and their sensors.
    private void registerMonitoringAreas() {

        for (String areaName : areas) {

            Sensor sensor = new Sensor(
                    "SENSOR_" + areaName.toUpperCase(Locale.ROOT),
                    areaName
            );

            Area area = new Area(areaName, sensor);

            airQualityMonitor.registerArea(area);
        }
    }

    // Load the latest saved reading for every area.
    private void loadReadings() {

        databaseReference.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        for (String area : areas) {

                            DataSnapshot areaSnapshot =
                                    snapshot.child(area);

                            Integer latestAqi = null;
                            String latestTimestamp = "";

                            for (DataSnapshot readingSnapshot
                                    : areaSnapshot.getChildren()) {

                                Integer aqi = readingSnapshot
                                        .child("aqi")
                                        .getValue(Integer.class);

                                String timestamp = readingSnapshot
                                        .child("timestamp")
                                        .getValue(String.class);

                                if (aqi != null) {
                                    latestAqi = aqi;
                                    latestTimestamp =
                                            timestamp != null
                                                    ? timestamp
                                                    : "";
                                }
                            }

                            if (latestAqi != null) {
                                areaReadings.put(area, latestAqi);
                            }
                        }

                        String selectedArea =
                                areaSpinner.getSelectedItem().toString();

                        displayReading(selectedArea);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                        Toast.makeText(
                                MainActivity.this,
                                "Could not load readings: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // Display AQI and health advice.
    private void displayReading(String area) {

        Integer aqi = areaReadings.get(area);

        if (aqi == null) {

            aqiValue.setText("--");
            aqiStatus.setText("No saved reading");

            healthAdvice.setText(
                    "Tap Generate New Reading to create a simulated reading."
            );

            lastUpdated.setText("Area: " + area);

            return;
        }

        aqiValue.setText(String.valueOf(aqi));

        String status;
        String advice;

        if (aqi <= 50) {

            status = "Good";
            advice = "Air quality is good. Enjoy your usual outdoor activities.";

        } else if (aqi <= 100) {

            status = "Moderate";
            advice = "Air quality is acceptable. Sensitive individuals should monitor symptoms.";

        } else if (aqi <= 200) {

            status = "Poor";
            advice = "Consider reducing prolonged outdoor exertion, especially if you are sensitive to air pollution.";

        } else {

            status = "Severe";
            advice = "Avoid strenuous outdoor activities and follow local health advisories.";
        }

        aqiStatus.setText(status);
        healthAdvice.setText(advice);

        lastUpdated.setText(
                "Area: " + area + " | Simulated reading"
        );
    }

    // Retrieve and display reading history.
    private void showReadingHistory(String area) {

        databaseReference.child(area)
                .orderByChild("timestamp")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot snapshot) {

                                StringBuilder history =
                                        new StringBuilder();

                                for (DataSnapshot readingSnapshot
                                        : snapshot.getChildren()) {

                                    Integer aqi = readingSnapshot
                                            .child("aqi")
                                            .getValue(Integer.class);

                                    String timestamp = readingSnapshot
                                            .child("timestamp")
                                            .getValue(String.class);

                                    if (aqi != null) {

                                        history.append("AQI: ")
                                                .append(aqi)
                                                .append("\n");

                                        history.append("Time: ")
                                                .append(
                                                        timestamp != null
                                                                ? timestamp
                                                                : "Unknown"
                                                )
                                                .append("\n\n");
                                    }
                                }

                                if (history.length() == 0) {
                                    history.append(
                                            "No saved readings for " + area
                                    );
                                }

                                new AlertDialog.Builder(
                                        MainActivity.this
                                )
                                        .setTitle(
                                                area + " - Reading History"
                                        )
                                        .setMessage(history.toString())
                                        .setPositiveButton("Close", null)
                                        .show();
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {

                                Toast.makeText(
                                        MainActivity.this,
                                        "Could not load history: "
                                                + error.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}