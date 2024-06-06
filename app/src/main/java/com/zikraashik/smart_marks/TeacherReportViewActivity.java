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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class TeacherReportViewActivity extends AppCompatActivity {

    private EditText etSearchStudent;
    private TextView tvStudentDetails, tvTotal;
    private LinearLayout llSubjectMarksContainer;
    private Spinner spinnerTerm;
    private ArrayAdapter<String> termAdapter;
    private List<String> termsList;
    private String indexNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_report_view);

        etSearchStudent = findViewById(R.id.et_search_student);
        tvStudentDetails = findViewById(R.id.tv_student_details);
        spinnerTerm = findViewById(R.id.spinner_term);
        tvTotal = findViewById(R.id.tv_total);
        llSubjectMarksContainer = findViewById(R.id.ll_subject_marks_container);

        termsList = new ArrayList<>();
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

    public void searchStudent(View view) {
        indexNo = etSearchStudent.getText().toString().trim();
        if (indexNo.isEmpty()) {
            Toast.makeText(this, "Please enter student index number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to the Users node to get user ID using indexNo
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        userRef.orderByChild("indexNo").equalTo(indexNo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot userSnapshot = dataSnapshot.getChildren().iterator().next();

                    String name = userSnapshot.child("firstname").getValue(String.class) + " " + userSnapshot.child("lastname").getValue(String.class);
                    String grade = userSnapshot.child("grade").getValue(String.class);
                    String className = userSnapshot.child("className").getValue(String.class);

                    String studentDetails = "Name: " + name + "\nGrade: " + grade + "\nClass: " + className;
                    tvStudentDetails.setText(studentDetails);

                    loadStudentData();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void loadStudentData() {
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
                    Toast.makeText(TeacherReportViewActivity.this, "No marks found for the provided index number", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TeacherReportViewActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

                        if(subjectName.equals("teacherId"))
                            continue;

                        String marks = subjectSnapshot.getValue(String.class);

                        View subjectLayout = LayoutInflater.from(TeacherReportViewActivity.this)
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
                    Toast.makeText(TeacherReportViewActivity.this, "No marks found for the selected term", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TeacherReportViewActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void clearData(View view) {
        etSearchStudent.getText().clear();
        tvStudentDetails.setText("Student Details");
        termsList.clear();

        llSubjectMarksContainer.removeAllViews();

        tvTotal.setText("Marks");
    }

    public void backHome(View view) {
        startActivity(new Intent(TeacherReportViewActivity.this, TeacherHomeActivity.class));
        finish();
    }
}
