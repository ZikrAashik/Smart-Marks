package com.zikraashik.smart_marks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class StudentHomeActivity extends AppCompatActivity {

    private Button btnViewReport, btnViewCharts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        btnViewReport = findViewById(R.id.btn_view_report);
        btnViewCharts = findViewById(R.id.btn_view_charts);

        //btnViewReport.setOnClickListener(v -> startActivity(new Intent(StudentHomeActivity.this, ViewReportActivity.class)));
        //btnViewCharts.setOnClickListener(v -> startActivity(new Intent(StudentHomeActivity.this, ViewChartsActivity.class)));
    }

    public void logOut(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(StudentHomeActivity.this, LoginActivity.class));
        finish();
    }
}
