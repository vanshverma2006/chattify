package com.example.chattify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class registration extends AppCompatActivity {

    TextView loginbut;
    EditText rg_username, rg_email, rg_password, rg_repassword;
    Button rg_signup;
    CircleImageView rg_profileImg;

    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;

    Uri imageURI;
    String imageuri;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Account...");
        progressDialog.setCancelable(false);

        // Firebase
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        // Views
        loginbut = findViewById(R.id.loginbut);

        rg_username = findViewById(R.id.rgusername);
        rg_email = findViewById(R.id.rgemail);
        rg_password = findViewById(R.id.rgpassword);
        rg_repassword = findViewById(R.id.rgrepassword);

        rg_profileImg = findViewById(R.id.profilerg0);
        rg_signup = findViewById(R.id.signupbutton);

        // Go to login
        loginbut.setOnClickListener(v -> {

            Intent intent = new Intent(registration.this, login.class);
            startActivity(intent);
            finish();

        });

        // Select Profile Image
        rg_profileImg.setOnClickListener(v -> {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(
                    Intent.createChooser(intent, "Select Picture"),
                    10
            );

        });

        // Signup
        rg_signup.setOnClickListener(v -> {

            String namee = rg_username.getText().toString().trim();
            String emaill = rg_email.getText().toString().trim();
            String password = rg_password.getText().toString();
            String cPassword = rg_repassword.getText().toString();

            String status = "Hey I'm Using This Application";

            // Validation
            if (TextUtils.isEmpty(namee) ||
                    TextUtils.isEmpty(emaill) ||
                    TextUtils.isEmpty(password) ||
                    TextUtils.isEmpty(cPassword)) {

                Toast.makeText(
                        registration.this,
                        "Please Enter Valid Information",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (!emaill.matches(emailPattern)) {

                rg_email.setError("Type A Valid Email Here");
                return;
            }

            if (password.length() < 6) {

                rg_password.setError(
                        "Password Must Be 6 Characters Or More"
                );

                return;
            }

            if (!password.equals(cPassword)) {

                rg_repassword.setError(
                        "The Password Doesn't Match"
                );

                return;
            }

            // Show progress
            progressDialog.show();

            // Create Firebase account
            auth.createUserWithEmailAndPassword(emaill, password)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            String id = task.getResult()
                                    .getUser()
                                    .getUid();

                            DatabaseReference reference =
                                    database.getReference()
                                            .child("user")
                                            .child(id);

                            StorageReference storageReference =
                                    storage.getReference()
                                            .child("Upload")
                                            .child(id);

                            // If user selected an image
                            if (imageURI != null) {

                                storageReference
                                        .putFile(imageURI)
                                        .addOnCompleteListener(uploadTask -> {

                                            if (uploadTask.isSuccessful()) {

                                                storageReference
                                                        .getDownloadUrl()
                                                        .addOnSuccessListener(uri -> {

                                                            imageuri =
                                                                    uri.toString();

                                                            createUser(
                                                                    reference,
                                                                    id,
                                                                    namee,
                                                                    emaill,
                                                                    password,
                                                                    imageuri,
                                                                    status
                                                            );

                                                        });

                                            } else {

                                                progressDialog.dismiss();

                                                Toast.makeText(
                                                        registration.this,
                                                        "Image Upload Failed",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        });

                            } else {

                                // Default profile image
                                imageuri =
                                        "https://firebasestorage.googleapis.com/v0/b/av-messenger-dc8f3.appspot.com/o/man.png?alt=media&token=880f431d-9344-45e7-afe4-c2cafe8a5257";

                                createUser(
                                        reference,
                                        id,
                                        namee,
                                        emaill,
                                        password,
                                        imageuri,
                                        status
                                );
                            }

                        } else {

                            progressDialog.dismiss();

                            String error =
                                    task.getException() != null
                                            ? task.getException().getMessage()
                                            : "Registration failed";

                            Toast.makeText(
                                    registration.this,
                                    error,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });
    }

    // Save user in Realtime Database
    private void createUser(
            DatabaseReference reference,
            String id,
            String name,
            String email,
            String password,
            String image,
            String status
    ) {

        Users users = new Users(
                id,
                name,
                email,
                password,
                image,
                status
        );

        reference.setValue(users)
                .addOnCompleteListener(task -> {

                    progressDialog.dismiss();

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                registration.this,
                                "Account Created Successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent intent =
                                new Intent(
                                        registration.this,
                                        MainActivity.class
                                );

                        startActivity(intent);
                        finish();

                    } else {

                        Toast.makeText(
                                registration.this,
                                "Error in creating the user",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == 10 &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null) {

            imageURI = data.getData();

            rg_profileImg.setImageURI(imageURI);
        }
    }
}