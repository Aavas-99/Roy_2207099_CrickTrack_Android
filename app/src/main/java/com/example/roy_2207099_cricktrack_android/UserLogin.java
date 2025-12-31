package com.example.roy_2207099_cricktrack_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class UserLogin extends AppCompatActivity {

    EditText loginEmail, loginPassword;
    Button btnLogin, btnBack;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        mAuth = FirebaseAuth.getInstance();

        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnBack = findViewById(R.id.btnBack);

        btnLogin.setEnabled(false);

        loginEmail.addTextChangedListener(simpleWatcher(this::validateForm));
        loginPassword.addTextChangedListener(simpleWatcher(this::validateForm));

        btnLogin.setOnClickListener(v -> onLogin());
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void validateForm() {
        String email = loginEmail.getText().toString().trim();
        String pass = loginPassword.getText().toString().trim();
        btnLogin.setEnabled(!email.isEmpty() && !pass.isEmpty());
    }

    private void onLogin() {
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

                    } else {
                        showAlert("Login Failed", "Invalid email or password.");
                    }
                });
    }

    private TextWatcher simpleWatcher(Runnable r) {
        return new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) { r.run(); }
            public void afterTextChanged(Editable s) {}
        };
    }

    private void showAlert(String title, String msg) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }
}