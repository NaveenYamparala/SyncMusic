package com.example.syncmusic;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.util.List;


public class UserInfoList extends ArrayAdapter<UserInfo> {
    private Activity context_;
    private List<UserInfo> userslist_;


    public UserInfoList(Activity context, List<UserInfo> userslist){
        super(context, R.layout.listview_item,userslist);
        this.context_ = context;
        this.userslist_ = userslist;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = context_.getLayoutInflater();
        View listviewItem = inflater.inflate(R.layout.listview_item, null, true);

        EditText Uid = (EditText) listviewItem.findViewById(R.id.editTextUid);
        EditText DisplayName = (EditText) listviewItem.findViewById(R.id.editTextDisplayName);
        EditText LastUpdated = (EditText) listviewItem.findViewById(R.id.editTextLastUpdated);

        UserInfo tmp_ = userslist_.get(position);
        Uid.setText(tmp_.getUid());
        DisplayName.setText(tmp_.getDisplayName());
        LastUpdated.setText(tmp_.getLastUpdated());

        return listviewItem;
    }
}

/*public class UserInfoList extends ArrayAdapter<UserInfo> {
    private Activity context_;
    private List<UserInfo> userslist_;

    public UserInfolist(Activity context, List<UserInfo> userslist){
        super(context, R.layout.listview_item,userslist);
        this.context_ = context;
        this.userslist_ = userslist;

    }

}*/
