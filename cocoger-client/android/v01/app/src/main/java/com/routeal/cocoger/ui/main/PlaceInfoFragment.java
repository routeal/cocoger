package com.routeal.cocoger.ui.main;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.manager.FriendManager;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.Place;
import com.routeal.cocoger.util.LoadImage;

import java.util.HashMap;

/**
 * Created by hwatanabe on 10/8/17.
 */

public class PlaceInfoFragment extends InfoFragment implements View.OnClickListener {
    private Bitmap mCopiedBitmap;
    private Place mPlace;
    private String mKey;
    private PlaceMarkers mPlaceMarkers;

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

        return view;
    }

    private final HashMap<String, Integer> mBackgroundColor = new HashMap<String, Integer>() {{
        put("light_blue_400", R.color.light_blue_400);
        put("red_700", R.color.red_700);
        put("teal_400", R.color.teal_400);
        put("amber_400", R.color.amber_400);
        put("pink_400", R.color.pink_400);
    }};

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mPlace.getDescription() != null && !mPlace.getDescription().isEmpty()) {
            super.setAddress(mPlace.getDescription());
        } else {
            if (mPlace.getAddress() != null && !mPlace.getAddress().isEmpty()) {
                super.setAddress(mPlace.getAddress());
            }
        }
        if (mPlace.getTitle() != null && !mPlace.getTitle().isEmpty()) {
            super.setTitle(mPlace.getTitle());
        }
        if (mPlace.getMarkerColor() != null && !mPlace.getMarkerColor().isEmpty()) {
            int colorId = mBackgroundColor.get(mPlace.getMarkerColor());
            mTitleTextView.setBackgroundColor(ContextCompat.getColor(getContext(), colorId));
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
                Friend friend = FriendManager.getFriend(mPlace.getUid());
                if (friend != null) {
                    String str = String.format(view.getResources().getString(R.string.by_creator), friend.getDisplayName());
                    mPlaceCreatorTextView.setText(str);
                }
                mActionEditPlaceButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        mPlaceMarkers.hideInfoWindow(this);
        switch (v.getId()) {
            case R.id.info_street_view:
                if (mLoadImage != null) mLoadImage.cancel();
                openStreetView(new LatLng(mPlace.getLatitude(), mPlace.getLongitude()), mPlace.getTitle());
                break;
            case R.id.action_edit_place:
                if (mLoadImage != null) mLoadImage.cancel();
                mPlaceMarkers.updatePlace(mKey, mPlace);
                break;
            case R.id.action_direction:
                if (mLoadImage != null) mLoadImage.cancel();
                showDirection(new LatLng(mPlace.getLatitude(), mPlace.getLongitude()));
                break;
            case R.id.action_googlemap:
                if (mLoadImage != null) mLoadImage.cancel();
                showGoogleMap(new LatLng(mPlace.getLatitude(), mPlace.getLongitude()), mPlace.getTitle());
                break;
        }
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

    void setPlaceMarkers(PlaceMarkers placeMarkers) {
        mPlaceMarkers = placeMarkers;
    }

    String getKey() {
        return mKey;
    }

    void setKey(String key) {
        mKey = key;
    }
}

