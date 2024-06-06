package com.zikraashik.smart_marks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText fNameEditText,lNameEditText, emailEditText, passwordEditText, retypePasswordEditText, indexNoEditText;
    private Spinner gradeSpinner, classSpinner;
    private RadioGroup rgRole;
    private ViewStub viewStubStudent;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    private String userType = "teacher";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fNameEditText = findViewById(R.id.et_fname);
        lNameEditText = findViewById(R.id.et_lname);
        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_password);
        retypePasswordEditText = findViewById(R.id.et_retype_password);
        rgRole = findViewById(R.id.rg_role);
        viewStubStudent = findViewById(R.id.view_stub_student);
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

        (findViewById(R.id.show_re_pass)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setActivated(!v.isActivated());
                if (v.isActivated()) {
                    retypePasswordEditText.setTransformationMethod(null);
                } else {
                    retypePasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
                }
                retypePasswordEditText.setSelection(retypePasswordEditText.getText().length());
            }
        });

        rgRole.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_student) {
                    userType = "student";
                    if (viewStubStudent.getParent() != null) {
                        viewStubStudent.inflate();
                        gradeSpinner = findViewById(R.id.spinner_grade);
                        classSpinner = findViewById(R.id.spinner_class);
                        indexNoEditText = findViewById(R.id.ed_index_no);

                        // Populate the spinners
                        ArrayAdapter<CharSequence> gradeAdapter = ArrayAdapter.createFromResource(RegisterActivity.this,
                                R.array.grade_array, android.R.layout.simple_spinner_item);
                        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        gradeSpinner.setAdapter(gradeAdapter);

                        ArrayAdapter<CharSequence> classAdapter = ArrayAdapter.createFromResource(RegisterActivity.this,
                                R.array.class_array, android.R.layout.simple_spinner_item);
                        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        classSpinner.setAdapter(classAdapter);
                    } else {
                        viewStubStudent.setVisibility(View.VISIBLE);
                    }
                } else if (checkedId == R.id.rb_teacher) {
                    userType = "teacher";
                    viewStubStudent.setVisibility(View.GONE);
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();
    }

    public void register(View view) {
        String fname = fNameEditText.getText().toString();
        String lname = lNameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String retypePassword = retypePasswordEditText.getText().toString();

        if (fname.isEmpty() || lname.isEmpty() || email.isEmpty() || password.isEmpty() || retypePassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(retypePassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> userdataMap = new HashMap<>();
        userdataMap.put("lastname", lname);
        userdataMap.put("firstname", fname);
        userdataMap.put("email", email);
        userdataMap.put("userType", userType);

        if (userType.equals("student")) {
            String grade = gradeSpinner.getSelectedItem().toString();
            String className = classSpinner.getSelectedItem().toString();
            String indexNo = indexNoEditText.getText().toString();

            if (grade.isEmpty() || className.isEmpty() || indexNo.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            userdataMap.put("grade", grade);
            userdataMap.put("className", className);
            userdataMap.put("indexNo", indexNo);
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();

                                // Add user details to the 'users' node in the database
                                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
                                
                                usersRef.child(userId).updateChildren(userdataMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                                    finish();
                                                }
                                                else
                                                {
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });


                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void openLoginActivity(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }
}