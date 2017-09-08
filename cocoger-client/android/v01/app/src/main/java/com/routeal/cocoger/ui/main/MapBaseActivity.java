package com.routeal.cocoger.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.routeal.cocoger.R;
import com.routeal.cocoger.service.MainService;
import com.routeal.cocoger.util.AppVisibilityDetector;
import com.routeal.cocoger.util.Utils;

import java.util.List;
import java.util.Locale;

public abstract class MapBaseActivity extends FragmentActivity {

    private final static String TAG = "MapBaseActivity";

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1000;

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        // set a listener to detect running in background or foreground
        AppVisibilityDetector.init(getApplication(), new AppVisibilityDetector.AppVisibilityCallback() {
            @Override
            public void onAppGotoForeground() {
                //app is from background to foreground
                MainService.setForegroundMode();
            }

            @Override
            public void onAppGotoBackground() {
                //app is from foreground to background
                MainService.setBackgroundMode();
            }
        });

        checkPermission();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            connectGoogleApiClient();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    connectGoogleApiClient();
                } else {
                    Drawable drawable = Utils.getIconDrawable(this, R.drawable.ic_place_white_36dp,
                            R.color.teal);

                    new MaterialDialog.Builder(this)
                            .icon(drawable)
                            .limitIconToDefaultSize()
                            .title(R.string.location_denied_title)
                            .content(R.string.location_denied_content)
                            .positiveText(R.string.try_again)
                            .negativeText(R.string.exit)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog,
                                                    @NonNull DialogAction which) {
                                    checkPermission();
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog,
                                                    @NonNull DialogAction which) {
                                    finish();
                                }
                            })
                            .show();
                }
            }
            break;
        }
    }

    class GoogleApiClientListeners implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {
        Activity activity;

        GoogleApiClientListeners(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            startApp();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            if (connectionResult.hasResolution()) {
                try {
                    // Start an Activity that tries to resolve the error
                    connectionResult.startResolutionForResult(activity,
                            CONNECTION_FAILURE_RESOLUTION_REQUEST);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "GoogleApi connection failed without resolution.");
                Toast.makeText(activity, connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Connects with Google API client
     */
    private void connectGoogleApiClient() {
        GoogleApiClientListeners listeners = new GoogleApiClientListeners(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(listeners)
                .addConnectionCallbacks(listeners)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    connectGoogleApiClient();
                } else {
                    Drawable drawable = Utils.getIconDrawable(this, R.drawable.ic_place_white_36dp,
                            R.color.teal);

                    new MaterialDialog.Builder(this)
                            .icon(drawable)
                            .limitIconToDefaultSize()
                            .title(R.string.googleapi_denied_title)
                            .content(R.string.googleapi_denied_content)
                            .positiveText(android.R.string.ok)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog,
                                                    @NonNull DialogAction which) {
                                    finish();
                                }
                            })
                            .show();
                }
                break;
        }
    }

    // start an app in the extented object
    abstract void startApp();

    @SuppressWarnings("MissingPermission")
    protected Location getDeviceLocation() {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        /*
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        */
        return location;
    }

    protected Address getAddress(Location location) {
        Address address = null;
        try {
            List<Address> addresses = new Geocoder(this, Locale.getDefault()).getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
            address = addresses.get(0);
        } catch (Exception e) {
            Log.d(TAG, "Geocoder failed:" + e.getLocalizedMessage());
        }
        return address;
    }
}
