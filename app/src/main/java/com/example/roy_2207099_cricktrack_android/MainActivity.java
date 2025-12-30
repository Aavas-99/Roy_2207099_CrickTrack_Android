package com.example.roy_2207099_cricktrack_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button adminLogin = findViewById(R.id.btnAdminLogin);
        Button userLogin = findViewById(R.id.btnUserLogin);
        Button userRegister = findViewById(R.id.btnUserRegister);

        adminLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminLogin.class);
            startActivity(intent);
            finish();
        });

        userLogin.setOnClickListener(v -> {

        });

        userRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserRegister.class);
            startActivity(intent);
            finish();
        });

    }
}