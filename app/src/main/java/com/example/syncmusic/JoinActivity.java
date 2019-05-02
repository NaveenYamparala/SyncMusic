package com.example.syncmusic;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//Beginning of Join Activity
public class JoinActivity extends AppCompatActivity {

    DatabaseReference userref;
    private List<ActiveUsers> userlist_;
    private ListView userlistview_;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        userlist_ = new ArrayList<>();

        userref = FirebaseDatabase.getInstance().getReference("ActiveUsers");
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
                    userlist_.add(user.getValue(ActiveUsers.class)); //returns an object of type users rather than a generic object

                }
                UserInfoList adapter = new UserInfoList(JoinActivity.this, userlist_);
                userlistview_.setAdapter(adapter);
                userlistview_.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                       ActiveUsers selectedItem = (ActiveUsers) userlistview_.getItemAtPosition(position);
                        playMusic(selectedItem);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void playMusic(ActiveUsers selectedItem) {
            String fileUploadPath = selectedItem.getCurrentSong();
            if (!TextUtils.isEmpty(fileUploadPath)) {
                Uri uri = Uri.parse(fileUploadPath);
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(this, uri); // Set the data source of the audio
                    mediaPlayer.prepare(); // Preparing audio file, to get data like audio length etc.
                    mediaPlayer.seekTo(Integer.parseInt(selectedItem.getCurrentSeekTime()));
                    mediaPlayer.start();
//                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                        @Override
//                        public void onCompletion(MediaPlayer mp) {
//                            if(myTimer != null){
//                                myTimer.cancel();
//                            }
//                            playButton.setEnabled(true);
//                        }
//                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

//                scrubControl.setMax(mediaPlayer.getDuration());
//                scrubControl.setOnSeekBarChangeListener(this);
            }
        }
}
