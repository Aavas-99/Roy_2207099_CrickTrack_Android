package com.example.roy_2207099_cricktrack_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserDashboard1 extends AppCompatActivity {

    private CardView cardProfileMenu;
    private ImageButton btnMenu;
    private TextView userEmailDisplay;
    private Button btnLogout, btnViewMatches;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard1);

        mAuth = FirebaseAuth.getInstance();

        btnMenu = findViewById(R.id.btnMenu);
        cardProfileMenu = findViewById(R.id.cardProfileMenu);
        userEmailDisplay = findViewById(R.id.userEmailDisplay);
        btnLogout = findViewById(R.id.btnLogout);
        btnViewMatches = findViewById(R.id.btnViewMatches);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userEmailDisplay.setText(user.getEmail());
        }

        btnMenu.setOnClickListener(v -> {
            if (cardProfileMenu.getVisibility() == View.GONE) {
                cardProfileMenu.setVisibility(View.VISIBLE);
            } else {
                cardProfileMenu.setVisibility(View.GONE);
            }
        });

        btnViewMatches = findViewById(R.id.btnViewMatches);

        btnViewMatches.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserDatabase.class);
            startActivity(intent);
            finish();
        });



        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, UserLogin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });



    }
}