package com.routeal.cocoger.ui.main;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.routeal.cocoger.R;

/**
 * Created by hwatanabe on 10/2/17.
 */

public class PoiInfoFragment extends InfoFragment implements View.OnClickListener {
    private String mTitle;
    private Location mLocation;
    private String mAddress;
    private InfoWindowManager mInfoWindowManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_one_info, container, false);
        setupView(view);

        mActionGoogleMapButton.setVisibility(View.VISIBLE);

        mPlaceCreatorTextView.setVisibility(View.GONE);
        mActionEditPlaceButton.setVisibility(View.GONE);
        mActionMessageButton.setVisibility(View.GONE);

        mStreetImageView.setOnClickListener(this);
        mActionAddPlaceButton.setOnClickListener(this);
        mActionDirectionButton.setOnClickListener(this);
        mActionGoogleMapButton.setOnClickListener(this);

        mTitleTextView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.peru));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mAddress != null && !mAddress.isEmpty()) {
            super.setAddress(mAddress);
        }
        if (mTitle != null && !mTitle.isEmpty()) {
            super.setTitle(mTitle);
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
            case R.id.action_add_place:
                saveLocation(mLocation, mAddress, mTitle);
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
        PoiManager.removePoiInfoWindow(mInfoWindowManager);
    }

    void setTitle(String title) {
        mTitle = title;
    }

    void setAddress(String address) {
        mAddress = address;
    }

    void setInfoWindowManager(InfoWindowManager infoWindowManager) {
        mInfoWindowManager = infoWindowManager;
    }

    Location getLocation() {
        return mLocation;
    }

    void setLocation(Location location) {
        mLocation = location;
    }
}
