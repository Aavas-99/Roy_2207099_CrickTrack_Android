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
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class Request extends AppCompatActivity {

    private RecyclerView rvRequests;
    private RequestAdapter adapter;
    private List<AccessRequest> requestList = new ArrayList<>();
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        Toolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(Request.this, AdminDashboard1.class);
                startActivity(intent);
                finish();
            }
        });

        rvRequests = findViewById(R.id.rvRequests);
        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        dbRef = FirebaseDatabase.getInstance().getReference();

        loadPendingRequests();
    }

    private void loadPendingRequests() {
        dbRef.child("pending_requests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    AccessRequest req = ds.getValue(AccessRequest.class);
                    if (req != null) {
                        requestList.add(req);
                    }
                }

                adapter = new RequestAdapter(requestList, new RequestAdapter.OnRequestActionListener() {
                    @Override
                    public void onApprove(AccessRequest request) {
                        grantPermission(request);
                    }

                    @Override
                    public void onReject(AccessRequest request) {
                        deleteRequest(request, "Request Rejected");
                    }
                });
                rvRequests.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Request.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void grantPermission(AccessRequest request) {
        dbRef.child("permissions").child(request.matchId).child(request.userId).setValue(true)
                .addOnSuccessListener(aVoid -> deleteRequest(request, "Match Access Approved"))
                .addOnFailureListener(e -> Toast.makeText(Request.this, "Failed to approve", Toast.LENGTH_SHORT).show());
    }

    private void deleteRequest(AccessRequest request, String message) {
        String requestId = request.matchId + "_" + request.userId;
        dbRef.child("pending_requests").child(requestId).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(Request.this, message, Toast.LENGTH_SHORT).show());
    }
}