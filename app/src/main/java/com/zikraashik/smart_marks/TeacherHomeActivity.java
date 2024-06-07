package com.zikraashik.smart_marks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TeacherHomeActivity extends AppCompatActivity {

    private Button btnAddMarks, btnViewReports, btnViewCharts;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home);

        btnAddMarks = findViewById(R.id.btn_add_marks);
        btnViewReports = findViewById(R.id.btn_view_reports);
        btnViewCharts = findViewById(R.id.btn_view_charts);

        btnAddMarks.setOnClickListener(v -> startActivity(new Intent(TeacherHomeActivity.this, AddMarksActivity.class)));
        btnViewReports.setOnClickListener(v -> startActivity(new Intent(TeacherHomeActivity.this, TeacherReportViewActivity.class)));
        btnViewCharts.setOnClickListener(v -> startActivity(new Intent(TeacherHomeActivity.this, TeacherViewStatsActivity.class)));

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Retrieve user's first name and last name from Firebase Realtime Database
            mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String firstName = dataSnapshot.child("firstname").getValue(String.class);
                        String lastName = dataSnapshot.child("lastname").getValue(String.class);

                        // Update the welcome message with user's first name and last name
                        TextView welcomeTextView = findViewById(R.id.tv_welcome_teacher);
                        welcomeTextView.setText("Welcome, " + firstName + " " + lastName);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors
                }
            });
        }
    }


    public void logOut(View view) {
        mAuth.signOut();
        startActivity(new Intent(TeacherHomeActivity.this, LoginActivity.class));
        finish();
    }
}
