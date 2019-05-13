package com.example.syncmusic;

import android.content.Intent;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

public class HostActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private Button playButton, pauseButton, uploadButton, loadButton;
    private SeekBar volumeSeekBar, scrubControl;
    private StorageReference mStorageRef, fileRef;
    private int Music_Choose = 1;
    Uri musicURI;
    UserInfo currentUser;
    private String fileName, fileUploadPath, uid, linkUrl;
    InputStream stream;
    private ProgressBar progressBar;
    private DatabaseReference activeUsersReference;
    Boolean IsBackButtonPressed = false;
    private EditText linkEditText;
    private boolean updateDB;
    private Thread t;
    DateFormat simple;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        activeUsersReference = FirebaseDatabase.getInstance().getReference("ActiveUsers");
        currentUser = (UserInfo) getIntent().getExtras().get("userObj");
        playButton = findViewById(R.id.PlayButton);
        pauseButton = findViewById(R.id.PauseButton);
        uploadButton = findViewById(R.id.UploadButton);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        playButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
        uploadButton.setOnClickListener(this);
        linkEditText = findViewById(R.id.linkEditText);
        loadButton = findViewById(R.id.loadButton);
        loadButton.setOnClickListener(this);

        volumeSeekBar = findViewById(R.id.seekBar);
        scrubControl = findViewById(R.id.scrubber);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE); // Registering audio manager service
        volumeSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));// To get full volume of the device
        volumeSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)); // To get current volume set on the device
        volumeSeekBar.setOnSeekBarChangeListener(this);

        //get values from intent
        uid = getIntent().getExtras().getString("userid");
        simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.PlayButton:
                if (currentUser != null) {
                    updateDB = true;
                    mediaPlayer.start();
//                    try {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                            mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(1.0f));
//                        }
//                    }
//                    catch (Exception ex){
//                        throw ex;
//                    }
                    UpdateDatabase();
                    playButton.setEnabled(false);
                    pauseButton.setEnabled(true);
                }
                break;
            case R.id.PauseButton:
                updateDB = false;
                mediaPlayer.pause();
                pauseButton.setEnabled(false);
                playButton.setEnabled(true);
                break;
            case R.id.UploadButton:
                uploadMusic();
                break;
            case R.id.loadButton:
                if(mediaPlayer != null){
                    mediaPlayer.stop();
                }
                if(t != null && t.isAlive()){
                    updateDB = false;
                }
                progressBar.setVisibility(View.VISIBLE);
                linkUrl = linkEditText.getText().toString();
                if (!TextUtils.isEmpty(linkUrl)) {
                    fileUploadPath = linkUrl;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        prepareMusicPlayer(fileUploadPath);
                    }
                }
                break;
        }
    }


    private void uploadMusic() {
        if(mediaPlayer != null){
            mediaPlayer.stop();
        }
        if(t != null && t.isAlive()){
            updateDB = false;
        }
        Intent myIntent = new Intent(Intent.ACTION_GET_CONTENT);
        myIntent.setType("audio/*");
        startActivityForResult(myIntent, Music_Choose);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null){
            return;
        }
        if (requestCode == Music_Choose && resultCode == RESULT_OK) {
            musicURI = data.getData();
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
        progressBar.setVisibility(View.VISIBLE);
        /*Start - Code to get file name*/
        File myFile = new File(musicURI.toString());
        if (musicURI.toString().startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(musicURI, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        } else if (musicURI.toString().startsWith("file://")) {
            fileName = myFile.getName();
        }
        /*End - Code to get file name*/

        /*Start - File Upload*/
        if (fileName != null) {
            try {
                stream = null;
                stream = getContentResolver().openInputStream(musicURI);
                //String mimeType = getContentResolver().getType(musicURI);
                // String fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                fileRef = mStorageRef.child(uid + "/" + fileName);
            } catch (Exception e) {
                e.getMessage();
            }
            UploadTask uploadTask = fileRef.putStream(stream);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(HostActivity.this, "Upload failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    long bytes = taskSnapshot.getMetadata().getSizeBytes();
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            fileUploadPath = uri.toString();
                            Toast.makeText(HostActivity.this, "Upload Successful", Toast.LENGTH_LONG).show();
                            //progressBar.setVisibility(View.GONE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                prepareMusicPlayer(fileUploadPath);
                            }
                        }
                    });
                }
            });
        } else {
            return;
        }
        /*End - File Upload*/
    }


    public void prepareMusicPlayer(String fileUploadPath) {
        if (!TextUtils.isEmpty(fileUploadPath)) {
            Uri uri = Uri.parse(fileUploadPath);
            mediaPlayer = new MediaPlayer();
            playButton.setEnabled(false);
            pauseButton.setEnabled(false);
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
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        progressBar.setVisibility(View.GONE);
                        playButton.setEnabled(true);
                        scrubControl.setMax(mediaPlayer.getDuration());
                        scrubControl.setOnSeekBarChangeListener(HostActivity.this);

                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        System.out.println(" Song Ending Time :" + System.currentTimeMillis());
                        updateDB = false;
                        playButton.setEnabled(true);

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    // To update DB as well as seekbar continously about the status of the song.
    public void UpdateDatabase() {
        t = new Thread(String.valueOf(Thread.MAX_PRIORITY)) {
            @Override
            public void run() {
                super.run();
                while (updateDB) {
                    long returnTime = 0;
                    String TIME_SERVER = "time.google.com";
                    NTPUDPClient timeClient = new NTPUDPClient();
                    try {
                        InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
                        TimeInfo timeInfo = null;
                        timeInfo = timeClient.getTime(inetAddress);
                        returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
                        System.out.println("System Time: " + System.currentTimeMillis());
                        System.out.println("Song current seek time: "+ mediaPlayer.getCurrentPosition());
                        System.out.println("Estimated song time: "+ String.valueOf(mediaPlayer.getCurrentPosition() + 2000));
                        System.out.println("Estimated Time to start: " + simple.format(new Date(returnTime + 2000)));
                        System.out.println("Song Duration: " + mediaPlayer.getDuration());

                        long st = System.currentTimeMillis();

                        ActiveUsers activeUsers = new ActiveUsers(new UserInfo(currentUser.getUid(), currentUser.getDisplayName(), currentUser.getLastUpdated()), fileUploadPath, String.valueOf(mediaPlayer.getCurrentPosition()),simple.format(new Date(returnTime+2000)),String.valueOf(mediaPlayer.getCurrentPosition() + 2000));
                        activeUsersReference.child(uid).setValue(activeUsers);

                        long e = System.currentTimeMillis();
                        System.out.println(" DB Time : " + (e - st));
                        scrubControl.setProgress(mediaPlayer.getCurrentPosition());
                        Thread.sleep(1000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }

    /*Start - Override methods of Volume bar and Scrubber*/
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.seekBar:
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                break;
            case R.id.scrubber:
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
                break;
        }

    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
    /*End - Override methods of Volume bar and Scrubber*/

    @Override
    public void onBackPressed() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            activeUsersReference.child(uid).removeValue();
            updateDB = false;
        }
        IsBackButtonPressed = true;
        // moveTaskToBack(true);
        super.onBackPressed();  // optional depending on your needs

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(t != null && t.isAlive()) {
            updateDB = false;
        }
    }

}
