package com.example.syncmusic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;

public class OptionsActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private Button hostButton,joinButton, signoutButton;
    private String uid;
    private Object userObj = null;
    private ImageView LoginBackground;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        mAuth = FirebaseAuth.getInstance();
        hostButton = findViewById(R.id.hostButton);
        joinButton = findViewById(R.id.joinButton);
        signoutButton = findViewById(R.id.signoutButton);
        LoginBackground = findViewById(R.id.LoginBackground);

        /*Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.background_two);
        LoginBackground.setImageBitmap(bitmap);*/

        uid = getIntent().getExtras().getString("userid");
        userObj = getIntent().getExtras().get("userObj");
        hostButton.setOnClickListener(this);
        joinButton.setOnClickListener(this);
        signoutButton.setOnClickListener(this);
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
                i.putExtra("userObj", (Parcelable) userObj);
                startActivity(i);
                break;
            case R.id.joinButton:
                Intent intent = new Intent(getApplicationContext(),JoinActivity.class);
                startActivity(intent);
                break;
            case R.id.signoutButton:
                mAuth.signOut();
                finish();
                startActivity(new Intent(this,MainActivity.class));
                break;
        }
    }
}
