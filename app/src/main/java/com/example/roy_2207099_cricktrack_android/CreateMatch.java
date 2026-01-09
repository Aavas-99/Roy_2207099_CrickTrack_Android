package com.example.roy_2207099_cricktrack_android;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class CreateMatch extends AppCompatActivity {
    private EditText txtTeamA, txtTeamB, txtOvers, txtStadium, datepicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_match);

        txtTeamA = findViewById(R.id.txtTeamA);
        txtTeamB = findViewById(R.id.txtTeamB);
        txtOvers = findViewById(R.id.txtOvers);
        txtStadium = findViewById(R.id.txtStadium);
        datepicker = findViewById(R.id.datepicker);

        datepicker.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) ->
                    datepicker.setText(year + "-" + (month + 1) + "-" + day),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        findViewById(R.id.btnStart).setOnClickListener(v -> onStartMatch());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void onStartMatch() {
        String teamA = txtTeamA.getText().toString().trim();
        String teamB = txtTeamB.getText().toString().trim();
        String oversText = txtOvers.getText().toString().trim();
        String stadium = txtStadium.getText().toString().trim();
        String date = datepicker.getText().toString().trim();

        if (teamA.isEmpty() || teamB.isEmpty() || oversText.isEmpty() || stadium.isEmpty() || date.isEmpty()) {
            new AlertDialog.Builder(this).setMessage("All fields are required!").show();
            return;
        }

        Intent intent = new Intent(this, Toss.class);
        intent.putExtra("TEAM_A", teamA);
        intent.putExtra("TEAM_B", teamB);
        intent.putExtra("OVERS", Integer.parseInt(oversText));
        intent.putExtra("STADIUM", stadium);
        intent.putExtra("DATE", date);
        startActivity(intent);
    }
}