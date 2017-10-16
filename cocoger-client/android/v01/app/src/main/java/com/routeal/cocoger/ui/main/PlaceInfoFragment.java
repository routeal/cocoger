package com.routeal.cocoger.ui.main;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.Marker;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.Place;
import com.routeal.cocoger.util.LoadImage;

/**
 * Created by hwatanabe on 10/8/17.
 */

public class PlaceInfoFragment extends InfoFragment implements View.OnClickListener {
    private String mTitle;
    private String mAddress;
    private String mPlaceCreator;
    private String mDescription;
    private String mColor;
    private String mPictureUrl;
    private Location mLocation;
    private Bitmap mCopiedBitmap;
    private PlaceManager mPlaceManager;
    private boolean mSeenFriend;
    private Place mPlace;
    private String mKey;

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
        if (mPlace.getDescription() != null) {
            super.setAddress(mPlace.getDescription());
        } else {
            if (mPlace.getAddress() != null) {
                super.setAddress(mPlace.getAddress());
            }
        }
        if (mPlace.getTitle() != null) {
            super.setTitle(mPlace.getTitle());
        }
        if (mCopiedBitmap != null) {
            mStreetImageView.setImageBitmap(mCopiedBitmap);
        }

        if (mPlace.getUid() != null) {
            new LoadImage(mStreetImageView).loadPlace(mPlace.getUid(), mKey);

            if (FB.getUid().equals(mPlace.getUid())) {
                String str = String.format(view.getResources().getString(R.string.by_creator), FB.getUser().getDisplayName());
                mPlaceCreatorTextView.setText(str);
            } else {
                Friend friend = FB.getFriend(mPlace.getUid());
                if (friend != null) {
                    String str = String.format(view.getResources().getString(R.string.by_creator), friend.getDisplayName());
                    mPlaceCreatorTextView.setText(str);
                }
            }
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
                mPlaceManager.editPlace(getActivity(), mPlace, mKey);
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

    Place getPlace() {
        return mPlace;
    }

    void setPlace(Place place) {
        mPlace = place;
    }

    void setStreetViewPicture(Bitmap bitmap) {
        mCopiedBitmap = bitmap;
    }

    void setPlaceManager(PlaceManager placeManager) {
        mPlaceManager = placeManager;
    }

    void setKey(String key) { mKey = key; }

    String getKey() {
        return mKey;
    }
}

