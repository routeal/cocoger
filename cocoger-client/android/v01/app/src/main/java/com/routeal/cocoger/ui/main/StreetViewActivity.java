package com.routeal.cocoger.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.routeal.cocoger.R;
import com.routeal.cocoger.util.Utils;

import java.io.File;

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

        /*
        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.send_facebook);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImage();
            }
        });
        */
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
        if (mLocation != null) {
            panorama.setPosition(mLocation);
            mPanorama = panorama;
        }
    }

    void sendFacebook(String filename) {
        // doesn't work with this uri, if it's replaced with the FS sample uri, it works fine.
        Uri uri = Uri.fromFile(new File(filename));
/*
        ShareToMessengerParams shareToMessengerParams =
                ShareToMessengerParams.newBuilder(uri, "image/png")
                        .setMetaData("{ \"image\" : \"streetview\" }")
                        .build();
        if (mPicking) {
            MessengerUtils.finishShareToMessenger(StreetViewActivity.this, shareToMessengerParams);
        } else {
            MessengerUtils.shareToMessenger(StreetViewActivity.this, 1, shareToMessengerParams);
        }
*/
    }

    void sendImage() {
        if (mPanorama == null) {
            return;
        }

        String url = String.format(getResources().getString(R.string.street_view_image_url),
                mPanorama.getLocation().position.latitude,
                mPanorama.getLocation().position.longitude);

        Utils.downloadImage(getApplicationContext(), url, new Utils.ImageDownloadListener() {
            @Override
            public void onDownloaded(final String result) {
                if (result != null) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            sendFacebook(result);
                        }
                    });

                }
            }
        });
    }
}
