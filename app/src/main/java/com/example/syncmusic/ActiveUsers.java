package com.example.syncmusic;

import java.sql.Timestamp;

public class ActiveUsers extends UserInfo {
    public ActiveUsers(){
        super();
    }
    private String CurrentSong;
    private String CurrentSeekTime;
    private String AnticipatedSongStartTime;
    private String AnticipatedSeekTime;


    public ActiveUsers(UserInfo userInfo, String currentSong, String currentSeekTime, String anticipatedSongStartTime, String anticipatedSeekTime) {
        super(userInfo.getUid(),userInfo.getDisplayName(),userInfo.getLastUpdated());
        CurrentSong = currentSong;
        CurrentSeekTime = currentSeekTime;
        AnticipatedSongStartTime = anticipatedSongStartTime;
        AnticipatedSeekTime = anticipatedSeekTime;
    }

    public ActiveUsers(String uid, String displayName, String lastUpdated, String currentSong, String currentSeekTime) {
        super(uid, displayName, lastUpdated);
        CurrentSong = currentSong;
        CurrentSeekTime = currentSeekTime;
    }


    public String getAnticipatedSongStartTime() {
        return AnticipatedSongStartTime;
    }

    public void setAnticipatedSongStartTime(String anticipatedSongStartTime) {
        AnticipatedSongStartTime = anticipatedSongStartTime;
    }

    public String getAnticipatedSeekTime() {
        return AnticipatedSeekTime;
    }

    public void setAnticipatedSeekTime(String anticipatedSeekTime) {
        AnticipatedSeekTime = anticipatedSeekTime;
    }

    public String getCurrentSong() {
        return CurrentSong;
    }

    public String getCurrentSeekTime() {
        return CurrentSeekTime;
    }

    public void setCurrentSong(String currentSong) {
        CurrentSong = currentSong;
    }

    public void setCurrentSeekTime(String currentSeekTime) {
        CurrentSeekTime = currentSeekTime;
    }
}
