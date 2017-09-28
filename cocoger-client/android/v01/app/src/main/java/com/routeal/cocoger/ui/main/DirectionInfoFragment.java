package com.routeal.cocoger.ui.main;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;

/**
 * Created by nabe on 9/25/17.
 */

public class DirectionInfoFragment extends Fragment {
    private String mDuration;
    private String mDistance;
    private Location mLocation;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dir_info, container, false);
        AppCompatTextView mDistanceTextView = (AppCompatTextView) view.findViewById(R.id.info_distance);
        AppCompatTextView mDurationTextView = (AppCompatTextView) view.findViewById(R.id.info_duration);
        ImageButton mActionDirectionButton = (ImageButton) view.findViewById(R.id.action_direction);
        ImageButton mActionRemoveButton = (ImageButton) view.findViewById(R.id.action_remove);
        mActionDirectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNavigation();
            }
        });
        mActionRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDirection();
            }
        });
        mDurationTextView.setText(mDuration);
        mDistanceTextView.setText(mDistance);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    void setDuration(String duration) {
        mDuration = duration;
    }

    void setDistance(String distance) {
        mDistance = distance;
    }

    void setDestination(Location location) {
        mLocation = location;
    }

    void startNavigation() {
        String url = String.format("google.navigation:q=%s,%s",
                mLocation.getLatitude(), mLocation.getLongitude());
        Uri gmmIntentUri = Uri.parse(url);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        MainApplication.getContext().startActivity(mapIntent);
    }

    void removeDirection() {
        Intent intent = new Intent(MapActivity.DIRECTION_ROUTE_REMOVE);
        LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
    }

}