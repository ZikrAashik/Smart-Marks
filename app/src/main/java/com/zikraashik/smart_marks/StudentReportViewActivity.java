package com.zikraashik.smart_marks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class StudentReportViewActivity extends AppCompatActivity {

    private TextView tvStudentDetails;
    private TextView tvTotal;
    private Spinner spinnerTerm;
    private ArrayAdapter<String> termAdapter;
    private List<String> termsList;
    private String indexNo;
    private LinearLayout llSubjectMarksContainer;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_report_view);

        tvStudentDetails = findViewById(R.id.tv_student_details);
        spinnerTerm = findViewById(R.id.spinner_term);
        tvTotal = findViewById(R.id.tv_total);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        llSubjectMarksContainer = findViewById(R.id.ll_subject_marks_container);

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
                        String grade = dataSnapshot.child("grade").getValue(String.class);
                        String className = dataSnapshot.child("className").getValue(String.class);

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
                String selectedTerm = termsList.get(position);
                loadMarks(selectedTerm);
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
                    Toast.makeText(StudentReportViewActivity.this, "No marks found for your index number", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(StudentReportViewActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMarks(String term) {
        DatabaseReference studentMarksRef = FirebaseDatabase.getInstance().getReference()
                .child("StudentMarks").child(indexNo).child(term);

        studentMarksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    llSubjectMarksContainer.removeAllViews();
                    float total = 0;

                    for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                        String subjectName = subjectSnapshot.getKey();

                        if(subjectName.equals("teacherId") || subjectName.equals("grade_class") || subjectName.equals("totalMarks"))
                            continue;

                        String marks = subjectSnapshot.getValue(String.class);

                        View subjectLayout = LayoutInflater.from(StudentReportViewActivity.this)
                                .inflate(R.layout.item_view_subject, llSubjectMarksContainer, false);

                        // Set subject name
                        TextView tvSubjectName = subjectLayout.findViewById(R.id.tv_subject_name);
                        tvSubjectName.setText(subjectName);

                        // Set marks
                        TextView tvMarks = subjectLayout.findViewById(R.id.tv_subject_marks);
                        tvMarks.setText(marks);

                        // Add subject layout to the container
                        llSubjectMarksContainer.addView(subjectLayout);

                        if (marks != null) {
                            try {
                                total += Float.parseFloat(marks);
                            } catch (NumberFormatException e) {
                                // Ignore if not a number
                            }
                        }
                    }

                    tvTotal.setText(String.valueOf(total));
                } else {
                    Toast.makeText(StudentReportViewActivity.this, "No marks found for the selected term", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(StudentReportViewActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void backHome(View view) {
        startActivity(new Intent(StudentReportViewActivity.this, StudentHomeActivity.class));
        finish();
    }
}
