package com.routeal.cocoger.ui.main;

import android.content.DialogInterface;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.provider.DBUtil;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.RangeSeekBar;
import com.routeal.cocoger.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by nabe on 9/16/17.
 */

public class TimelineActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final static String TAG = "TimelineActivity";

    private Date mDate;
    private int mStartTime;
    private int mEndTime;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(true);
            ab.setTitle(R.string.sharing_timeline);
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        selectDateTime();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save:
                return true;
            case R.id.action_date_change:
                selectDateTime();
                return true;
            default:
                return false;
        }
    }

    private void selectDateTime() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(TimelineActivity.this);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_datetime, null);
        final EditText dateText = (EditText) view.findViewById(R.id.dialog_date);
        final EditText timeText = (EditText) view.findViewById(R.id.dialog_time);

        mDate = null;
        mStartTime = 0;
        mEndTime = 0;

        new AlertDialog.Builder(TimelineActivity.this)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mDate == null || mEndTime == 0) {
                            return;
                        }
                        showTimeline();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(TimelineActivity.this);
                View childView = layoutInflaterAndroid.inflate(R.layout.dialog_date, null);
                final DatePicker datePicker = (DatePicker) childView.findViewById(R.id.datePicker);
                new AlertDialog.Builder(TimelineActivity.this)
                        .setView(childView)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int day = datePicker.getDayOfMonth();
                                int month = datePicker.getMonth();
                                int year = datePicker.getYear();
                                SimpleDateFormat sdf = new SimpleDateFormat("MM dd yyyy");
                                mDate = new Date(year - 1900, month, day, 0, 0);
                                String formatDate = sdf.format(mDate);
                                dateText.setText(formatDate);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });

        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(TimelineActivity.this);
                View childView = layoutInflaterAndroid.inflate(R.layout.dialog_time, null);
                final RangeSeekBar seekBar = (RangeSeekBar) childView.findViewById(R.id.seekbar);
                new AlertDialog.Builder(TimelineActivity.this)
                        .setView(childView)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mStartTime = seekBar.getSelectedMinValue().intValue();
                                mEndTime = seekBar.getSelectedMaxValue().intValue();
                                String max = seekBar.getSelectedMaxTime();
                                String min = seekBar.getSelectedMinTime();
                                timeText.setText(String.format("%s - %s", min, max));
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });

    }

    private void showTimeline() {
        Log.d(TAG, "showTimeline: " + mDate.toString() + " start: " + mStartTime + " end: " + mEndTime);
        long timestamp = mDate.getTime();
        long startAt = timestamp + mStartTime * 60 * 60 * 1000;
        long endAt = timestamp + mEndTime * 60 * 60 * 1000;

        List<LocationAddress> locations = DBUtil.getSentLocations(startAt, endAt);

        for (LocationAddress la : locations) {
            Log.d(TAG, "showTimeline: " + Utils.getAddressLine(Utils.getAddress(la), LocationRange.CURRENT.range) + " lat:" + la.getLatitude() + " log:" + la.getLongitude());
            mMap.addMarker(new MarkerOptions().position(new LatLng(la.getLatitude(), la.getLongitude())));
        }

        int i = 0;

        LocationAddress [] array = locations.toArray(new LocationAddress[0]);

        for (i = 0; i < array.length; i++) {
            LocationAddress prev = array[i];
            LocationAddress current = (array.length >= (i+1)) ? array[i+1] : null;
            LocationAddress next = (array.length >= (i+2)) ? array[i+2] : null;

            if (current == null) break;

            long prevTime = prev.getTimestamp();
            long currentTime = current.getTimestamp();

            long diffTime = (currentTime - prevTime) / 1000; // seconds
            double distance = Utils.distanceTo(Utils.getLocation(prev), Utils.getLocation(current));

            if (diffTime < (5 * 60) && distance < 100) {

            }

            long nextTime = next.getTimestamp();

        }


/*
        FB.getTimelineLocations(startAt, endAt, new FB.LocationListener() {
            @Override
            public void onSuccess(final Location location, final Address address) {
            }

            @Override
            public void onFail(String err) {
            }
        });
*/
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
    }
}
