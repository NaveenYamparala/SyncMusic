package com.example.syncmusic;

import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class HostActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private Button playButton, pauseButton, uploadButton;
    private SeekBar volumeSeekBar, scrubControl;
    private StorageReference mStorageRef,fileRef;
    private int Music_Choose = 1;
    Uri musicURI;
    private FirebaseAuth mAuth;
    private String fileName,fileUploadPath,uid;
    InputStream stream;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        mAuth = FirebaseAuth.getInstance();
        playButton = findViewById(R.id.PlayButton);
        pauseButton = findViewById(R.id.PauseButton);
        uploadButton = findViewById(R.id.UploadButton);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        playButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
        uploadButton.setOnClickListener(this);


        volumeSeekBar = findViewById(R.id.seekBar);
        scrubControl = findViewById(R.id.scrubber);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE); // Registering audio manager service
        volumeSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));// To get full volume of the device
        volumeSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)); // To get current volume set on the device
        volumeSeekBar.setOnSeekBarChangeListener(this);

        //get values from intent
        Intent intent = new Intent();
        uid = getIntent().getExtras().getString("userid");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.PlayButton:
                mediaPlayer.start();
                break;
            case R.id.PauseButton:
                mediaPlayer.pause();
                break;
            case R.id.UploadButton:
                uploadMusic();
                break;
        }
    }


    private void uploadMusic() {
        Intent myIntent = new Intent(Intent.ACTION_GET_CONTENT);
        myIntent.setType("audio/*");
        startActivityForResult(myIntent,Music_Choose);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Music_Choose && resultCode == RESULT_OK){
            musicURI = data.getData();
            if(mediaPlayer != null && mediaPlayer.isPlaying()) {
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
        if(fileName != null) {
            try {
                stream = null;
                stream = getContentResolver().openInputStream(musicURI);
                String mimeType = getContentResolver().getType(musicURI);
               // String fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                fileRef = mStorageRef.child( uid+ "/" +fileName);
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
                            progressBar.setVisibility(View.GONE);
                            prepareMusicPlayer();
                        }
                    });
                    Toast.makeText(HostActivity.this, "Upload Successful", Toast.LENGTH_LONG).show();
                    playButton.setEnabled(true);
                    pauseButton.setEnabled(true);
                }
            });
        }
        else{
            return;
        }
        /*End - File Upload*/
    }


    public void prepareMusicPlayer() {
        if (!TextUtils.isEmpty(fileUploadPath)) {
            Uri uri = Uri.parse(fileUploadPath);
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(this, uri); // Set the data source of the audio
                mediaPlayer.prepare(); // Preparing audio file, to get data like audio length etc.
            } catch (IOException e) {
                e.printStackTrace();
            }

            scrubControl.setMax(mediaPlayer.getDuration());
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    scrubControl.setProgress(mediaPlayer.getCurrentPosition());
                }
            }, 0, 1000);

            scrubControl.setOnSeekBarChangeListener(this);
        }
    }

    /* Override methods of Volume bar and Scrubber*/
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()){
            case R.id.seekBar:
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
                break;
            case R.id.scrubber:
                if(fromUser){
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

    @Override
    protected void onStop() {
        if(mediaPlayer!=null){
            mediaPlayer.pause();
        }

        super.onStop();
    }
}
