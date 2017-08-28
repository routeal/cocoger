package com.routeal.cocoger.ui.main;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.util.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class AddFriendViewHolder extends RecyclerView.ViewHolder {
    private final CheckBox mCheckbox;
    private final ImageView mPicture;
    private final TextView mName;
    private final TextView mLocation;
    private final View mView;

    public AddFriendViewHolder(View itemView) {
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
        if (key == FirebaseAuth.getInstance().getCurrentUser().getUid()) {
            mView.setBackgroundColor(mView.getResources().getColor(R.color.light_gray));
            mView.setEnabled(false);
            mView.setClickable(false);
            mCheckbox.setChecked(false);
            mCheckbox.setClickable(false);
            return;
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
        Map<String, Long> friends = me.getFriends();
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
        Picasso.with(MainApplication.getContext())
                .load(url)
                .transform(new CircleTransform())
                .resize(48, 48)
                .into(mPicture);
    }
}

