package com.example.syncmusic;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import java.security.Timestamp;

//Making this class parceable so that i can pass this to Intent
public class UserInfo implements Parcelable {
    private String Uid;
    private String DisplayName;
    private String LastUpdated;

    public UserInfo(){

    }

    public UserInfo(String uid, String displayName, String lastUpdated) {
        Uid = uid;
        DisplayName = displayName;
        LastUpdated = lastUpdated;
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    public void setUid(String uid) {
        Uid = uid;
    }

    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }

    public void setLastUpdated(String lastUpdated) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Uid);
        dest.writeString(DisplayName);
        dest.writeString(LastUpdated);
    }

    public UserInfo(Parcel source) {
        Uid = source.readString();
        DisplayName = source.readString();
        LastUpdated = source.readString();
    }
}