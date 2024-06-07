package com.zikraashik.smart_marks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StudentViewStatsActivity extends AppCompatActivity {

    private LinearLayout llStudentContainer;
    private TextView tvStudentDetails;
    private Spinner spinnerTerm;
    private String selectedTerm, indexNo, grade, className;
    private DatabaseReference studentsRef;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ArrayAdapter<String> termAdapter;
    private List<String> termsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_view_stats);

        llStudentContainer = findViewById(R.id.ll_student_container);
        tvStudentDetails = findViewById(R.id.tv_student_details);
        spinnerTerm = findViewById(R.id.spinner_term);

        studentsRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        termsList = new ArrayList<>();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Retrieve user's first name and last name from Firebase Realtime Database
            mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.child("firstname").getValue(String.class) + " " + dataSnapshot.child("lastname").getValue(String.class);
                        grade = dataSnapshot.child("grade").getValue(String.class);
                        className = dataSnapshot.child("className").getValue(String.class);

                        String studentDetails = "Name: " + name + "\nGrade: " + grade + "\nClass: " + className;
                        tvStudentDetails.setText(studentDetails);

                        indexNo = dataSnapshot.child("indexNo").getValue(String.class);

                        setStudentTerms();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors
                }
            });
        }

        termAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, termsList);
        termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTerm.setAdapter(termAdapter);

        spinnerTerm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTerm = (String) parent.getItemAtPosition(position);
                displayTopThreeStudents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setStudentTerms() {

        if (indexNo.isEmpty()) {
            return;
        }

        DatabaseReference studentMarksRef = FirebaseDatabase.getInstance().getReference()
                .child("StudentMarks").child(indexNo);

        studentMarksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    termsList.clear();
                    for (DataSnapshot termSnapshot : dataSnapshot.getChildren()) {
                        termsList.add(termSnapshot.getKey());
                    }
                    termAdapter.notifyDataSetChanged();
                    if (!termsList.isEmpty()) {
                        spinnerTerm.setSelection(0);
                    }
                } else {
                    Toast.makeText(StudentViewStatsActivity.this, "No marks found for your index number", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(StudentViewStatsActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayTopThreeStudents() {
        DatabaseReference studentsRef = FirebaseDatabase.getInstance().getReference("StudentMarks");

        // Query based on the composite key
        Query query = studentsRef.orderByChild("totalMarks").limitToLast(3);  // Query to get top 3 students with highest marks

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<DataSnapshot> topStudents = new ArrayList<>();
                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    topStudents.add(studentSnapshot);
                }

                Collections.reverse(topStudents);  // Because limitToLast returns in ascending order

                llStudentContainer.removeAllViews();  // Clear any existing views

                for (DataSnapshot studentSnapshot : topStudents) {
                    String studentId = studentSnapshot.getKey();
                    int totalMarks = studentSnapshot.child("totalMarks").getValue(Integer.class);

                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(studentId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            String studentName = userSnapshot.child("firstname").getValue(String.class) + " " +
                                    userSnapshot.child("lastname").getValue(String.class);
                            String grade = userSnapshot.child("grade").getValue(String.class);
                            String className = userSnapshot.child("className").getValue(String.class);

                            // Inflate layout for each student
                            View studentLayout = LayoutInflater.from(StudentViewStatsActivity.this)
                                    .inflate(R.layout.student_item_layout, llStudentContainer, false);

                            TextView tvStudentName = studentLayout.findViewById(R.id.tv_student_name);
                            TextView tvGrade = studentLayout.findViewById(R.id.tv_student_grade);
                            TextView tvClass = studentLayout.findViewById(R.id.tv_student_class);
                            TextView tvTotalMarks = studentLayout.findViewById(R.id.tv_student_total);

                            tvStudentName.setText(studentName);
                            tvGrade.setText("Grade: " + grade);
                            tvClass.setText("Class: " + className);
                            tvTotalMarks.setText("Total Marks: " + totalMarks);

                            // Add student layout to the container
                            llStudentContainer.addView(studentLayout);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(StudentViewStatsActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(StudentViewStatsActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int calculateTotalMarks(DataSnapshot termSnapshot) {
        int totalMarks = 0;

        for (DataSnapshot subjectSnapshot : termSnapshot.getChildren()) {
            String marks = subjectSnapshot.getValue(String.class);
            if (marks != null) {
                try {
                    totalMarks += Integer.parseInt(marks);
                } catch (NumberFormatException e) {
                    // Ignore if not a number
                }
            }
        }

        return totalMarks;
    }
}