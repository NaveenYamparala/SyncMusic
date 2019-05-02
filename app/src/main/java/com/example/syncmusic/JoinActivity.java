package com.example.syncmusic;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

//Beginning of Join Activity
public class JoinActivity extends AppCompatActivity {

    DatabaseReference userref;
    private List<UserInfo> userlist_;
    private ListView userlistview_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        userlist_ = new ArrayList<>();

        userref = FirebaseDatabase.getInstance().getReference("UserInfo");
        userlistview_ = (ListView) findViewById(R.id.userlistview);
    }

    @Override
    protected void onStart() {
        super.onStart();

        userref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                userlist_.clear();

                for(DataSnapshot user:dataSnapshot.getChildren()){
                    userlist_.add(user.getValue(UserInfo.class)); //returns an object of type users rather than a generic object

                }
                UserInfoList adapter = new UserInfoList(JoinActivity.this, userlist_);
                userlistview_.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
