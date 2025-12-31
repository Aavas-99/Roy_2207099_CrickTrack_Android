package com.example.roy_2207099_cricktrack_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class UserRegister extends AppCompatActivity {

    EditText userEmail, userPassword;
    TextView emailError;
    Button btnRegister, btnBackReg;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        mAuth = FirebaseAuth.getInstance();

        userEmail = findViewById(R.id.userEmail);
        userPassword = findViewById(R.id.userPassword);
        emailError = findViewById(R.id.emailError);
        btnRegister = findViewById(R.id.btnRegister);
        btnBackReg = findViewById(R.id.btnBackReg);

        btnRegister.setEnabled(false);

        userEmail.addTextChangedListener(simpleWatcher(this::validateForm));
        userPassword.addTextChangedListener(simpleWatcher(this::validateForm));

        btnRegister.setOnClickListener(v -> onRegister());
        btnBackReg.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void validateForm() {
        String email = userEmail.getText().toString().trim();
        String pass = userPassword.getText().toString().trim();

        if (!isValidEmail(email)) {
            emailError.setText("Invalid email format");
            btnRegister.setEnabled(false);
        } else {
            emailError.setText("");
            btnRegister.setEnabled(pass.length() >= 6);
        }
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void onRegister() {
        String email = userEmail.getText().toString().trim();
        String password = userPassword.getText().toString().trim();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, UserLogin.class);
                        startActivity(intent);
                        finish();
                    } else {
                        showAlert("Registration Failed", task.getException().getMessage());
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
                .setPositiveButton("OK", (dialog, which) -> {
                    if(title.equals("Success")) finish();
                }).show();
    }
}