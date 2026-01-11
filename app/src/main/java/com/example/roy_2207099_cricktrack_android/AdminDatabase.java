package com.example.roy_2207099_cricktrack_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminDatabase extends AppCompatActivity {

    private RecyclerView rvMatches;
    private MatchAdapter adapter;
    private final List<MatchRow> matchList = new ArrayList<>();
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_database);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(AdminDatabase.this, AdminDashboard1.class);
            startActivity(intent);
            finish();
        });

        rvMatches = findViewById(R.id.rvMatches);
        rvMatches.setLayoutManager(new LinearLayoutManager(this));

        dbRef = FirebaseDatabase.getInstance().getReference("matches");

        loadMatchesFromFirebase();
    }

    private void loadMatchesFromFirebase() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                matchList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    MatchRow match = data.getValue(MatchRow.class);
                    if (match != null) {
                        match.id = data.getKey(); // Get the Firebase ID
                        matchList.add(match);
                    }
                }
                Collections.reverse(matchList);

                adapter = new MatchAdapter(matchList, matchId -> openMatchSummary(matchId));
                rvMatches.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDatabase.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openMatchSummary(String matchId) {
        Intent intent = new Intent(this, MatchSummary.class);
        intent.putExtra("MATCH_ID", matchId);
        startActivity(intent);
    }
}