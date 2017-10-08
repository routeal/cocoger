package com.routeal.cocoger.ui.main;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.routeal.cocoger.R;
import com.routeal.cocoger.service.LocationUpdateService;
import com.routeal.cocoger.util.Utils;

abstract class MapBaseActivity extends FragmentActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG = "MapBaseActivity";

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 1234;
    private final static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5678;
    private final MapActivityLifecycleCallbacks mCallbacks = new MapActivityLifecycleCallbacks();
    private GoogleApiClient mGoogleApiClient;
    private Location mLastKnownLocation;
    private Address mLastKnownAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent intent = new Intent(this, LocationUpdateService.class);
        startService(intent);
        getApplication().registerActivityLifecycleCallbacks(mCallbacks);
        checkPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        getApplication().unregisterActivityLifecycleCallbacks(mCallbacks);
        super.onDestroy();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            connectGoogleApiClient();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    connectGoogleApiClient();
                } else {
                    Drawable drawable = Utils.getIconDrawable(this, R.drawable.ic_place_white_36dp,
                            R.color.teal);
                    new AlertDialog.Builder(this)
                            .setIcon(drawable)
                            .setTitle(R.string.location_denied_title)
                            .setMessage(R.string.location_denied_content)
                            .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checkPermission();
                                }
                            })
                            .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    exitApp();
                                }
                            })
                            .show();
                }
            }
            break;
        }
    }

    private void connectGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
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
                    new AlertDialog.Builder(this)
                            .setIcon(drawable)
                            .setTitle(R.string.googleapi_denied_title)
                            .setMessage(R.string.googleapi_denied_content)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    exitApp();
                                }
                            })
                            .show();
                }
                break;
        }
    }

    void exitApp() {
        Intent intent = new Intent(this, LocationUpdateService.class);
        stopService(intent);
        finish();
    }

    abstract void startApp();

    Location getLocation() {
        return mLastKnownLocation;
    }

    void setLocation(Location location) {
        mLastKnownLocation = location;
    }

    Address getAddress() {
        return mLastKnownAddress;
    }

    void setAddress(Address address) {
        mLastKnownAddress = address;
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected(@Nullable Bundle bundle) {
        mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mGoogleApiClient.disconnect();
        startApp();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.d(TAG, "onConnectionFailed", e);
            }
        } else {
            Log.d(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
            Toast.makeText(this, connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static class MapActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        private LocationUpdateService mService = null;

        private boolean mBound = false;

        private final ServiceConnection mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LocationUpdateService.LocalBinder binder = (LocationUpdateService.LocalBinder) service;
                mService = binder.getService();
                mBound = true;
                Log.d(TAG, "onServiceConnected");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
                mBound = false;
                Log.d(TAG, "onServiceDisconnected");
            }
        };

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            activity.bindService(new Intent(activity, LocationUpdateService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (mBound) {
                // Unbind from the service. This signals to the service that this activity is no longer
                // in the foreground, and the service can respond by promoting itself to a foreground
                // service.
                activity.unbindService(mServiceConnection);
                mBound = false;
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }
}
