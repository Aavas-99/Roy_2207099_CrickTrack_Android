package com.example.roy_2207099_cricktrack_android;

import java.util.ArrayList;
public class MatchModels {

    public static class PlayerStats {
        public String playerName;
        public int runs;
        public int ballsPlayed;
        public boolean isOut;
        public int runsGiven;
        public int bowls;
        public int wickets;
        public PlayerStats() {}
    }

    public static class MatchData {
        public String team_a;
        public String team_b;
        public int overs;
        public String stadium;
        public String date;
        public String toss_winner;
        public String decision;
        public String result;

        public ArrayList<PlayerStats> players_a;
        public ArrayList<PlayerStats> players_b;

        public int totalRuns;
        public int wickets;
        public String currentOvers;

        public MatchData() {}
    }

    public static class BatsmanStat {
        public String name;
        public int runs;
        public int balls;
        public int is_out;
        public String team;
        public BatsmanStat() {}
    }

    public static class BowlerStat {
        public String name;
        public int balls_bowled;
        public int runs;
        public int wickets;
        public String team;
        public BowlerStat() {}
    }

}

