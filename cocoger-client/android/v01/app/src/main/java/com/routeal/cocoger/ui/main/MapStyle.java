package com.routeal.cocoger.ui.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.util.Utils;

/**
 * Created by hwatanabe on 9/27/17.
 */

class MapStyle {

    private CustomMapStyle mMapStyles[];
    private GoogleMap mMap;
    private Activity mActivity;
    private View.OnClickListener mapLayerButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog();
        }
    };

    MapStyle(GoogleMap map, Activity activity) {
        mMap = map;
        mActivity = activity;

        mMapStyles = new CustomMapStyle[7];
        mMapStyles[0] = new CustomMapStyle();
        mMapStyles[0].id = R.raw.mapstyle_retro;
        mMapStyles[0].resource = "retro";
        mMapStyles[1] = new CustomMapStyle();
        mMapStyles[1].id = R.raw.mapstyle_night;
        mMapStyles[1].resource = "night";
        mMapStyles[2] = new CustomMapStyle();
        mMapStyles[2].id = R.raw.mapstyle_gray;
        mMapStyles[2].resource = "gray";
        mMapStyles[3] = new CustomMapStyle();
        mMapStyles[3].id = R.raw.mapstyle_blue;
        mMapStyles[3].resource = "blue";
        mMapStyles[4] = new CustomMapStyle();
        mMapStyles[4].id = R.raw.mapstyle_slate;
        mMapStyles[4].resource = "slate";
        mMapStyles[5] = new CustomMapStyle();
        mMapStyles[5].id = R.raw.mapstyle_white;
        mMapStyles[5].resource = "white";
        mMapStyles[6] = new CustomMapStyle();
        mMapStyles[6].id = R.raw.mapstyle_pink;
        mMapStyles[6].resource = "pink";

        // set up the 'my' location button
        Drawable layerDrawable = Utils.getIconDrawable(mActivity, R.drawable.ic_layers_white_24dp, R.color.gray);
        FloatingActionButton layerButton = (FloatingActionButton) mActivity.findViewById(R.id.map_layer);
        layerButton.setImageDrawable(layerDrawable);
        layerButton.setOnClickListener(mapLayerButtonListener);
    }

    private void dialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mActivity);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_layer, null);

        mMapStyles[0].view = (SwitchCompat) view.findViewById(R.id.switch_retro);
        mMapStyles[1].view = (SwitchCompat) view.findViewById(R.id.switch_night);
        mMapStyles[2].view = (SwitchCompat) view.findViewById(R.id.switch_grayscale);
        mMapStyles[3].view = (SwitchCompat) view.findViewById(R.id.switch_muted_blue);
        mMapStyles[4].view = (SwitchCompat) view.findViewById(R.id.switch_pale_down);
        mMapStyles[5].view = (SwitchCompat) view.findViewById(R.id.switch_paper);
        mMapStyles[6].view = (SwitchCompat) view.findViewById(R.id.switch_pinky);

        for (int i = 0; i < mMapStyles.length; i++) {
            String current_style = MainApplication.getString("style");
            if (current_style != null && !current_style.isEmpty()) {
                if (mMapStyles[i].resource.equals(current_style)) {
                    mMapStyles[i].view.setChecked(true);
                } else {
                    mMapStyles[i].view.setChecked(false);
                }
            }
        }

        for (int i = 0; i < mMapStyles.length; i++) {
            final int n = i;
            mMapStyles[n].view.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        CustomMapStyle cstyle = mMapStyles[n];
                        for (int j = 0; j < mMapStyles.length; j++) {
                            if (cstyle.view != mMapStyles[j].view) {
                                mMapStyles[j].view.setChecked(false);
                            }
                        }
                        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(mActivity, cstyle.id);
                        mMap.setMapStyle(style);
                        MainApplication.putString("style", cstyle.resource);
                    } else {
                        mMap.setMapStyle(null);
                        MainApplication.putString("style", "normal");
                    }
                }
            });
        }

        final SwitchCompat traffic = (SwitchCompat) view.findViewById(R.id.switch_traffic);
        traffic.setChecked(MainApplication.getBool("traffic"));

        traffic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mMap.setTrafficEnabled(isChecked);
                MainApplication.putBool("traffic", isChecked);
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(mActivity)
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(R.string.dismiss, null)
                .show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    void init(Context context, GoogleMap googleMap) {
        boolean traffic_value = MainApplication.getBool("traffic");
        googleMap.setTrafficEnabled(traffic_value);

        String current_style = MainApplication.getString("style");
        if (current_style == null || current_style.equals("normal")) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else {
            for (int i = 0; i < mMapStyles.length; i++) {
                if (mMapStyles[i].resource.equals(current_style)) {
                    MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(context, mMapStyles[i].id);
                    googleMap.setMapStyle(style);
                    break;
                }
            }
        }
    }

    private class CustomMapStyle {
        SwitchCompat view;
        int id;
        String resource;
    }
}
