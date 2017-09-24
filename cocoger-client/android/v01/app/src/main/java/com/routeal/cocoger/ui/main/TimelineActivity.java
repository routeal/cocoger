package com.routeal.cocoger.ui.main;

import android.content.DialogInterface;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.provider.DBUtil;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.RangeSeekBar;
import com.routeal.cocoger.util.Utils;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by nabe on 9/16/17.
 */

public class TimelineActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final static String TAG = "TimelineActivity";

    private Date mDate;
    private int mStartTime;
    private int mEndTime;
    private String mStartTimeStr;
    private String mEndTimeStr;
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
                                mDate = new Date(year - 1900, month, day, 0, 0);
                                DateFormat f = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
                                String formattedDate = f.format(mDate);
                                dateText.setText(formattedDate);
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
                                mStartTimeStr = seekBar.getSelectedMaxTime();
                                mEndTimeStr = seekBar.getSelectedMinTime();
                                timeText.setText(String.format("%s - %s", mStartTimeStr, mEndTimeStr));
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

            Location location = Utils.getLocation(la);
            Address address = Utils.getAddress(la);
            String addressLine = Utils.getAddressLine(address, LocationRange.CURRENT.range);

            double speed = location.getSpeed() * 18 / 5;  // km / hour
            String title = String.format("speed=%f", speed);

            mMap.addMarker(new MarkerOptions().position(new LatLng(la.getLatitude(), la.getLongitude()))
                    .title(title)
                    .snippet(addressLine));
        }

        // shows the status message at the bottom
        DateFormat f = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        String formattedDate = f.format(mDate);
        String message = formattedDate + " " + mStartTimeStr + " - " + mEndTimeStr;
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE).show();

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
