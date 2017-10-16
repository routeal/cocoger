package com.routeal.cocoger.ui.main;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.Place;
import com.routeal.cocoger.util.LoadImage;

/**
 * Created by hwatanabe on 10/9/17.
 */

public class PlaceListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final static String TAG = "PlaceListViewHolder";

    private View mView;
    private ImageView mPictureImage;
    private TextView mTitleText;
    private TextView mAddressText;
    private TextView mCreatorText;
    private ImageButton mEditButton;
    private ImageButton mRemoveButton;
    private Place mPlace;
    private String mKey;

    public PlaceListViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mPictureImage = (ImageView) itemView.findViewById(R.id.place_picture);
        mTitleText = (TextView) itemView.findViewById(R.id.place_title);
        mAddressText = (TextView) itemView.findViewById(R.id.place_address);
        mCreatorText = (TextView) itemView.findViewById(R.id.place_creator);
        mEditButton = (ImageButton) itemView.findViewById(R.id.place_edit);
        mRemoveButton = (ImageButton) itemView.findViewById(R.id.place_remove);

        mPictureImage.setOnClickListener(this);
        mTitleText.setOnClickListener(this);
        mAddressText.setOnClickListener(this);
        mCreatorText.setOnClickListener(this);
        mEditButton.setOnClickListener(this);
        mRemoveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mEditButton) {
            Intent intent = new Intent(FB.PLACE_EDIT);
            intent.putExtra(FB.KEY, mKey);
            intent.putExtra(FB.PLACE, mPlace);
            LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
        } else if (v == mRemoveButton) {
            Intent intent = new Intent(FB.PLACE_REMOVE);
            intent.putExtra(FB.KEY, mKey);
            intent.putExtra(FB.PLACE, mPlace);
            LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
        } else if (v == mTitleText || v == mAddressText || v == mCreatorText || v == mPictureImage) {
            Intent intent = new Intent(FB.PLACE_SHOW);
            intent.putExtra(FB.KEY, mKey);
            LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
        }
    }

    public void bind(String key, Place place) {
        mKey = key;
        mPlace = place;
        if (!mPlace.getUid().equals(FB.getUid())) {
            mEditButton.setVisibility(View.INVISIBLE);
            mRemoveButton.setVisibility(View.INVISIBLE);
            Friend friend = FB.getFriend(mPlace.getUid());
            String str = String.format(mView.getResources().getString(R.string.by_creator), friend.getDisplayName());
            mCreatorText.setText(str);
        } else {
            mEditButton.setVisibility(View.VISIBLE);
            mRemoveButton.setVisibility(View.VISIBLE);
            String str = String.format(mView.getResources().getString(R.string.by_creator), FB.getUser().getDisplayName());
            mCreatorText.setText(str);
        }
        mTitleText.setText(place.getTitle());
        mAddressText.setText(place.getAddress());
        new LoadImage(mPictureImage).loadPlace(place.getUid(), key);
    }
}
