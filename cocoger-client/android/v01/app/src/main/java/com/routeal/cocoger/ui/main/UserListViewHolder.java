package com.routeal.cocoger.ui.main;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.util.LoadImage;

import java.util.Map;

public class UserListViewHolder extends RecyclerView.ViewHolder {
    private final CheckBox mCheckbox;
    private final ImageView mPicture;
    private final TextView mName;
    private final TextView mLocation;
    private final View mView;

    public UserListViewHolder(View itemView) {
        super(itemView);

        mView = itemView;
        mCheckbox = (CheckBox) itemView.findViewById(R.id.check_user);
        mPicture = (ImageView) itemView.findViewById(R.id.picture);
        mName = (TextView) itemView.findViewById(R.id.name);
        mLocation = (TextView) itemView.findViewById(R.id.location);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckbox.setChecked(!mCheckbox.isChecked());
            }
        });
    }

    public void bind(User user, String key /* user's key */) {
        setName(user.getDisplayName());
        setPicture(user.getPicture());
        // TODO:
        //setLocation(user.getlocation());

        // disable myself, don't know how to remove myself from the searched list
        try {
            if (FB.isCurrentUser(key)) {
                mView.setBackgroundColor(mView.getResources().getColor(R.color.light_gray));
                mView.setEnabled(false);
                mView.setClickable(false);
                mCheckbox.setChecked(false);
                mCheckbox.setClickable(false);
                return;
            }
        } catch (Exception e) {
        }

        // check to see already invited
        User me = MainApplication.getUser();
        Map<String, Long> invitees = me.getInvitees();
        if (invitees != null && invitees.containsKey(key)) {
            // already invited
            mCheckbox.setChecked(true);
            return;
        }

        // check to see already being friend
        Map<String, Friend> friends = me.getFriends();
        if (friends != null && friends.containsKey(key)) {
            // already being friend
            mCheckbox.setChecked(true);
            mCheckbox.setClickable(false);
        }
    }

    private void setName(String name) {
        mName.setText(name);
    }

    private void setLocation(String location) {
        mLocation.setText(location);
    }

    private void setPicture(String url) {
        new LoadImage.LoadImageView(mPicture).execute(url);
    }
}

