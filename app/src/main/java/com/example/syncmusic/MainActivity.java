package com.example.syncmusic;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
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
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private EditText emailTextView;
    private EditText passwordTextView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.loginButton).setOnClickListener(this);
        findViewById(R.id.signupTextView).setOnClickListener(this);
        emailTextView = (EditText) findViewById(R.id.emailEditText);
        passwordTextView = (EditText) findViewById(R.id.passwordEditText);
        progressBar = findViewById(R.id.progressBar);
        FirebaseApp.initializeApp(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            finish();
            Intent intent = new Intent(getApplicationContext(),OptionsActivity.class);
            intent.putExtra("name",currentUser.getDisplayName());
            intent.putExtra("userid",currentUser.getUid());
            startActivity(intent);
        }
    }

    private void login(){
        String email = emailTextView.getText().toString().trim();
        String password = passwordTextView.getText().toString().trim();
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
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    finish();
                    Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                    try {
                        Intent i = new Intent(MainActivity.this, OptionsActivity.class);
                        i.putExtra("name", mAuth.getCurrentUser().getDisplayName());
                        i.putExtra("userid", mAuth.getCurrentUser().getUid());
                        i.putExtra("userObj", new UserInfo(mAuth.getCurrentUser().getUid(), mAuth.getCurrentUser().getDisplayName(), null));
                        startActivity(i);
                    }
                    catch (Exception ex){
                        throw ex;
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.loginButton:
                login();
                break;
            case R.id.signupTextView:
                Intent  i = new Intent(this,SignupActivity.class);
                startActivity(i);
                break;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.signOut();
    }
}
