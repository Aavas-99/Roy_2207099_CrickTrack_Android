package com.example.roy_2207099_cricktrack_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminDashboard1 extends AppCompatActivity {

    TextView textView, textView2;
    Button button, button2 , button3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);


        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateMatch.class);
            startActivity(intent);
            finish();
        });

        button2.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminDatabase.class);
            startActivity(intent);
            finish();
        });

        button3.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminLogin.class);
            startActivity(intent);
            finish();
        });
    }
}