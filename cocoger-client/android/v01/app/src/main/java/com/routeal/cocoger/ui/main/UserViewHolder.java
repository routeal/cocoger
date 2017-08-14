package com.routeal.cocoger.ui.main;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.util.CircleTransform;
import com.squareup.picasso.Picasso;

public class UserViewHolder extends RecyclerView.ViewHolder {
    private final CheckBox mCheckbox;
    private final ImageView mPicture;
    private final TextView mName;
    private final TextView mLocation;

    public UserViewHolder(View itemView) {
        super(itemView);

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

    public void bind(User user) {
        setName(user.getName());
        setPicture(user.getPicture());
        // TODO:
        //setLocation(user.getlocation());
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

