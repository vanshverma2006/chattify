package com.example.chattify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;

    RecyclerView mainUserRecyclerView;
    UserAdpter adapter;
    ArrayList<Users> usersArrayList;

    ImageView imglogout;
    ImageView cumbut, setbut;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Check if user is logged in
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, login.class);
            startActivity(intent);
            finish();
            return;
        }

        // Firebase Database
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("user");

        // Initialize views
        cumbut = findViewById(R.id.camBut);
        setbut = findViewById(R.id.settingBut);
        imglogout = findViewById(R.id.logoutimg);

        // RecyclerView
        usersArrayList = new ArrayList<>();

        mainUserRecyclerView = findViewById(R.id.mainUserRecyclerView);
        mainUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserAdpter(MainActivity.this, usersArrayList);
        mainUserRecyclerView.setAdapter(adapter);

        // Get users from Firebase
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Prevent duplicate users
                usersArrayList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    Users users = dataSnapshot.getValue(Users.class);

                    if (users != null) {
                        usersArrayList.add(users);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error if needed
            }
        });

        // Logout button
        imglogout.setOnClickListener(v -> {

            Dialog dialog = new Dialog(MainActivity.this, R.style.Theme_Chattify);
            dialog.setContentView(R.layout.dialog_layout);

            Button no = dialog.findViewById(R.id.nobnt);
            Button yes = dialog.findViewById(R.id.yesbnt);

            yes.setOnClickListener(v1 -> {

                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(MainActivity.this, login.class);
                startActivity(intent);
                finish();

            });

            no.setOnClickListener(v1 -> dialog.dismiss());

            dialog.show();
        });

        // Settings button
        setbut.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, setting.class);
            startActivity(intent);

        });

        // Camera button
        cumbut.setOnClickListener(v -> {

            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 10);

        });
    }
}