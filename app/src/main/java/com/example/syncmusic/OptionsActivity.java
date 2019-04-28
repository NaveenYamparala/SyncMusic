package com.example.syncmusic;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class OptionsActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private Button hostButton,joinButton;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        mAuth = FirebaseAuth.getInstance();
        hostButton = findViewById(R.id.hostButton);
        joinButton = findViewById(R.id.joinButton);

        Intent i = new Intent();
        uid = getIntent().getExtras().getString("userid");
        hostButton.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.signOut();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.hostButton:
                Intent i = new Intent(getApplicationContext(),HostActivity.class);
                i.putExtra("userid",uid);
                startActivity(i);
                break;
            case R.id.joinButton:
//                Intent intent = new Intent(getApplicationContext(),HostActivity.class);
//                startActivity(intent);
                break;
        }
    }
}
