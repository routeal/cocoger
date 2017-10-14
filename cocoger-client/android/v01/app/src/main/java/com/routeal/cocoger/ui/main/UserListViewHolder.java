package com.routeal.cocoger.ui.main;

import android.location.Address;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

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
        mName = (TextView) itemView.findViewById(R.id.title);
        mLocation = (TextView) itemView.findViewById(R.id.location);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckbox.setChecked(!mCheckbox.isChecked());
            }
        });
    }

    private void disableInput(boolean checkbox, int color) {
        mView.setBackgroundColor(mView.getResources().getColor(color));
        mView.setEnabled(false);
        mView.setClickable(false);
        mCheckbox.setChecked(checkbox);
        mCheckbox.setClickable(false);
    }

    public void bind(User user, String key /* user's key */) {
        setName(user.getDisplayName());
        setPicture(key);

        // FIXME:
        // disable myself, don't know how to remove myself from the searched list
        if (FB.isCurrentUser(key)) {
            disableInput(false, R.color.light_gray);
            setLocation("");
            return;
        }

        // check to see already invited
        User me = FB.getUser();
        Map<String, Long> invites = me.getInvites();
        if (invites != null && invites.containsKey(key)) {
            // already invited
            disableInput(true, R.color.peachpuff);
            setLocation(mView.getResources().getString(R.string.pending));
            return;
        }

        // check to see already being friend
        Map<String, Friend> friends = me.getFriends();
        if (friends != null && friends.containsKey(key)) {
            // already being friend
            disableInput(true, R.color.teal100);
            setLocation(mView.getResources().getString(R.string.friend));
            return;
        }

        FB.getLocation(user.getLocation(), new FB.LocationListener() {
            @Override
            public void onSuccess(Location location, Address address) {
                if (address.getCountryName() != null) {
                    setLocation(address.getCountryName());
                }
            }

            @Override
            public void onFail(String err) {
            }
        });
    }

    private void setName(String name) {
        mName.setText(name);
    }

    private void setLocation(String location) {
        mLocation.setText(location);
    }

    private void setPicture(String key) {
        new LoadImage(mPicture).loadProfile(key);
    }
}

