package com.example.syncmusic;

import android.content.Intent;
import android.os.Trace;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Timestamp;


public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText emailTextView,displayName;
    private EditText passwordTextView;
    private ProgressBar progressBar;

    DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_signup);
        findViewById(R.id.registerButton).setOnClickListener(this);
        displayName = findViewById(R.id.displayName);
        emailTextView = findViewById(R.id.emailEditText);
        passwordTextView = findViewById(R.id.passwordEditText);
        progressBar = findViewById(R.id.progressBar);

        databaseReference = FirebaseDatabase.getInstance().getReference("UsersInfo");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.registerButton:
                registerUser();
                break;
        }
    }

    private void registerUser(){
        String email = emailTextView.getText().toString().trim();
        String password = passwordTextView.getText().toString().trim();
        final String name = displayName.getText().toString().trim();
        if(email.isEmpty()){
            emailTextView.setError("Email is required");
            emailTextView.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailTextView.setError("Enter valid email address");
            emailTextView.requestFocus();
            return;
        }
        if(password.isEmpty()){
            passwordTextView.setError("Password is required");
            passwordTextView.requestFocus();
            return;
        }
        if(password.length() < 6){
            passwordTextView.setError("Minimum Password length must be 6");
            passwordTextView.requestFocus();
            return;
        }
        if(name.isEmpty()){
            displayName.setError("Display Name is required");
            displayName.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String userid = mAuth.getCurrentUser().getUid();

                    //Save in Auth DB
                    UserProfileChangeRequest req = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                    mAuth.getCurrentUser().updateProfile(req);

                    //Save in RealTime Database
                    UserInfo user = new UserInfo(userid,name,new Timestamp(System.currentTimeMillis()).toString());
                    databaseReference.child(userid).setValue(user);

                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),"Registration Successful",Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(),OptionsActivity.class);
                    i.putExtra("name",name);
                    i.putExtra("userid",userid);
                    startActivity(i);
                }
                else {
                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                        Toast.makeText(getApplicationContext(),"User already registered",Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                }

            }
        });

    }
}

