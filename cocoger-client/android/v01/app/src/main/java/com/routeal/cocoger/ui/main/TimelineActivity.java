package com.routeal.cocoger.ui.main;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.provider.DBUtil;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.RangeSeekBar;
import com.routeal.cocoger.util.Utils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
    private List<Marker> mMarkers = new ArrayList<>();
    private Polyline mPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(true);
            ab.setTitle(R.string.timeline);
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
            case R.id.action_send:
                sendTimeline();
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
        final EditText dateText = (EditText) view.findViewById(R.id.dialog_datepicker);
        final EditText timeText = (EditText) view.findViewById(R.id.dialog_timerange);

        mDate = null;
        mStartTime = 0;
        mEndTime = 24;
        mStartTimeStr = "0 am";
        mEndTimeStr = "12 pm";

        new AlertDialog.Builder(TimelineActivity.this)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mDate == null) {
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
                View childView = layoutInflaterAndroid.inflate(R.layout.dialog_datepicker, null);
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
                View childView = layoutInflaterAndroid.inflate(R.layout.dialog_timerange, null);
                final RangeSeekBar seekBar = (RangeSeekBar) childView.findViewById(R.id.seekbar);
                new AlertDialog.Builder(TimelineActivity.this)
                        .setView(childView)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mStartTime = seekBar.getSelectedMinValue().intValue();
                                mEndTime = seekBar.getSelectedMaxValue().intValue();
                                mStartTimeStr = seekBar.getSelectedMinTime();
                                mEndTimeStr = seekBar.getSelectedMaxTime();
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

    private void removeLocationJitters(List<LocationAddress> locations) {
        Log.d(TAG, "removeLocationJitters: original size=" + locations.size());

        // removes the same address as the previous one
        Address prevAddress = null;
        Iterator<LocationAddress> it = locations.iterator();
        while (it.hasNext()) {
            LocationAddress la = it.next();
            Address address = Utils.getAddress(la);
            if (prevAddress != null) {
                String newAddressLine = Utils.getAddressLine(address);
                String oldAddressLine = Utils.getAddressLine(prevAddress);
                if (newAddressLine.equals(oldAddressLine)) {
                    it.remove();
                }
            }
            prevAddress = address;
        }

        Log.d(TAG, "removeLocationJitters: removed same address size=" + locations.size());

        // removes B where B is moved from A and moved to C but A and C is very close and
        // duration of B and C is within 60 mins
        LocationAddress A = null;
        LocationAddress B = null;
        it = locations.iterator();
        List<LocationAddress> removed2 = new ArrayList<>();
        while (it.hasNext()) {
            LocationAddress C = it.next();
            if (A == null && B == null) {
                A = C;
            } else if (B == null) {
                B = C;
            } else {
                Location a = Utils.getLocation(A);
                Location b = Utils.getLocation(B);
                Location c = Utils.getLocation(C);
                if (Utils.distanceTo(a, c) < 100 &&
                        (c.getTime() - b.getTime()) < 60 * 60 * 1000) {
                    removed2.add(B);
                }
                A = C;
                B = null;
            }
        }
        for (LocationAddress la : removed2) {
            locations.remove(la);
        }
        Log.d(TAG, "removeLocationJitters: removed triangle errors size=" + locations.size());

        // removes B where B is moved from A and B moved to C and the heading angle is same.
        A = null;
        B = null;
        it = locations.iterator();
        removed2 = new ArrayList<>();
        while (it.hasNext()) {
            LocationAddress C = it.next();
            if (A == null && B == null) {
                A = C;
            } else if (B == null) {
                B = C;
            } else {
                Location a = Utils.getLocation(A);
                Location b = Utils.getLocation(B);
                Location c = Utils.getLocation(C);

                //double distance1 = Utils.distanceTo(a, b);
                double heading1 = SphericalUtil.computeHeading(Utils.getLatLng(a), Utils.getLatLng(b));

                //double distance2 = Utils.distanceTo(b, c);
                double heading2 = SphericalUtil.computeHeading(Utils.getLatLng(b), Utils.getLatLng(c));

                if (/*Math.abs(distance1 - distance2) < 100 &&*/ Math.abs(heading1 - heading2) < 15) {
                    removed2.add(B);
                }
                A = B;
                B = null;
            }
        }
        for (LocationAddress la : removed2) {
            locations.remove(la);
        }
        Log.d(TAG, "removeLocationJitters: removed the same directions size=" + locations.size());


        // removes the negative times (invalid) and the speed is less
        // than walking and also the moving distance is less than 40
        // meters
        Location prevLocation = null;
        it = locations.iterator();
        while (it.hasNext()) {
            LocationAddress la = it.next();
            Location location = Utils.getLocation(la);
            if (prevLocation != null) {
                long time = location.getTime() - prevLocation.getTime();
                // 20/18 = 4 * 5 / 18 (4 km/h)
                if ((time <= 0) || (location.getSpeed() < (25 / 18) && Utils.distanceTo(prevLocation, location) < 50)) {
                    it.remove();
                }
            }
            prevLocation = location;
        }

        Log.d(TAG, "removeLocationJitters: removed walking distances size=" + locations.size());

        // removes p1-pn if p0-pn are within the certain range.
        List<LocationAddress> jitters = new ArrayList<>();
        List<LocationAddress> removed = new ArrayList<>();
        LocationAddress prev = null;
        it = locations.iterator();
        while (it.hasNext()) {
            LocationAddress la = it.next();
            Location location = Utils.getLocation(la);
            if (prev != null) {
                if (jitters.isEmpty()) {
                    jitters.add(prev);
                }
                prevLocation = Utils.getLocation(prev);
                float currentDistance = prevLocation.distanceTo(location);
                Location firstLocation = Utils.getLocation(jitters.get(0));
                float firstDistance = firstLocation.distanceTo(location);
                if (currentDistance < 100 && firstDistance < 100) {
                    jitters.add(la);
                } else {
                    for (int i = 1; i < jitters.size(); i++) {
                        removed.add(jitters.get(i));
                    }
                    jitters.clear();
                }
            }
            prev = la;
        }
        for (LocationAddress la : removed) {
            locations.remove(la);
        }
        Log.d(TAG, "removeLocationJitters: removed jitters size=" + locations.size());
    }

    private void showTimeline() {
        Log.d(TAG, "showTimeline: " + mDate.toString() + " start: " + mStartTime + " end: " + mEndTime);
        long timestamp = mDate.getTime();
        long startAt = timestamp + mStartTime * 60 * 60 * 1000;
        long endAt = timestamp + mEndTime * 60 * 60 * 1000;

        // cleanup the previous timeline if any
        if (mPolyline != null) {
            mPolyline.remove();
        }
        // remove the previous markers
        for (Marker marker : mMarkers) {
            marker.remove();
        }
        mMarkers.clear();

        // location raw data
        List<LocationAddress> locations = DBUtil.getSentLocations(startAt, endAt);

        // remove the location jitters
        removeLocationJitters(locations);

        for (LocationAddress la : locations) {
            Location location = Utils.getLocation(la);
            Address address = Utils.getAddress(la);

            // whole address line
            String addressLine = Utils.getAddressLine(address, LocationRange.CURRENT.range, true);

            // speed in m/h
            String speedRate = getResources().getString(R.string.speed_rate);
            double speed = location.getSpeed() * Double.parseDouble(speedRate); //m/h  - 18 / 5;  // km / hour

            // date in short format
            DateFormat f = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
            String formattedDate = f.format(new Date(location.getTime()));
            String titleFormat = getResources().getString(R.string.timeline_marker_title_format);

            String title = String.format("%s (%.2f m/h)", formattedDate, speed);

            Drawable drawable = Utils.getIconDrawable(this, R.drawable.ic_place_white_24dp, R.color.indigo_500);

            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(la.getLatitude(), la.getLongitude()))
                    .title(title)
                    .icon(Utils.getBitmapDescriptor(drawable))
                    .snippet(addressLine));
            mMarkers.add(marker);
        }

        // zoom into the area where all the markers can be shown
        if (mMarkers.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.timeline)
                    .setMessage(R.string.no_location_timeline)
                    .setCancelable(true)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        } else {
            // area bound
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            // polyline
            List<LatLng> points = new ArrayList<>();
            PolylineOptions lineOptions = new PolylineOptions();

            for (Marker marker : mMarkers) {
                builder.include(marker.getPosition());
                points.add(marker.getPosition());
            }

            // area zoom in
            LatLngBounds bounds = builder.build();
            int padding = 100; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.moveCamera(cu);

            // polyline
            lineOptions.addAll(points);
            lineOptions.width(10);
            lineOptions.color(ContextCompat.getColor(this, R.color.blue_grey_400));
            mPolyline = mMap.addPolyline(lineOptions);
        }

        // shows the status message at the bottom
        DateFormat f = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        String formattedDate = f.format(mDate);
        String message = String.format("Start: %s End: %s Date: %s", mStartTimeStr, mEndTimeStr, formattedDate);

        Snackbar snack = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE);
        View view = snack.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        snack.show();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_timeline);
        mMap.setMapStyle(style);

    }

    void sendTimeline() {
        Toast.makeText(this, "Share Timeline not implemented", Toast.LENGTH_SHORT).show();
    }
}
