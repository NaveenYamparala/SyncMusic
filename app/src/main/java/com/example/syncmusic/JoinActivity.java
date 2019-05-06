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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//Beginning of Join Activity
public class JoinActivity extends AppCompatActivity {

    DatabaseReference userref;
    private List<ActiveUsers> userlist_;
    private ListView userlistview_;
    MediaPlayer mediaPlayer;
    public long startTime,endTime;

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
                        startTime = System.currentTimeMillis();
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
                if(mediaPlayer != null && mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                }
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(this, uri); // Set the data source of the audio
                    mediaPlayer.prepare(); // Preparing audio file, to get data like audio length etc.
                    endTime = System.currentTimeMillis();
                    long delay = endTime - startTime;
                    int anticipatedSeek = Integer.parseInt(selectedItem.getCurrentSeekTime()) + 2600;
                    if(mediaPlayer.getDuration() > anticipatedSeek) {
                        mediaPlayer.seekTo(anticipatedSeek);
                        long time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(selectedItem.getLastUpdated()).getTime() + 2000;
                        scheduleTimer(time);
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Song ended!",Toast.LENGTH_LONG).show();
                        return;
                    }

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
                } catch (ParseException e) {
                    e.printStackTrace();
                }

//                scrubControl.setMax(mediaPlayer.getDuration());
//                scrubControl.setOnSeekBarChangeListener(this);
            }
        }

    public void scheduleTimer(final long t) throws IOException {
        final Timer myTimer = new Timer();
        myTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String TIME_SERVER = "time-a.nist.gov";
                NTPUDPClient timeClient = new NTPUDPClient();
                try {
                    InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
                    TimeInfo timeInfo = null;
                    timeInfo = timeClient.getTime(inetAddress);
                    long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
                    if(returnTime >= t) {
                        mediaPlayer.start();
                        if(!mediaPlayer.isPlaying()){
                            mediaPlayer.start();
                        }
                        myTimer.cancel();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 10);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
    }
}
