package com.example.syncmusic;

import java.sql.Timestamp;

public class ActiveUsers extends UserInfo {
    public ActiveUsers(){
        super();
    }
    private String CurrentSong;
    private String CurrentSeekTime;

    public ActiveUsers(UserInfo userInfo, String currentSong, String currentSeekTime) {
        super(userInfo.getUid(),userInfo.getDisplayName(),new Timestamp(System.currentTimeMillis()).toString());
        CurrentSong = currentSong;
        CurrentSeekTime = currentSeekTime;
    }

    public ActiveUsers(String uid, String displayName, String lastUpdated, String currentSong, String currentSeekTime) {
        super(uid, displayName, lastUpdated);
        CurrentSong = currentSong;
        CurrentSeekTime = currentSeekTime;
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
