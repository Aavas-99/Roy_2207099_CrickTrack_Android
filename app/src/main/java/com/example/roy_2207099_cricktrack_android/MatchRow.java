package com.example.roy_2207099_cricktrack_android;

public class MatchRow {
    public String id; // Firebase key
    public String team_a;
    public String team_b;
    public String stadium;
    public String date;
    public String result;

    public MatchRow() {} // Required for Firebase

    public String getTeamsText() {
        return team_a + " vs " + team_b;
    }
}