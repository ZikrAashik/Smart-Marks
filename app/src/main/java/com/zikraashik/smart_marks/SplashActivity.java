package com.zikraashik.smart_marks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private ProgressBar progress_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        progress_bar = findViewById(R.id.progress_bar);
        progress_bar.setMax(100);
        startProgressBar();
    }

    private void startProgressBar() {
        final int delayMillis = 50;
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            int progress = 0;

            @Override
            public void run() {
                progress=progress+2;
                progress_bar.setProgress(progress);

                if (progress >= 100) {
                    checkLoginStatus();
                    return;
                }

                handler.postDelayed(this, delayMillis);
            }
        };

        handler.postDelayed(runnable, delayMillis);
    }

    private void checkLoginStatus() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User is already logged in, redirect to Home
            loginUser(currentUser.getUid());
        } else {
            // User is not logged in, redirect to LoginActivity
            startActivityLogin();
        }
    }

    private void startActivityLogin() {
        Intent i = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    private void loginUser(String userId) {
        final DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userType = snapshot.child("userType").getValue(String.class);
                    if ("student".equals(userType)) {
                        startActivity(new Intent(SplashActivity.this, StudentHomeActivity.class));
                    } else if ("teacher".equals(userType)) {
                        startActivity(new Intent(SplashActivity.this, TeacherHomeActivity.class));
                    }
                    finish();
                } else {
                    Toast.makeText(SplashActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SplashActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}