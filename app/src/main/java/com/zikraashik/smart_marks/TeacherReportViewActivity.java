package com.zikraashik.smart_marks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
    private TextView tvStudentDetails;
    private TextView tvEnglish, tvMaths, tvReligion, tvHistory, tvMusic, tvGeography, tvICT, tvScience, tvTotal;
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

        tvEnglish = findViewById(R.id.tv_english);
        tvMaths = findViewById(R.id.tv_maths);
        tvReligion = findViewById(R.id.tv_religion);
        tvHistory = findViewById(R.id.tv_history);
        tvMusic = findViewById(R.id.tv_music);
        tvGeography = findViewById(R.id.tv_geography);
        tvICT = findViewById(R.id.tv_ict);
        tvScience = findViewById(R.id.tv_science);
        tvTotal = findViewById(R.id.tv_total);

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
                    tvEnglish.setText(dataSnapshot.child("english").getValue(String.class));
                    tvMaths.setText(dataSnapshot.child("maths").getValue(String.class));
                    tvReligion.setText(dataSnapshot.child("religion").getValue(String.class));
                    tvHistory.setText(dataSnapshot.child("history").getValue(String.class));
                    tvMusic.setText(dataSnapshot.child("music").getValue(String.class));
                    tvGeography.setText(dataSnapshot.child("geography").getValue(String.class));
                    tvICT.setText(dataSnapshot.child("ict").getValue(String.class));
                    tvScience.setText(dataSnapshot.child("science").getValue(String.class));

                    // Calculate total
                    int total = 0;
                    for (DataSnapshot markSnapshot : dataSnapshot.getChildren()) {
                        String markStr = markSnapshot.getValue(String.class);
                        if (markStr != null) {
                            try {
                                total += Integer.parseInt(markStr);
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
        
        tvEnglish.setText("Marks");
        tvMaths.setText("Marks");
        tvReligion.setText("Marks");
        tvHistory.setText("Marks");
        tvMusic.setText("Marks");
        tvGeography.setText("Marks");
        tvICT.setText("Marks");
        tvScience.setText("Marks");
        tvTotal.setText("Marks");
    }
}
