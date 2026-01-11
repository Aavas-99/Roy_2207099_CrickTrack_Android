package com.example.roy_2207099_cricktrack_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ScoreUpdate extends AppCompatActivity {

    private String matchId;
    private DatabaseReference dbRef;
    private String battingTeam;
    private String bowlingTeam;
    private ArrayList<String> battingPlayers = new ArrayList<>();
    private ArrayList<String> bowlingPlayers = new ArrayList<>();
    private int oversLimit = 20;
    private String stadium;
    private String date;
    private String tossWinner;
    private String decision;
    private String firstInningsBattingTeam = null;
    private int firstInningsTotal = -1;
    private boolean firstInnings = true;
    private int currentOver = 0;
    private int currentBall = 0;
    private int wickets = 0;
    private int totalRuns = 0;
    private boolean isFreeHit = false;
    private int strikerIndex = 0;
    private int nonStrikerIndex = 1;
    private int currentBowlerIndex = 5;

    private ArrayList<Batsman> batsmenStats = new ArrayList<>();
    private ArrayList<Bowler> bowlerStats = new ArrayList<>();
    private ArrayList<BallEvent> ballHistory = new ArrayList<>();
    private TextView etInningsInfo, etScore, etOvers;
    private TextView etStriker, etStrikerRuns, etStrikerBalls;
    private TextView etNonStriker, etNonStrikerRuns, etNonStrikerBalls;
    private TextView etBowler, etBowlerOvers, etBowlerMaiden, etBowlerRuns, etBowlerWickets;
    private Button cbWide, cbNoBall, cbByes, cbLegByes, cbWicket, btnUndo;
    private boolean wideSelected = false, noBallSelected = false, byesSelected = false, legByesSelected = false, wicketSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_update);

        matchId = getIntent().getStringExtra("MATCH_ID");
        if (matchId == null) {
            Toast.makeText(this, "Match ID missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbRef = FirebaseDatabase.getInstance().getReference("matches").child(matchId);

        initViews();
        setupUtilityButtons();
        setupRunButtons();
        btnUndo.setOnClickListener(v -> undoLastBall());

        loadMatchDataFromFirebase();
    }

    private void initViews() {
        etInningsInfo = findViewById(R.id.etInningsInfo);
        etScore = findViewById(R.id.etScore);
        etOvers = findViewById(R.id.etOvers);
        etStriker = findViewById(R.id.etStriker);
        etStrikerRuns = findViewById(R.id.etStrikerRuns);
        etStrikerBalls = findViewById(R.id.etStrikerBalls);
        etNonStriker = findViewById(R.id.etNonStriker);
        etNonStrikerRuns = findViewById(R.id.etNonStrikerRuns);
        etNonStrikerBalls = findViewById(R.id.etNonStrikerBalls);
        etBowler = findViewById(R.id.etBowler);
        etBowlerOvers = findViewById(R.id.etBowlerOvers);
        etBowlerMaiden = findViewById(R.id.etBowlerMaiden);
        etBowlerRuns = findViewById(R.id.etBowlerRuns);
        etBowlerWickets = findViewById(R.id.etBowlerWickets);
        cbWide = findViewById(R.id.cbWide);
        cbNoBall = findViewById(R.id.cbNoBall);
        cbByes = findViewById(R.id.cbByes);
        cbLegByes = findViewById(R.id.cbLegByes);
        cbWicket = findViewById(R.id.cbWicket);
        btnUndo = findViewById(R.id.btnUndo);

        etScore.setText("0 - 0");
        etOvers.setText("(0.0)");
    }

    private void setupUtilityButtons() {
        cbWide.setOnClickListener(v -> { wideSelected = !wideSelected; updateToggleVisual(cbWide, wideSelected); });
        cbNoBall.setOnClickListener(v -> { noBallSelected = !noBallSelected; updateToggleVisual(cbNoBall, noBallSelected); });
        cbByes.setOnClickListener(v -> {
            byesSelected = !byesSelected; updateToggleVisual(cbByes, byesSelected);
            if (byesSelected) {
                askRunsWithNumberPicker("Byes", value -> {
                    handleBall(value, false, false, true, false);
                    byesSelected = false; updateToggleVisual(cbByes, false);
                });
            }
        });
        cbLegByes.setOnClickListener(v -> {
            legByesSelected = !legByesSelected; updateToggleVisual(cbLegByes, legByesSelected);
            if (legByesSelected) {
                askRunsWithNumberPicker("Leg Byes", value -> {
                    handleBall(value, false, false, true, false);
                    legByesSelected = false; updateToggleVisual(cbLegByes, false);
                });
            }
        });
        cbWicket.setOnClickListener(v -> { wicketSelected = !wicketSelected; updateToggleVisual(cbWicket, wicketSelected); });
    }

    private void setupRunButtons() {
        int[] runButtons = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6};
        for (int i = 0; i < runButtons.length; i++) {
            final int runs = i;
            View v = findViewById(runButtons[i]);
            if (v != null) v.setOnClickListener(view -> {
                if (wideSelected) {
                    handleBall(runs + 1, true, false, false, false);
                    wideSelected = false; updateToggleVisual(cbWide, false);
                } else if (noBallSelected) {
                    handleBall(runs + 1, true, false, false, true);
                    noBallSelected = false; updateToggleVisual(cbNoBall, false);
                    isFreeHit = true;
                } else {
                    handleBall(runs, false, wicketSelected, false, false);
                    wicketSelected = false; updateToggleVisual(cbWicket, false);
                }
            });
        }
    }

    private void loadMatchDataFromFirebase() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String teamA = snapshot.child("team_a").getValue(String.class);
                String teamB = snapshot.child("team_b").getValue(String.class);
                tossWinner = snapshot.child("toss_winner").getValue(String.class);
                decision = snapshot.child("decision").getValue(String.class);

                if ("Bat".equalsIgnoreCase(decision)) {
                    battingTeam = tossWinner;
                    bowlingTeam = tossWinner.equals(teamA) ? teamB : teamA;
                } else {
                    battingTeam = tossWinner.equals(teamA) ? teamB : teamA;
                    bowlingTeam = tossWinner;
                }

                etInningsInfo.setText(battingTeam + ", 1st Innings");
                oversLimit = snapshot.child("overs").getValue(Integer.class) != null ? snapshot.child("overs").getValue(Integer.class) : 20;
                stadium = snapshot.child("stadium").getValue(String.class);
                date = snapshot.child("date").getValue(String.class);

                battingPlayers.clear(); bowlingPlayers.clear();
                String batPath = battingTeam.equals(teamA) ? "players_a" : "players_b";
                String bowlPath = bowlingTeam.equals(teamA) ? "players_a" : "players_b";

                for (DataSnapshot child : snapshot.child(batPath).getChildren()) {
                    String p = child.child("playerName").getValue(String.class);
                    if (p != null) battingPlayers.add(p);
                }
                for (DataSnapshot child : snapshot.child(bowlPath).getChildren()) {
                    String p = child.child("playerName").getValue(String.class);
                    if (p != null) bowlingPlayers.add(p);
                }

                batsmenStats.clear(); bowlerStats.clear();
                for (String p : battingPlayers) batsmenStats.add(new Batsman(p));
                for (String p : bowlingPlayers) bowlerStats.add(new Bowler(p));

                updateLabels();
                saveStatsToFirebase();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateLabels() {
        if (strikerIndex != -1 && strikerIndex < batsmenStats.size()) {
            Batsman s = batsmenStats.get(strikerIndex);
            etStriker.setText(s.getName());
            etStrikerRuns.setText(String.valueOf(s.getRuns()));
            etStrikerBalls.setText(String.valueOf(s.getBalls()));
        }
        if (nonStrikerIndex != -1 && nonStrikerIndex < batsmenStats.size()) {
            Batsman ns = batsmenStats.get(nonStrikerIndex);
            etNonStriker.setText(ns.getName());
            etNonStrikerRuns.setText(String.valueOf(ns.getRuns()));
            etNonStrikerBalls.setText(String.valueOf(ns.getBalls()));
        }
        if (currentBowlerIndex != -1 && currentBowlerIndex < bowlerStats.size()) {
            Bowler b = bowlerStats.get(currentBowlerIndex);
            etBowler.setText(b.getName());
            etBowlerRuns.setText(String.valueOf(b.getRuns()));
            etBowlerWickets.setText(String.valueOf(b.getWickets()));
            etBowlerOvers.setText(b.getOvers() + "." + (b.getBallsBowled() % 6));
        }
        etScore.setText(String.format(Locale.US, "%d - %d", totalRuns, wickets));
        etOvers.setText(String.format(Locale.US, "(%d.%d)", currentOver, currentBall));
    }

    private void handleBall(int runs, boolean isExtra, boolean isWicket, boolean isblb, boolean isNo) {
        if (strikerIndex == -1) return;

        boolean actualWicket = isWicket && !isFreeHit;
        ballHistory.add(new BallEvent(strikerIndex, nonStrikerIndex, currentBowlerIndex, runs, isExtra, isWicket, currentOver, currentBall, wickets, totalRuns, isblb, isNo));

        if (isblb || !isExtra) {
            Batsman striker = batsmenStats.get(strikerIndex);
            if (!isblb) striker.setRuns(striker.getRuns() + runs);
            striker.setBalls(striker.getBalls() + 1);

            Bowler bowler = bowlerStats.get(currentBowlerIndex);
            bowler.setRuns(bowler.getRuns() + runs);
            bowler.incrementBallsBowled();
            totalRuns += runs;
            isFreeHit = false;
        } else {
            totalRuns += runs;
            bowlerStats.get(currentBowlerIndex).setRuns(bowlerStats.get(currentBowlerIndex).getRuns() + runs);
            if (isNo) isFreeHit = true;
        }

        if (actualWicket) {
            wickets++;
            bowlerStats.get(currentBowlerIndex).setWickets(bowlerStats.get(currentBowlerIndex).getWickets() + 1);
            batsmenStats.get(strikerIndex).setOut(true);
            strikerIndex = getNextBatsman();
            if (strikerIndex == -1) { endInnings(); return; }
        }

        if (!isExtra) {
            currentBall++;
            if (currentBall == 6) {
                currentOver++; currentBall = 0;
                swapStriker();
                currentBowlerIndex = getNextBowler();
            }
            if (runs == 1 || runs == 3 || runs == 5) swapStriker();
        }

        if (currentOver == oversLimit) { endInnings(); return; }
        if (!firstInnings && totalRuns > firstInningsTotal) { endInnings(); return; }

        updateLabels();
        saveStatsToFirebase();
    }

    private void endInnings() {
        saveStatsToFirebase();
        if (firstInnings) {
            firstInningsTotal = totalRuns;
            firstInningsBattingTeam = battingTeam;
            dbRef.child("first_innings_total").setValue(firstInningsTotal);

            firstInnings = false;
            String temp = battingTeam; battingTeam = bowlingTeam; bowlingTeam = temp;
            ArrayList<String> tempPlayers = new ArrayList<>(battingPlayers);
            battingPlayers = new ArrayList<>(bowlingPlayers);
            bowlingPlayers = tempPlayers;

            totalRuns = 0; wickets = 0; currentOver = 0; currentBall = 0;
            batsmenStats.clear(); bowlerStats.clear();
            for (String p : battingPlayers) batsmenStats.add(new Batsman(p));
            for (String p : bowlingPlayers) bowlerStats.add(new Bowler(p));

            strikerIndex = 0; nonStrikerIndex = 1; currentBowlerIndex = 5;
            etInningsInfo.setText(battingTeam + ", 2nd Innings");
            updateLabels();
            Toast.makeText(this, "Second Innings Started", Toast.LENGTH_SHORT).show();
        } else {
            String result;
            if (totalRuns > firstInningsTotal) result = battingTeam + " won by " + (battingPlayers.size() - wickets) + " wickets";
            else if (totalRuns < firstInningsTotal) result = firstInningsBattingTeam + " won by " + (firstInningsTotal - totalRuns) + " runs";
            else result = "Match Tied";

            dbRef.child("result").setValue(result);
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void saveStatsToFirebase() {
        if (dbRef == null) return;
        Map<String, Object> updates = new HashMap<>();

        for (Batsman b : batsmenStats) {
            String key = battingTeam + "_" + b.getName();
            Map<String, Object> stat = new HashMap<>();
            stat.put("name", b.getName());
            stat.put("runs", b.getRuns());
            stat.put("balls", b.getBalls());
            stat.put("is_out", b.isOut() ? 1 : 0);
            stat.put("team", battingTeam);
            updates.put("batsman_stats/" + key, stat);
        }

        for (Bowler b : bowlerStats) {
            String key = bowlingTeam + "_" + b.getName();
            Map<String, Object> stat = new HashMap<>();
            stat.put("name", b.getName());
            stat.put("balls_bowled", b.getBallsBowled());
            stat.put("runs", b.getRuns());
            stat.put("wickets", b.getWickets());
            stat.put("team", bowlingTeam);
            updates.put("bowler_stats/" + key, stat);
        }

        updates.put("score", totalRuns + " - " + wickets);
        updates.put("overs_played", currentOver + "." + currentBall);
        dbRef.updateChildren(updates);
    }

    private void swapStriker() {
        int tmp = strikerIndex; strikerIndex = nonStrikerIndex; nonStrikerIndex = tmp;
    }

    private int getNextBatsman() {
        for (int i = 0; i < batsmenStats.size(); i++) {
            if (!batsmenStats.get(i).isOut() && i != strikerIndex && i != nonStrikerIndex) return i;
        }
        return -1;
    }

    private int getNextBowler() {
        if(currentBowlerIndex==3 ) return 5;
        return (currentBowlerIndex - 1) % bowlerStats.size();
    }

    private void undoLastBall() {
        if (ballHistory.isEmpty()) {
            Toast.makeText(this, "No balls to undo", Toast.LENGTH_SHORT).show();
            return;
        }

        BallEvent last = ballHistory.remove(ballHistory.size() - 1);
        strikerIndex = last.strikerIndex;
        nonStrikerIndex = last.nonStrikerIndex;
        currentBowlerIndex = last.bowlerIndex;
        currentOver = last.over;
        currentBall = last.ball;
        wickets = last.wickets;
        totalRuns = last.runsTotal;
        isFreeHit = false;

        for (Batsman b : batsmenStats) {
            b.setRuns(0);
            b.setBalls(0);
            b.setOut(false);
        }
        for (Bowler b : bowlerStats) {
            b.setRuns(0);
            b.setBallsBowled(0);
            b.setWickets(0);
        }
        for (BallEvent ev : ballHistory) {
            if (ev.blb || !ev.isExtra) {
                Batsman s = batsmenStats.get(ev.strikerIndex);
                if (!ev.blb) s.setRuns(s.getRuns() + ev.runs);
                s.setBalls(s.getBalls() + 1);

                Bowler bow = bowlerStats.get(ev.bowlerIndex);
                bow.setRuns(bow.getRuns() + ev.runs);
                bow.incrementBallsBowled();
                isFreeHit = false;
            } else {
                Bowler bow = bowlerStats.get(ev.bowlerIndex);
                bow.setRuns(bow.getRuns() + ev.runs);
                if (ev.no) isFreeHit = true;
            }

            if (ev.isWicket && !isFreeHit) {
                Bowler bow = bowlerStats.get(ev.bowlerIndex);
                bow.setWickets(bow.getWickets() + 1);
                batsmenStats.get(ev.strikerIndex).setOut(true);
            }
        }
        updateLabels();
        saveStatsToFirebase();
    }

    private void updateToggleVisual(Button btn, boolean sel) { btn.setAlpha(sel ? 0.5f : 1.0f); }

    private void askRunsWithNumberPicker(String title, final NumberPickedCallback cb) {
        NumberPicker np = new NumberPicker(this);
        np.setMinValue(0); np.setMaxValue(6);
        new AlertDialog.Builder(this).setTitle(title).setView(np)
                .setPositiveButton("OK", (d, w) -> cb.onPicked(np.getValue())).show();
    }

    private interface NumberPickedCallback { void onPicked(int value); }
    public static class Batsman {
        private String name; private int runs, balls; private boolean out;
        public Batsman(String n) { this.name = n; }
        public String getName() { return name; }
        public int getRuns() { return runs; } public void setRuns(int r) { runs = r; }
        public int getBalls() { return balls; } public void setBalls(int b) { balls = b; }
        public boolean isOut() { return out; } public void setOut(boolean o) { out = o; }
    }

    public static class Bowler {
        private String name; private int ballsBowled, runs, wickets;
        public Bowler(String n) { this.name = n; }
        public String getName() { return name; }
        public int getBallsBowled() { return ballsBowled; } public void setBallsBowled(int b) { ballsBowled = b; }
        public void incrementBallsBowled() { ballsBowled++; }
        public int getRuns() { return runs; } public void setRuns(int r) { runs = r; }
        public int getWickets() { return wickets; } public void setWickets(int w) { wickets = w; }
        public int getOvers() { return ballsBowled / 6; }
        public void setBallsBowledDirect(int b) { ballsBowled = b; }
    }

    private static class BallEvent {
        int strikerIndex, nonStrikerIndex, bowlerIndex, runs, over, ball, wickets, runsTotal;
        boolean isExtra, isWicket, blb, no;
        BallEvent(int s, int ns, int b, int r, boolean ex, boolean w, int ov, int bl, int wk, int rt, boolean blb, boolean no) {
            this.strikerIndex = s; this.nonStrikerIndex = ns; this.bowlerIndex = b;
            this.runs = r; this.isExtra = ex; this.isWicket = w; this.over = ov;
            this.ball = bl; this.wickets = wk; this.runsTotal = rt; this.blb = blb; this.no = no;
        }
    }
}