package com.routeal.cocoger.ui.main;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.routeal.cocoger.R;

/**
 * Created by hwatanabe on 10/8/17.
 */

public class PlaceInfoFragment extends InfoFragment implements View.OnClickListener {
    private String mTitle;
    private String mAddress;
    private String mPlaceCreator;
    private String mDescription;
    private String mColor;
    private Location mLocation;
    private Bitmap mCopiedBitmap;
    private PlaceManager mPlaceManager;
    private boolean mSeenFriend;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_one_info, container, false);
        setupView(view);

        mActionAddPlaceButton.setVisibility(View.GONE);

        mPlaceCreatorTextView.setVisibility(View.VISIBLE);
        mActionEditPlaceButton.setVisibility(View.VISIBLE);
        mActionGoogleMapButton.setVisibility(View.VISIBLE);

        mActionMessageButton.setVisibility(View.GONE);

        mStreetImageView.setOnClickListener(this);
        mActionEditPlaceButton.setOnClickListener(this);
        mActionDirectionButton.setOnClickListener(this);
        mActionGoogleMapButton.setOnClickListener(this);

        mTitleTextView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.peru));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mDescription != null && !mDescription.isEmpty()) {
            super.setAddress(mDescription);
        } else {
            if (mAddress != null && !mAddress.isEmpty()) {
                super.setAddress(mAddress);
            }
        }
        if (mTitle != null && !mTitle.isEmpty()) {
            super.setTitle(mTitle);
        }
        if (mPlaceCreator != null && !mPlaceCreator.isEmpty()) {
            mPlaceCreatorTextView.setText(mPlaceCreator);
        }
        if (mCopiedBitmap != null) {
            mStreetImageView.setImageBitmap(mCopiedBitmap);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.info_street_view:
                if (mLocation != null && mTitle != null) {
                    openStreetView(mLocation, mTitle);
                }
                break;
            case R.id.action_edit_place:
                mPlaceManager.editPlace(this, mTitle, mLocation, mAddress, mDescription,
                        mSeenFriend, mCopiedBitmap, mColor);
                break;
            case R.id.action_direction:
                if (mLocation != null) {
                    showDirection(mLocation);
                }
                break;
            case R.id.action_googlemap:
                showGoogleMap(mLocation, mTitle);
                break;
        }
        mPlaceManager.hideInfoWindow(this);
    }

    void setTitle(String title) {
        mTitle = title;
    }

    void setDescription(String description) {
        mDescription = description;
    }

    void setAddress(String address) {
        mAddress = address;
    }

    void setPlaceManager(PlaceManager placeManager) {
        mPlaceManager = placeManager;
    }

    Location getLocation() {
        return mLocation;
    }

    void setLocation(Location location) {
        mLocation = location;
    }

    void setPlaceCreator(String creator) {
        mPlaceCreator = creator;
    }

    void setStreetViewPicture(Bitmap bitmap) {
        mCopiedBitmap = bitmap;
    }

    String getColor() {
        return mColor;
    }

    void setColor(String color) {
        mColor = color;
    }

    void setSeenFriend(boolean seenFriend) {
        mSeenFriend = seenFriend;
    }
}
