package com.zikraashik.smart_marks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
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
import com.zikraashik.smart_marks.adapter.SubjectsAdapter;
import com.zikraashik.smart_marks.model.Subject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddMarksActivity extends AppCompatActivity {

    private EditText etSearchStudent, etTerm;
    private TextView tvStudentDetails;
    private RecyclerView rvSubjects;
    private SubjectsAdapter subjectAdapter;
    private List<Subject> subjectList;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_marks);

        etSearchStudent = findViewById(R.id.et_search_student);
        etTerm = findViewById(R.id.et_term);
        tvStudentDetails = findViewById(R.id.tv_student_details);
        rvSubjects = findViewById(R.id.rv_subjects);

        subjectList = new ArrayList<>();
        subjectAdapter = new SubjectsAdapter(this, subjectList);
        rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        rvSubjects.setAdapter(subjectAdapter);

        etTerm = findViewById(R.id.et_term);

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

    public void addSubject(View view) {
        subjectList.add(new Subject("", "0"));
        subjectAdapter.notifyItemInserted(subjectList.size() - 1);
    }

    public void addMarks(View view) {
        String term = etTerm.getText().toString().trim();
        if (term.isEmpty()) {
            Toast.makeText(this, "Please enter term name", Toast.LENGTH_SHORT).show();
            return;
        }

        String indexNo = etSearchStudent.getText().toString().trim();
        if (indexNo.isEmpty()) {
            Toast.makeText(this, "Please enter student index number", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> marksData = new HashMap<>();
        for (int i = 0; i < subjectList.size(); i++) {
            Subject subject = subjectList.get(i);
            View viewHolder = rvSubjects.getChildAt(i);
            EditText etSubjectName = viewHolder.findViewById(R.id.tv_subject_name);
            EditText etSubjectMarks = viewHolder.findViewById(R.id.et_subject_marks);

            String subjectName = etSubjectName.getText().toString().trim();
            String subjectMarks = etSubjectMarks.getText().toString().trim();

            subject.setName(subjectName);
            subject.setMarks(subjectMarks);

            marksData.put(subjectName, subjectMarks);
        }

        // Get the current user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        String teacherId = currentUser.getUid();
        marksData.put("teacherId", teacherId);

        // Get reference to the Firebase database
        DatabaseReference studentMarksRef = FirebaseDatabase.getInstance().getReference().child("StudentMarks").child(indexNo).child(term);

        studentMarksRef.updateChildren(marksData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AddMarksActivity.this, "Marks added successfully", Toast.LENGTH_SHORT).show();
                        clearFields();
                    } else {
                        Toast.makeText(AddMarksActivity.this, "Failed to add marks", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearFields() {
        etSearchStudent.getText().clear();
        tvStudentDetails.setText("Student Details");
        etTerm.getText().clear();
        subjectList.clear();
        subjectAdapter.notifyDataSetChanged();
    }

    public void clearData(View view) {
        clearFields();
    }

    public void backHome(View view) {
        startActivity(new Intent(AddMarksActivity.this, TeacherHomeActivity.class));
        finish();
    }
}
