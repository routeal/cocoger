package com.routeal.cocoger.ui.main;

import android.content.Intent;
import android.graphics.drawable.Drawable;
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

import com.google.android.gms.maps.model.LatLng;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.util.Utils;

/**
 * Created by nabe on 9/25/17.
 */

public class DirectionInfoFragment extends Fragment {
    private String mDuration;
    private String mDistance;
    private LatLng mLocation;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dir_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatTextView mDistanceTextView = (AppCompatTextView) view.findViewById(R.id.info_distance);
        AppCompatTextView mDurationTextView = (AppCompatTextView) view.findViewById(R.id.info_duration);
        ImageButton mActionDirectionButton = (ImageButton) view.findViewById(R.id.action_direction);
        ImageButton mActionRemoveButton = (ImageButton) view.findViewById(R.id.action_remove);
        Drawable drawable = Utils.getIconDrawable(view.getContext(), R.drawable.ic_directions_white_24dp, R.color.dodgerblue);
        mActionDirectionButton.setImageDrawable(drawable);
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
        if (mDuration != null) {
            mDurationTextView.setText(mDuration);
        }
        if (mDistance != null) {
            mDistanceTextView.setText(mDistance);
        }
    }

    void setDuration(String duration) {
        mDuration = duration;
    }

    void setDistance(String distance) {
        mDistance = distance;
    }

    void setDestination(LatLng location) {
        mLocation = location;
    }

    void startNavigation() {
        String url = String.format("google.navigation:q=%s,%s",
                mLocation.latitude, mLocation.longitude);
        Uri gmmIntentUri = Uri.parse(url);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        MainApplication.getContext().startActivity(mapIntent);
    }

    void removeDirection() {
        Intent intent = new Intent(FB.DIRECTION_ROUTE_REMOVE);
        LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
    }

}
