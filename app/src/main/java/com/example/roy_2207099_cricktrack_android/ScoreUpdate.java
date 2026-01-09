package com.example.roy_2207099_cricktrack_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
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
    private int currentBowlerIndex = 0;

    private ArrayList<Batsman> batsmenStats = new ArrayList<>();
    private ArrayList<Bowler> bowlerStats = new ArrayList<>();
    private ArrayList<BallEvent> ballHistory = new ArrayList<>();
    private EditText etInningsInfo, etScore, etOvers;
    private EditText etStriker, etStrikerRuns, etStrikerBalls;
    private EditText etNonStriker, etNonStrikerRuns, etNonStrikerBalls;
    private EditText etBowler, etBowlerOvers, etBowlerMaiden, etBowlerRuns, etBowlerWickets;
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
        cbWide.setOnClickListener(v -> {
            wideSelected = !wideSelected; updateToggleVisual(cbWide, wideSelected);
        });
        cbNoBall.setOnClickListener(v -> {
            noBallSelected = !noBallSelected; updateToggleVisual(cbNoBall, noBallSelected);
        });
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
        cbWicket.setOnClickListener(v -> {
            wicketSelected = !wicketSelected; updateToggleVisual(cbWicket, wicketSelected);
        });
    }

    private void setupRunButtons() {
        int[] runButtons = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6};
        for (int i = 0; i < runButtons.length; i++) {
            final int runs = i;
            View v = findViewById(runButtons[i]);
            if (v != null) v.setOnClickListener(view -> {
                boolean isWide = wideSelected;
                boolean isNo = noBallSelected;
                boolean isWicket = wicketSelected;
                if (isWide) {
                    handleBall(runs + 1, true, false, false, false);
                    wideSelected = false; updateToggleVisual(cbWide, false);
                } else if (isNo) {
                    handleBall(runs+1, true, false, false, true);
                    noBallSelected = false; updateToggleVisual(cbNoBall, false);
                    isFreeHit = true;
                } else {
                    handleBall(runs, false, isWicket, false, false);
                    wicketSelected = false; updateToggleVisual(cbWicket, false);
                }
            });
        }
    }

    private void loadMatchDataFromFirebase() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(ScoreUpdate.this, "Match data not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                battingTeam = snapshot.child("toss_winner").getValue(String.class);
                assert battingTeam != null;
                if(battingTeam.equals(snapshot.child("team_a").getValue(String.class))){
                    bowlingTeam = snapshot.child("team_b").getValue(String.class);
                }
                else{
                    bowlingTeam = snapshot.child("team_a").getValue(String.class);
                }
                etInningsInfo.setText(battingTeam + ", 1st inning");
                Integer ov = snapshot.child("overs").getValue(Integer.class);
                if (ov != null) oversLimit = ov;
                stadium = snapshot.child("stadium").getValue(String.class);
                date = snapshot.child("date").getValue(String.class);
                tossWinner = snapshot.child("toss_winner").getValue(String.class);
                decision = snapshot.child("decision").getValue(String.class);

                Object fitObj = snapshot.child("first_innings_total").getValue();
                if (fitObj != null) {
                    try {
                        firstInningsTotal = Integer.parseInt(fitObj.toString());
                        if (!firstInnings) updateTargetUI();
                    } catch (Exception ignored) {}
                }

                battingPlayers.clear(); bowlingPlayers.clear();
                for (DataSnapshot child : snapshot.child("players_a").getChildren()) {
                    String p = child.child("playerName").getValue(String.class);
                    if (p != null && !p.isEmpty()) battingPlayers.add(p);
                }
                for (DataSnapshot child : snapshot.child("players_b").getChildren()) {
                    String p = child.child("playerName").getValue(String.class);
                    if (p != null && !p.isEmpty()) bowlingPlayers.add(p);
                }

                if (battingPlayers.isEmpty()) {
                    battingPlayers.add("Batsman 1");
                    battingPlayers.add("Batsman 2");
                    battingPlayers.add("Batsman 3");
                    battingPlayers.add("Batsman 4");
                    battingPlayers.add("Batsman 5");
                    battingPlayers.add("Batsman 6");
                }
                if (bowlingPlayers.isEmpty()) {
                    bowlingPlayers.addAll(battingPlayers);
                }

                batsmenStats.clear(); bowlerStats.clear();
                for (String p : battingPlayers) batsmenStats.add(new Batsman(p));
                for (String p : bowlingPlayers) bowlerStats.add(new Bowler(p));

                strikerIndex = 0; nonStrikerIndex = 1; currentBowlerIndex = 0;

                updateLabels();
                loadSavedStatsFromFirebase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ScoreUpdate.this, "Error loading match: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSavedStatsFromFirebase() {
        dbRef.child("batsman_stats").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot b : snapshot.getChildren()) {
                    String name = b.getKey();
                    if (name == null) continue;
                    MatchModels.BatsmanStat stat = b.getValue(MatchModels.BatsmanStat.class);
                    if (stat != null) {
                        for (Batsman bs : batsmenStats) {
                            if (bs.getName().equals(stat.name)) {
                                bs.setRuns(stat.runs);
                                bs.setBalls(stat.balls);
                                bs.setOut(stat.is_out == 1);
                                break;
                            }
                        }
                    }
                }
                dbRef.child("bowler_stats").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot2) {
                        for (DataSnapshot b2 : snapshot2.getChildren()) {
                            String name = b2.getKey(); if (name == null) continue;
                            MatchModels.BowlerStat stat = b2.getValue(MatchModels.BowlerStat.class);
                            if (stat != null) {
                                for (Bowler bw : bowlerStats) {
                                    if (bw.getName().equals(stat.name)) {
                                        bw.setBallsBowled(stat.balls_bowled);
                                        bw.setRuns(stat.runs);
                                        bw.setWickets(stat.wickets);
                                        break;
                                    }
                                }
                            }
                        }
                        updateLabels();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void updateLabels() {
        if (strikerIndex >= 0 && strikerIndex < batsmenStats.size()) {
            etStriker.setText(batsmenStats.get(strikerIndex).getName());
            etStrikerRuns.setText(String.valueOf(batsmenStats.get(strikerIndex).getRuns()));
            etStrikerBalls.setText(String.valueOf(batsmenStats.get(strikerIndex).getBalls()));
        } else {
            etStriker.setText("-");
            etStrikerRuns.setText("0");
            etStrikerBalls.setText("0");
        }
        if (nonStrikerIndex >= 0 && nonStrikerIndex < batsmenStats.size()) {
            etNonStriker.setText(batsmenStats.get(nonStrikerIndex).getName());
            etNonStrikerRuns.setText(String.valueOf(batsmenStats.get(nonStrikerIndex).getRuns()));
            etNonStrikerBalls.setText(String.valueOf(batsmenStats.get(nonStrikerIndex).getBalls()));
        } else {
            etNonStriker.setText("-");
            etNonStrikerRuns.setText("0");
            etNonStrikerBalls.setText("0");
        }

        if (currentBowlerIndex >= 0 && currentBowlerIndex < bowlerStats.size()) {
            etBowler.setText(bowlerStats.get(currentBowlerIndex).getName());
            etBowlerRuns.setText(String.valueOf(bowlerStats.get(currentBowlerIndex).getRuns()));
            etBowlerWickets.setText(String.valueOf(bowlerStats.get(currentBowlerIndex).getWickets()));
            etBowlerOvers.setText(bowlerStats.get(currentBowlerIndex).getOvers() + ".0");
        }

        etScore.setText(String.format(Locale.US, "%d - %d", totalRuns, wickets));
        etOvers.setText(String.format(Locale.US, "(%d.%d)", currentOver, currentBall));
    }

    private void updateTargetUI() {

    }

    private void handleBall(int runs, boolean isExtra, boolean isWicket, boolean isblb, boolean isNo) {
        if (strikerIndex == -1) {
            saveStatsToFirebase();
            Toast.makeText(this, "Innings Over: All batsmen are out", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean actualWicket = isWicket;
        if (isFreeHit && isWicket) {
            Toast.makeText(this, "Free Hit: wicket not allowed", Toast.LENGTH_SHORT).show();
            actualWicket = false;
        }

        BallEvent event = new BallEvent(strikerIndex, nonStrikerIndex, currentBowlerIndex, runs, isExtra, isWicket, currentOver, currentBall, wickets, totalRuns, isblb, isNo);
        ballHistory.add(event);

        if (isblb) {
            Batsman striker = batsmenStats.get(strikerIndex);
            striker.setBalls(striker.getBalls() + 1);
            totalRuns += runs;
            Bowler bowler = bowlerStats.get(currentBowlerIndex);
            bowler.setRuns(bowler.getRuns() + runs);
            bowler.incrementBallsBowled();
            isFreeHit = false;
        } else if (!isExtra) {
            Batsman striker = batsmenStats.get(strikerIndex);
            striker.setRuns(striker.getRuns() + runs);
            striker.setBalls(striker.getBalls() + 1);
            totalRuns += runs;
            Bowler bowler = bowlerStats.get(currentBowlerIndex);
            bowler.setRuns(bowler.getRuns() + runs);
            bowler.incrementBallsBowled();
            isFreeHit = false;
        } else {
            totalRuns += runs;
            if (isNo) isFreeHit = true;
            bowlerStats.get(currentBowlerIndex).setRuns(bowlerStats.get(currentBowlerIndex).getRuns() + runs);
        }

        if (!firstInnings && firstInningsTotal != -1 && totalRuns > firstInningsTotal) {
            int remainingWickets = Math.max(0, battingPlayers.size() - wickets);
            String resultText = battingTeam + " won by " + remainingWickets + " wickets";
            saveStatsToFirebase();
            dbRef.child("result").setValue(resultText);
            Toast.makeText(this, "Match Over: " + resultText, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (actualWicket) {
            wickets++;
            Bowler bw = bowlerStats.get(currentBowlerIndex);
            bw.setWickets(bw.getWickets() + 1);
            batsmenStats.get(strikerIndex).setOut(true);
            strikerIndex = getNextBatsman();
            if (strikerIndex == -1) { endInnings(); return; }
        }

        if (!isExtra) currentBall++;
        if (currentBall == 6) {
            currentOver++; currentBall = 0; swapStriker(); currentBowlerIndex = getNextBowler();
        }
        if (!isExtra && (runs == 1 || runs == 3)) swapStriker();
        if (currentOver == oversLimit) { endInnings(); return; }

        updateLabels();
        saveStatsToFirebase();
    }

    private void swapStriker() {
        int tmp = strikerIndex; strikerIndex = nonStrikerIndex; nonStrikerIndex = tmp; updateLabels();
    }

    private int getNextBatsman() {
        for (int i = 0; i < batsmenStats.size(); i++) {
            if (!batsmenStats.get(i).isOut() && i != strikerIndex && i != nonStrikerIndex) return i;
        }
        return -1;
    }

    private int getNextBowler() {
        currentBowlerIndex++;
        if (currentBowlerIndex >= bowlerStats.size()) currentBowlerIndex = 0;
        return currentBowlerIndex;
    }

    private void endInnings() {
        saveStatsToFirebase();
        Toast.makeText(this, "Innings Over for " + battingTeam, Toast.LENGTH_SHORT).show();
        if (firstInnings) {
            firstInningsTotal = totalRuns;
            firstInningsBattingTeam = battingTeam;
            dbRef.child("first_innings_total").setValue(firstInningsTotal);

            firstInnings = false;
            String tmpTeam = battingTeam; battingTeam = bowlingTeam; bowlingTeam = tmpTeam;
            etInningsInfo.setText(battingTeam+ ", 1st Innings");
            ArrayList<String> tmpPlayers = battingPlayers; battingPlayers = bowlingPlayers; bowlingPlayers = tmpPlayers;

            currentOver = 0; currentBall = 0; wickets = 0; totalRuns = 0;
            batsmenStats.clear(); bowlerStats.clear();
            for (String p : battingPlayers) batsmenStats.add(new Batsman(p));
            for (String p : bowlingPlayers) bowlerStats.add(new Bowler(p));
            strikerIndex = 0; nonStrikerIndex = 1; currentBowlerIndex = 0;
            updateLabels();
        } else {
            String resultText;
            etInningsInfo.setText(battingTeam+ ", 2nd Innings");
            if (firstInningsTotal == -1) resultText = "Result not available";
            else {
                if (totalRuns > firstInningsTotal) {
                    int remainingWickets = Math.max(0, battingPlayers.size() - wickets);
                    resultText = battingTeam + " won by " + remainingWickets + " wickets";
                } else if (totalRuns == firstInningsTotal) resultText = "Match tied";
                else resultText = firstInningsBattingTeam + " won by " + (firstInningsTotal - totalRuns) + " runs";
            }
            dbRef.child("result").setValue(resultText);
            saveStatsToFirebase();
            Toast.makeText(this, "Match Over: " + resultText, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, AdminDashboard1.class);
            startActivity(intent);
            finish();
        }
    }

    private void undoLastBall() {
        if (ballHistory.isEmpty()) return;
        BallEvent last = ballHistory.remove(ballHistory.size() - 1);
        strikerIndex = last.strikerIndex; nonStrikerIndex = last.nonStrikerIndex; currentBowlerIndex = last.bowlerIndex;
        currentOver = last.over; currentBall = last.ball; wickets = last.wickets; totalRuns = last.runsTotal;
        isFreeHit = false;

        for (Batsman b : batsmenStats) b.reset();
        for (Bowler b : bowlerStats) b.reset();

        for (BallEvent ev : ballHistory) {
            if (ev.blb) {
                Batsman s = batsmenStats.get(ev.strikerIndex);
                s.setBalls(s.getBalls() + 1);
                Bowler bow = bowlerStats.get(ev.bowlerIndex);
                bow.setRuns(bow.getRuns() + ev.runs);
                bow.incrementBallsBowled();
                isFreeHit = false;
            } else if (!ev.isExtra) {
                Batsman s = batsmenStats.get(ev.strikerIndex);
                s.setRuns(s.getRuns() + ev.runs);
                s.setBalls(s.getBalls() + 1);
                Bowler bow = bowlerStats.get(ev.bowlerIndex);
                bow.setRuns(bow.getRuns() + ev.runs);
                bow.incrementBallsBowled();
                isFreeHit = false;
            } else {
                if (ev.no) isFreeHit = true;
                Bowler bow = bowlerStats.get(ev.bowlerIndex);
                bow.setRuns(bow.getRuns() + ev.runs);
            }
            if (ev.isWicket) {
                Bowler bow = bowlerStats.get(ev.bowlerIndex);
                bow.setWickets(bow.getWickets() + 1);
                batsmenStats.get(ev.strikerIndex).setOut(true);
            }
        }

        updateLabels();
        saveStatsToFirebase();
    }

    private void saveStatsToFirebase() {
        if (dbRef == null) return;
        Map<String, MatchModels.BatsmanStat> batsMap = new HashMap<>();
        for (Batsman b : batsmenStats) {
            MatchModels.BatsmanStat stat = new MatchModels.BatsmanStat();
            stat.name = b.getName();
            stat.runs = b.getRuns();
            stat.balls = b.getBalls();
            stat.is_out = b.isOut() ? 1 : 0;
            stat.team = battingTeam;
            batsMap.put(stat.name, stat);
        }
        dbRef.child("batsman_stats").setValue(batsMap);

        Map<String, MatchModels.BowlerStat> bowlMap = new HashMap<>();
        for (Bowler b : bowlerStats) {
            MatchModels.BowlerStat stat = new MatchModels.BowlerStat();
            stat.name = b.getName();
            stat.balls_bowled = b.getBallsBowled();
            stat.runs = b.getRuns();
            stat.wickets = b.getWickets();
            stat.team = bowlingTeam;
            bowlMap.put(stat.name, stat);
        }
        dbRef.child("bowler_stats").setValue(bowlMap);
        dbRef.child("score").setValue(totalRuns + " - " + wickets);
        dbRef.child("overs_played").setValue(currentOver + "." + currentBall);
    }

    private void askRunsWithNumberPicker(String title, final NumberPickedCallback cb) {
        NumberPicker np = new NumberPicker(this);
        np.setMinValue(0); np.setMaxValue(6); np.setValue(1);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setView(np);
        builder.setPositiveButton("OK", (dialog, which) -> {
            cb.onPicked(np.getValue());
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateToggleVisual(Button btn, boolean sel) {
        btn.setAlpha(sel ? 0.6f : 1.0f);
    }

    private interface NumberPickedCallback { void onPicked(int value); }
    public static class Batsman {
        private final String name;
        private int runs = 0;
        private int balls = 0;
        private boolean out = false;
        public Batsman(String name) { this.name = name; }
        public String getName() { return name; }
        public int getRuns() { return runs; }
        public void setRuns(int r) { runs = r; }
        public int getBalls() { return balls; }
        public void setBalls(int b) { balls = b; }
        public boolean isOut() { return out; }
        public void setOut(boolean o) { out = o; }
        public void reset() { runs = 0; balls = 0; out = false; }
    }

    public static class Bowler {
        private final String name;
        private int ballsBowled = 0;
        private int runs = 0;
        private int wickets = 0;
        public Bowler(String name) { this.name = name; }
        public String getName() { return name; }
        public int getOvers() { return ballsBowled / 6; }
        public int getRuns() { return runs; }
        public void setRuns(int r) { runs = r; }
        public int getWickets() { return wickets; }
        public void setWickets(int w) { wickets = w; }
        public void incrementBallsBowled() { ballsBowled++; }
        public int getBallsBowled() { return ballsBowled; }
        public void setBallsBowled(int b) { ballsBowled = b; }
        public void reset() { ballsBowled = 0; runs = 0; wickets = 0; }
        public void setBallsBowledPublic(int b) { ballsBowled = b; }
        public void setBallsBowledDirect(int b) { ballsBowled = b; }
    }

    private static class BallEvent {
        final int strikerIndex, nonStrikerIndex, bowlerIndex;
        final int runs, over, ball, wickets, runsTotal;
        final boolean isExtra, isWicket, blb, no;
        BallEvent(int s, int ns, int b, int r, boolean extra, boolean wicket, int o, int bl, int w, int rt, boolean isblb, boolean isNo) {
            strikerIndex = s; nonStrikerIndex = ns; bowlerIndex = b;
            runs = r; isExtra = extra; isWicket = wicket; blb = isblb; no = isNo;
            over = o; ball = bl; wickets = w; runsTotal = rt;
        }
    }
}




