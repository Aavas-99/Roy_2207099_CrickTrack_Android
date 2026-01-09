package com.example.roy_2207099_cricktrack_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Toss extends AppCompatActivity {
    private LinearLayout teamAList, teamBList;
    private TextView lblteamA, lblteamB;
    private RadioButton rbTeamA, rbTeamB, rbBat;
    private ArrayList<EditText> fieldsA = new ArrayList<>(), fieldsB = new ArrayList<>();
    private String tA, tB, stadium, date;
    private int overs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toss);

        teamAList = findViewById(R.id.teamAList); teamBList = findViewById(R.id.teamBList);
        lblteamA = findViewById(R.id.lblteamA); lblteamB = findViewById(R.id.lblteamB);
        rbTeamA = findViewById(R.id.rbTeamA); rbTeamB = findViewById(R.id.rbTeamB);
        rbBat = findViewById(R.id.rbBat);

        tA = getIntent().getStringExtra("TEAM_A");
        tB = getIntent().getStringExtra("TEAM_B");
        overs = getIntent().getIntExtra("OVERS", 20);
        stadium = getIntent().getStringExtra("STADIUM");
        date = getIntent().getStringExtra("DATE");

        lblteamA.setText(tA); lblteamB.setText(tB);
        rbTeamA.setText(tA); rbTeamB.setText(tB);

        for (int i = 1; i <= 6; i++) {
            EditText etA = new EditText(this); etA.setHint("Player " + i); teamAList.addView(etA); fieldsA.add(etA);
            EditText etB = new EditText(this); etB.setHint("Player " + i); teamBList.addView(etB); fieldsB.add(etB);
        }

        findViewById(R.id.btnContinue).setOnClickListener(v -> onContinue());
    }

    private void onContinue() {
        RadioGroup rgToss = findViewById(R.id.rgToss);
        RadioGroup rgDecision = findViewById(R.id.rgDecision);

        if (rgToss.getCheckedRadioButtonId() == -1 || rgDecision.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select Toss and Decision!", Toast.LENGTH_SHORT).show();
            return;
        }

        MatchModels.MatchData match = new MatchModels.MatchData();
        match.team_a = tA;
        match.team_b = tB;
        match.overs = overs;
        match.stadium = stadium;
        match.date = date;
        match.toss_winner = rbTeamA.isChecked() ? tA : tB;
        match.decision = rbBat.isChecked() ? "Bat" : "Bowl";
        match.result = null;

        match.players_a = new ArrayList<>();
        for(EditText e : fieldsA) {
            match.players_a.add(initPlayerStats(e.getText().toString().trim()));
        }

        match.players_b = new ArrayList<>();
        for(EditText e : fieldsB) {
            match.players_b.add(initPlayerStats(e.getText().toString().trim()));
        }

        match.totalRuns = 0;
        match.wickets = 0;
        match.currentOvers = "0.0";

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("matches").push();
        String uniqueMatchId = ref.getKey();

        ref.setValue(match).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(Toss.this, "Match Created!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Toss.this, ScoreUpdate.class);
                intent.putExtra("MATCH_ID", uniqueMatchId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(Toss.this, "Error: " + (task.getException() != null ? task.getException().getMessage() : "unknown"), Toast.LENGTH_LONG).show();
            }
        });
    }

    private MatchModels.PlayerStats initPlayerStats(String name) {
        if(name == null || name.isEmpty()) name = "Unknown Player";

        MatchModels.PlayerStats p = new MatchModels.PlayerStats();
        p.playerName = name;
        p.runs = 0;
        p.ballsPlayed = 0;
        p.isOut = false;
        p.runsGiven = 0;
        p.bowls = 0;
        p.wickets = 0;
        return p;
    }
}