package com.example.syncmusic;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;


public class UserInfoList extends ArrayAdapter<ActiveUsers> {
    private Activity context_;
    private List<ActiveUsers> userslist_;


    public UserInfoList(Activity context, List<ActiveUsers> userslist){
        super(context, R.layout.listview_item,userslist);
        this.context_ = context;
        this.userslist_ = userslist;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = context_.getLayoutInflater();
        View listviewItem = inflater.inflate(R.layout.listview_item, null, true);

        TextView DisplayName = (TextView) listviewItem.findViewById(R.id.editTextDisplayName);

        ActiveUsers tmp_ = userslist_.get(position);
        DisplayName.setText(tmp_.getDisplayName());

        return listviewItem;
    }
}

