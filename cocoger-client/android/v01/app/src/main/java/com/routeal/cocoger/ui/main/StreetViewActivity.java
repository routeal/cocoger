package com.routeal.cocoger.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.routeal.cocoger.R;

/**
 * Created by nabe on 7/25/17.
 */

public class StreetViewActivity extends FragmentActivity
        implements OnStreetViewPanoramaReadyCallback {

    private LatLng mLocation;
    private StreetViewPanorama mPanorama;
    private boolean mPicking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streetview);
        StreetViewPanoramaFragment streetViewPanoramaFragment =
                (StreetViewPanoramaFragment) getFragmentManager().findFragmentById(R.id.map);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);
        mLocation = getIntent().getParcelableExtra("location");

        Intent intent = getIntent();
        if (Intent.ACTION_PICK.equals(intent.getAction())) {
            //mThreadParams = MessengerUtils.getMessengerThreadParamsForIntent(intent);
            mPicking = true;

            // Note, if mThreadParams is non-null, it means the activity was launched from Messenger.
            // It will contain the metadata associated with the original content, if there was content.
        }
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
        if (mLocation != null) {
            panorama.setPosition(mLocation);
            mPanorama = panorama;
        }
    }
}
