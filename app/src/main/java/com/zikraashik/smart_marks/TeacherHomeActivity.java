package com.zikraashik.smart_marks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class TeacherHomeActivity extends AppCompatActivity {

    private Button btnAddMarks, btnViewReports, btnViewCharts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home);

        btnAddMarks = findViewById(R.id.btn_add_marks);
        btnViewReports = findViewById(R.id.btn_view_reports);
        btnViewCharts = findViewById(R.id.btn_view_charts);

        //btnAddMarks.setOnClickListener(v -> startActivity(new Intent(TeacherHomeActivity.this, AddMarksActivity.class)));
        //btnViewReports.setOnClickListener(v -> startActivity(new Intent(TeacherHomeActivity.this, ViewReportsActivity.class)));
        //btnViewCharts.setOnClickListener(v -> startActivity(new Intent(TeacherHomeActivity.this, ViewChartsActivity.class)));
    }


    public void logOut(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(TeacherHomeActivity.this, LoginActivity.class));
        finish();
    }
}
