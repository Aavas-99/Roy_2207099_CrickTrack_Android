package com.example.roy_2207099_cricktrack_android;

public class AccessRequest {
    public String userId, userEmail, matchId, matchName;

    public AccessRequest() {}

    public AccessRequest(String userId, String userEmail, String matchId, String matchName) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.matchId = matchId;
        this.matchName = matchName;
    }
}