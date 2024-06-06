package com.zikraashik.smart_marks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_password);
        progressBar = findViewById(R.id.progress_bar);
        (findViewById(R.id.show_pass)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setActivated(!v.isActivated());
                if (v.isActivated()) {
                    passwordEditText.setTransformationMethod(null);
                } else {
                    passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
                }
                passwordEditText.setSelection(passwordEditText.getText().length());
            }
        });

        mAuth = FirebaseAuth.getInstance();
    }

    public void login(View view) {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null;
                        String userId = user.getUid();

                        Toast.makeText(this, "Authentication success.", Toast.LENGTH_SHORT).show();

                        loginType(userId);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                });


    }

    private void loginType(String userId) {
        final DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userType = snapshot.child("userType").getValue(String.class);

                    if ("student".equals(userType)) {
                        Toast.makeText(LoginActivity.this, "Student", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(LoginActivity.this, StudentHomeActivity.class));
                    } else if ("teacher".equals(userType)) {
                        Toast.makeText(LoginActivity.this, "Teacher", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(LoginActivity.this, TeacherHomeActivity.class));
                    }
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openRegisterActivity(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void closeApp(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Exit Smart Marks")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity(); // Close all activities and exit the app
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}