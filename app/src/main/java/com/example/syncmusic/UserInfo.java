package com.example.syncmusic;

import android.support.annotation.Nullable;
import java.security.Timestamp;

public class UserInfo {
    private String Uid;
    private String DisplayName;
    private String LastUpdated;

    public UserInfo(String uid, String displayName, String lastUpdated) {
        Uid = uid;
        DisplayName = displayName;
        LastUpdated = lastUpdated;
    }

    public String getUid() {
        return Uid;
    }

    public String getDisplayName() {
        return DisplayName;
    }


    public String getLastUpdated() {
        return LastUpdated;
    }
}