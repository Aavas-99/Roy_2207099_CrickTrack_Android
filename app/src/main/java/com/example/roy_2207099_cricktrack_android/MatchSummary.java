package com.example.roy_2207099_cricktrack_android;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class MatchSummary extends AppCompatActivity {

    private TextView lblVenue, lblDate, lblToss, lblDecision, lblResult, lblFirstInnings, lblSecondInnings;
    private RecyclerView rvFBat, rvFBow, rvSBat, rvSBow;
    private String matchId, firstInningsTeam, secondInningsTeam;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_summary);

        matchId = getIntent().getStringExtra("MATCH_ID");
        dbRef = FirebaseDatabase.getInstance().getReference("matches").child(matchId);

        initViews();
        loadMatchData();
    }

    private void initViews() {
        lblVenue = findViewById(R.id.lblVenue);
        lblDate = findViewById(R.id.lblDate);
        lblToss = findViewById(R.id.lblToss);
        lblDecision = findViewById(R.id.lblDecision);
        lblResult = findViewById(R.id.lblResult);
        lblFirstInnings = findViewById(R.id.lblFirstInnings);
        lblSecondInnings = findViewById(R.id.lblSecondInnings);

        rvFBat = findViewById(R.id.rvFirstBatsmen);
        rvFBow = findViewById(R.id.rvFirstBowlers);
        rvSBat = findViewById(R.id.rvSecondBatsmen);
        rvSBow = findViewById(R.id.rvSecondBowlers);

        rvFBat.setLayoutManager(new LinearLayoutManager(this));
        rvFBow.setLayoutManager(new LinearLayoutManager(this));
        rvSBat.setLayoutManager(new LinearLayoutManager(this));
        rvSBow.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadMatchData() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String teamA = snapshot.child("team_a").getValue(String.class);
                String teamB = snapshot.child("team_b").getValue(String.class);
                String toss = snapshot.child("toss_winner").getValue(String.class);
                String decision = snapshot.child("decision").getValue(String.class);

                if ("Bat".equalsIgnoreCase(decision)) {
                    firstInningsTeam = toss;
                    secondInningsTeam = toss.equals(teamA) ? teamB : teamA;
                } else {
                    firstInningsTeam = toss.equals(teamA) ? teamB : teamA;
                    secondInningsTeam = toss;
                }

                lblFirstInnings.setText("1st Innings - " + firstInningsTeam);
                lblSecondInnings.setText("2nd Innings - " + secondInningsTeam);
                lblVenue.setText(snapshot.child("stadium").getValue(String.class));
                lblDate.setText(snapshot.child("date").getValue(String.class));
                lblToss.setText(toss);
                lblDecision.setText(decision);
                lblResult.setText(snapshot.child("result").getValue(String.class));

                loadStats(snapshot);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadStats(DataSnapshot snapshot) {
        List<ScoreUpdate.Batsman> fBatList = new ArrayList<>();
        List<ScoreUpdate.Batsman> sBatList = new ArrayList<>();
        List<ScoreUpdate.Bowler> fBowList = new ArrayList<>();
        List<ScoreUpdate.Bowler> sBowList = new ArrayList<>();

        for (DataSnapshot ds : snapshot.child("batsman_stats").getChildren()) {
            MatchModels.BatsmanStat stat = ds.getValue(MatchModels.BatsmanStat.class);
            if (stat == null) continue;
            ScoreUpdate.Batsman b = new ScoreUpdate.Batsman(stat.name);
            b.setRuns(stat.runs);
            b.setBalls(stat.balls);
            b.setOut(stat.is_out == 1);
            if (stat.team.equals(firstInningsTeam)) fBatList.add(b);
            else sBatList.add(b);
        }

        for (DataSnapshot ds : snapshot.child("bowler_stats").getChildren()) {
            MatchModels.BowlerStat stat = ds.getValue(MatchModels.BowlerStat.class);
            if (stat == null) continue;
            ScoreUpdate.Bowler bow = new ScoreUpdate.Bowler(stat.name);
            bow.setRuns(stat.runs);
            bow.setBallsBowledDirect(stat.balls_bowled);
            bow.setWickets(stat.wickets);

            if (stat.team.equals(secondInningsTeam)) fBowList.add(bow);
            else sBowList.add(bow);
        }

        rvFBat.setAdapter(new BatsmanSummaryAdapter(fBatList));
        rvSBat.setAdapter(new BatsmanSummaryAdapter(sBatList));
        rvFBow.setAdapter(new BowlerSummaryAdapter(fBowList));
        rvSBow.setAdapter(new BowlerSummaryAdapter(sBowList));
    }
}