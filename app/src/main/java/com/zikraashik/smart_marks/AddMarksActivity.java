package com.zikraashik.smart_marks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AddMarksActivity extends AppCompatActivity {

    private EditText etSearchStudent;
    private TextView tvStudentDetails;
    private EditText etTerm, etEnglish, etMaths, etReligion, etHistory, etMusic, etGeography, etICT, etScience;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_marks);

        etSearchStudent = findViewById(R.id.et_search_student);
        tvStudentDetails = findViewById(R.id.tv_student_details);

        etTerm = findViewById(R.id.et_term);
        etEnglish = findViewById(R.id.et_english);
        etMaths = findViewById(R.id.et_maths);
        etReligion = findViewById(R.id.et_religion);
        etHistory = findViewById(R.id.et_history);
        etMusic = findViewById(R.id.et_music);
        etGeography = findViewById(R.id.et_geography);
        etICT = findViewById(R.id.et_ict);
        etScience = findViewById(R.id.et_science);
    }

    public void searchStudent(View view) {
        String indexNo = etSearchStudent.getText().toString().trim();
        if (indexNo.isEmpty()) {
            Toast.makeText(this, "Please enter student index number", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.orderByChild("indexNo").equalTo(indexNo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String name = snapshot.child("firstname").getValue(String.class) + " " + snapshot.child("lastname").getValue(String.class);
                        String grade = snapshot.child("grade").getValue(String.class);
                        String className = snapshot.child("className").getValue(String.class);

                        String studentDetails = "Name: " + name + "\nGrade: " + grade + "\nClass: " + className;
                        tvStudentDetails.setText(studentDetails);
                        return;
                    }
                } else {
                    tvStudentDetails.setText("No student found with the provided index number");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddMarksActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addMarks(View view) {
        String term = etTerm.getText().toString();
        String englishMarks = etEnglish.getText().toString();
        String mathsMarks = etMaths.getText().toString();
        String religionMarks = etReligion.getText().toString();
        String historyMarks = etHistory.getText().toString();
        String musicMarks = etMusic.getText().toString();
        String geographyMarks = etGeography.getText().toString();
        String ICTMarks = etICT.getText().toString();
        String scienceMarks = etScience.getText().toString();

        // Check if any field is empty
        if (term.isEmpty() || englishMarks.isEmpty() || mathsMarks.isEmpty() || religionMarks.isEmpty() ||
                historyMarks.isEmpty() || musicMarks.isEmpty() || geographyMarks.isEmpty() ||
                ICTMarks.isEmpty() || scienceMarks.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a HashMap to store the marks
        HashMap<String, Object> marksMap = new HashMap<>();
        marksMap.put("term", term);
        marksMap.put("english", englishMarks);
        marksMap.put("maths", mathsMarks);
        marksMap.put("religion", religionMarks);
        marksMap.put("history", historyMarks);
        marksMap.put("music", musicMarks);
        marksMap.put("geography", geographyMarks);
        marksMap.put("ict", ICTMarks);
        marksMap.put("science", scienceMarks);

        // Get the current user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Get reference to the Firebase database
            DatabaseReference studentMarksRef = FirebaseDatabase.getInstance().getReference().child("studentmarks").child(userId);

            // Push the marks data to the Firebase database
            studentMarksRef.push().setValue(marksMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddMarksActivity.this, "Marks added successfully", Toast.LENGTH_SHORT).show();
                            // Clear the EditText fields after successfully adding marks
                            clearFields();
                        } else {
                            Toast.makeText(AddMarksActivity.this, "Failed to add marks", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void clearFields() {
        etTerm.getText().clear();
        etEnglish.getText().clear();
        etMaths.getText().clear();
        etReligion.getText().clear();
        etHistory.getText().clear();
        etMusic.getText().clear();
        etGeography.getText().clear();
        etICT.getText().clear();
        etScience.getText().clear();
    }

    public void clearData(View view) {
        etSearchStudent.getText().clear();
        tvStudentDetails.setText("Name\nGrade\nClass");
        clearFields();
    }
}
