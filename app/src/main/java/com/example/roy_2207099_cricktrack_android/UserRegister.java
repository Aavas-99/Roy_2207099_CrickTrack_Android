package com.example.roy_2207099_cricktrack_android;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UserRegister extends AppCompatActivity {

    EditText userEmail, userPassword;
    TextView emailError;
    Button btnRegister, btnBackReg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

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
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void validateForm() {
        String email = userEmail.getText().toString().trim();
        String pass = userPassword.getText().toString().trim();

        if (email.isEmpty()) {
            emailError.setText("");
            btnRegister.setEnabled(false);
            return;
        }

        if (!isValidEmail(email)) {
            emailError.setText("Invalid email format");
            btnRegister.setEnabled(false);
        } else {
            emailError.setText("");
            btnRegister.setEnabled(!pass.isEmpty());
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void onRegister() {
        String email = userEmail.getText().toString().trim().toLowerCase();
        String password = userPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "All fields are required!");
            return;
        }

        DBHelper dbHelper = DBHelper.getInstance(this);
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            Cursor cursor = db.rawQuery("SELECT id FROM users WHERE username = ?", new String[]{email});
            if (cursor.moveToFirst()) {
                showAlert("Registration Failed", "Email already registered!");
                cursor.close();
                return;
            }
            cursor.close();

            String hashedPassword = PasswordHash.hashPassword(password);

            ContentValues values = new ContentValues();
            values.put("username", email);
            values.put("password", hashedPassword);

            long newRowId = db.insert("users", null, values);

            if (newRowId != -1) {
                Cursor debugCursor = db.rawQuery("SELECT * FROM users", null);
                while (debugCursor.moveToNext()) {
                    android.util.Log.d("DB_CHECK", "User: " + debugCursor.getString(1));
                }
                debugCursor.close();
                showAlert("Success", "Registration successful!");
            }
            else {
                showAlert("Error", "Database insertion failed.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Registration failed. Try again!");
        }
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
