package com.example.syncmusic;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
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
import java.text.DateFormat;
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
    public long startTime, endTime;
    private String selectedUserID;
    private Boolean proceed = true;
    private int count = 0;
    DateFormat simple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        userlist_ = new ArrayList<>();
        simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        userref = FirebaseDatabase.getInstance().getReference("ActiveUsers");
        userlistview_ = (ListView) findViewById(R.id.userlistview);
    }

    @Override
    protected void onStart() {
        super.onStart();

        userref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                startTime = System.currentTimeMillis();
                userlist_.clear();
                for (DataSnapshot user : dataSnapshot.getChildren()) {

                    ActiveUsers activeUsers = user.getValue(ActiveUsers.class);
//                    if (activeUsers.getUid().equals(selectedUserID)) {
//                        if (mediaPlayer.isPlaying()) {
//                            endTime = System.currentTimeMillis();
//                            int hostPosition = Integer.parseInt(activeUsers.getCurrentSeekTime());
//                            int ourPosition = mediaPlayer.getCurrentPosition();
//                            int s = Integer.parseInt(activeUsers.getCurrentSeekTime()) - mediaPlayer.getCurrentPosition();
//                            int delay = Integer.parseInt(String.valueOf(endTime - startTime));
//                            System.out.println(" Host :" + hostPosition);
//                            System.out.println(" Our :" + ourPosition);
//                            System.out.println(" Delay :" + delay);
//                            System.out.println(" s :" + s);
//                            if (proceed) {
//                                if(s>150) {
//                                    count++;
//                                    mediaPlayer.seekTo(Integer.parseInt(activeUsers.getCurrentSeekTime()) + s);
//                                }
//                                if(s<0){
//                                    count++;
//                                    mediaPlayer.seekTo(Integer.parseInt(activeUsers.getCurrentSeekTime()) - s);
//                                }
//                                if(count > 3){
//                                    proceed = false;
//                                }
//                            }
//                        }
//                    }
                    userlist_.add(activeUsers); //returns an object of type users rather than a generic object
                }
                UserInfoList adapter = new UserInfoList(JoinActivity.this, userlist_);
                userlistview_.setAdapter(adapter);
                userlistview_.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        count = 0;
                        proceed = true;
                        ActiveUsers selectedItem = (ActiveUsers) userlistview_.getItemAtPosition(position);
                        System.out.println("Anticipated Time to start :"+selectedItem.getAnticipatedSongStartTime());
                        System.out.println("Anticipated seek time :"+selectedItem.getAnticipatedSeekTime());
                        selectedUserID = selectedItem.getUid();
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
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(this, uri); // Set the data source of the audio
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .build());
                } else {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                }
                mediaPlayer.prepare(); // Preparing audio file, to get data like audio length etc.
                mediaPlayer.seekTo(Integer.parseInt(selectedItem.getAnticipatedSeekTime()));
                long time = simple.parse(selectedItem.getAnticipatedSongStartTime()).getTime();
                scheduleTimer(time);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Toast.makeText(getApplicationContext(), "Song ended!", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void scheduleTimer(final long t) throws IOException {
        final Timer myTimer = new Timer();
        myTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
//                String TIME_SERVER = "time-a.nist.gov";
                startTime = System.currentTimeMillis();
                String TIME_SERVER = "time.google.com";
                NTPUDPClient timeClient = new NTPUDPClient();
                InetAddress inetAddress = null;
                try {
                    inetAddress = InetAddress.getByName(TIME_SERVER);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                TimeInfo timeInfo = null;
                try {
                    timeInfo = timeClient.getTime(inetAddress);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
                endTime = System.currentTimeMillis();
                //System.out.println("Timer running..Delay =" + (endTime -startTime) );
                if (returnTime >= t) {
//                    mediaPlayer.start();
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(1.0f));
                        }
                    }
                    catch (Exception ex){
                        throw ex;
                    }
                    System.out.println("Song start time :" + simple.format(returnTime) + "Song current Duration :" + mediaPlayer.getCurrentPosition());
                    myTimer.cancel();
                }

            }
        }, 0, 10);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}
