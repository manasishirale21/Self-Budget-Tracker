package com.example.selfbudgettracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegistrationActivity extends AppCompatActivity {

    private EditText email, password, cPassword, phNumber, user;
    private Button signIn;

    private final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private final String phonePattern = "[6-9][0-9]{9}";

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private DBHelper DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        initializeViews();
    }

    private void initializeViews() {
        email = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);
        signIn = findViewById(R.id.registerButton);
        phNumber = findViewById(R.id.phoneEditText);
        cPassword = findViewById(R.id.confirmPasswordEditText);
        user = findViewById(R.id.usernameEditText);

        // Initialize Firebase Auth and DBHelper
        mAuth = FirebaseAuth.getInstance();
        DB = new DBHelper(this);

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Registration");
        progressDialog.setMessage("Please wait while registration...");
        progressDialog.setCanceledOnTouchOutside(false);

        signIn.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String Email = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String confirmPass = cPassword.getText().toString().trim();
        String ph = phNumber.getText().toString().trim();
        String userName = user.getText().toString().trim();

        if (Email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty() || ph.isEmpty() || userName.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Email.matches(emailPattern)) {
            email.setError("Enter valid email");
            return;
        }

        if (pass.length() < 6) {
            password.setError("Password must be at least 6 characters");
            return;
        }

        if (!ph.matches(phonePattern)) {
            phNumber.setError("Invalid Phone Number");
            return;
        }

        if (!pass.equals(confirmPass)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user already exists in SQLite
        if (DB.checkEmail(Email)) {
            Toast.makeText(this, "User already exists! Please sign in.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        // Create user in Firebase
        mAuth.createUserWithEmailAndPassword(Email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = task.getResult().getUser();

                if (firebaseUser != null) {
                    firebaseUser.sendEmailVerification();

                    // Insert into SQLite
                    boolean insert = DB.insertData(userName, Email, ph, pass);
                    if (insert) {
                        Toast.makeText(this, "Registered successfully. Verification email sent.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Local DB insert failed.", Toast.LENGTH_SHORT).show();
                    }

                    progressDialog.dismiss();

                    // Navigate to Login page
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                progressDialog.dismiss();
                Toast.makeText(this, "Firebase error: " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
