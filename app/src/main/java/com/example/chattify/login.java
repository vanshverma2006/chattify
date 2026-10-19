package com.example.chattify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {

    TextView logsignup;
    Button button;
    EditText email, password;

    FirebaseAuth auth;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Hide ActionBar safely
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        // Firebase
        auth = FirebaseAuth.getInstance();

        // Views
        button = findViewById(R.id.logbutton);
        email = findViewById(R.id.editTexLogEmail);
        password = findViewById(R.id.editTextLogPassword);
        logsignup = findViewById(R.id.logsignup);

        // Go to Registration
        logsignup.setOnClickListener(v -> {

            Intent intent = new Intent(
                    login.this,
                    registration.class
            );

            startActivity(intent);
            finish();
        });

        // Login
        button.setOnClickListener(v -> {

            String Email = email.getText().toString().trim();
            String pass = password.getText().toString();

            // Validation
            if (TextUtils.isEmpty(Email)) {

                email.setError("Enter The Email");
                return;

            } else if (TextUtils.isEmpty(pass)) {

                password.setError("Enter The Password");
                return;

            } else if (!Email.matches(emailPattern)) {

                email.setError("Give Proper Email Address");
                return;

            } else if (pass.length() < 6) {

                password.setError("Password Needs To Be 6 Characters Or More");
                return;
            }

            // Show progress before Firebase request
            progressDialog.show();

            auth.signInWithEmailAndPassword(Email, pass)
                    .addOnCompleteListener(task -> {

                        progressDialog.dismiss();

                        if (task.isSuccessful()) {

                            Intent intent = new Intent(
                                    login.this,
                                    MainActivity.class
                            );

                            startActivity(intent);
                            finish();

                        } else {

                            String error = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Login failed";

                            Toast.makeText(
                                    login.this,
                                    error,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });
    }
}