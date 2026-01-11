package com.example.roy_2207099_cricktrack_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserDatabase extends AppCompatActivity {
    private RecyclerView rvMatches;
    private UserMatchAdapter adapter;
    private final List<MatchRow> matchList = new ArrayList<>();
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_database);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                getOnBackPressedDispatcher().onBackPressed();
            });
        }
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(UserDatabase.this, UserDashboard1.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        rvMatches = findViewById(R.id.rvMatches);
        rvMatches.setLayoutManager(new LinearLayoutManager(this));
        dbRef = FirebaseDatabase.getInstance().getReference();

        loadMatches();
    }

    private void loadMatches() {
        dbRef.child("matches").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                matchList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MatchRow m = ds.getValue(MatchRow.class);
                    if (m != null) {
                        m.id = ds.getKey();
                        matchList.add(m);
                    }
                }
                Collections.reverse(matchList);

                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    adapter = new UserMatchAdapter(matchList, uid, new UserMatchAdapter.OnMatchClickListener() {
                        @Override
                        public void onReqest(MatchRow m) { sendRequest(m); }

                        @Override
                        public void onView(MatchRow m) {
                            Intent i = new Intent(UserDatabase.this, MatchSummary.class);
                            i.putExtra("MATCH_ID", m.id);
                            startActivity(i);
                        }
                    });
                    rvMatches.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDatabase.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendRequest(MatchRow match) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String requestId = match.id + "_" + uid;

        AccessRequest req = new AccessRequest(uid, email, match.id, match.team_a + " vs " + match.team_b);
        dbRef.child("pending_requests").child(requestId).setValue(req)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Request Sent", Toast.LENGTH_SHORT).show());
    }
}