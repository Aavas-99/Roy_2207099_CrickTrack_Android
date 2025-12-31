package com.example.roy_2207099_cricktrack_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminLogin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        EditText email = findViewById(R.id.adminEmail);
        EditText password = findViewById(R.id.adminPassword);
        Button loginBtn = findViewById(R.id.btnAdminLogin);
        Button backBtn = findViewById(R.id.btnBack);

        loginBtn.setOnClickListener(v -> {
            String adminEmail = email.getText().toString().trim();
            String adminPass = password.getText().toString().trim();

            if (adminEmail.isEmpty() || adminPass.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            }
            else if (adminEmail.equals("roy.aavas@gmail.com") && adminPass.equals("123")) {
                Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, AdminDashboard1.class);
                startActivity(intent);
                finish();
            }
            else {
                Toast.makeText(this, "Invalid Admin Credentials", Toast.LENGTH_SHORT).show();
            }
        });

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
