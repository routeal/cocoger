package com.routeal.cocoger.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.util.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by hwatanabe on 10/8/17.
 */

public class PlaceManager implements MapActivity.MarkerInterface, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {
    private final static String TAG = "PlaceManager";
    private static PlaceColorButton mPlaceColorButtons[] = {
            new PlaceColorButton(R.id.place_1, R.id.place_image_1, R.color.steelblue, "steelblue"),
            new PlaceColorButton(R.id.place_2, R.id.place_image_2, R.color.yellowgreen, "yellowgreen"),
            new PlaceColorButton(R.id.place_3, R.id.place_image_3, R.color.firebrick, "firebrick"),
            new PlaceColorButton(R.id.place_4, R.id.place_image_4, R.color.gold, "gold"),
            new PlaceColorButton(R.id.place_5, R.id.place_image_5, R.color.hotpink, "hotpink"),
    };
    private static HashMap<String, Integer> mPlaceColorMap = new HashMap<String, Integer>() {{
        put("steelblue", R.color.steelblue);
        put("yellowgreen", R.color.yellowgreen);
        put("firebrick", R.color.firebrick);
        put("gold", R.color.gold);
        put("hotpink", R.color.hotpink);
    }};
    Map<Marker, InfoWindow> mPlaceMarkers = new HashMap<Marker, InfoWindow>();
    private GoogleMap mMap;
    private GeoDataClient mGeoDataClient;
    private InfoWindowManager mInfoWindowManager;

    PlaceManager(GoogleMap googleMap, GeoDataClient geoDataClient, InfoWindowManager infoWindowManager) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mGeoDataClient = geoDataClient;
        mInfoWindowManager = infoWindowManager;
    }

    void addPlace(final Context context, final Location location, String address, String title, final Bitmap bitmap) {
        Toast.makeText(MainApplication.getContext(), "Save To Map:" + title, Toast.LENGTH_SHORT).show();

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_place, null);

        for (PlaceColorButton pc : mPlaceColorButtons) {
            pc.imageView = (ImageView) view.findViewById(pc.imageId);
            Drawable drawable = Utils.getIconDrawable(context, R.drawable.ic_place_white_18dp, pc.colorId);
            pc.imageView.setImageDrawable(drawable);
            pc.radioButton = (RadioButton) view.findViewById(pc.id);
            pc.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    for (PlaceColorButton pc : mPlaceColorButtons) {
                        if (isChecked) {
                            if (pc.radioButton != buttonView) {
                                pc.radioButton.setChecked(false);
                            }
                        }
                    }
                }
            });
        }

        final TextView placeTitle = (TextView) view.findViewById(R.id.place_title);
        placeTitle.setText(title);

        final TextView placeAddress = (TextView) view.findViewById(R.id.place_address);
        placeAddress.setText(address);

        final TextView placeDesc = (TextView) view.findViewById(R.id.place_description);

        final SwitchCompat placeFriend = (SwitchCompat) view.findViewById(R.id.place_seen_friend);

        ImageView placeImage = (ImageView) view.findViewById(R.id.place_image);

        if (bitmap != null) {
            placeImage.setImageBitmap(bitmap);
        } else {
            Drawable drawable = Utils.getIconDrawable(context, R.drawable.ic_place_white_48dp, R.color.steelblue);
            placeImage.setImageDrawable(drawable);
        }

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String color = "";
                        for (PlaceColorButton pc : mPlaceColorButtons) {
                            if (pc.radioButton.isChecked()) {
                                color = pc.colorName;
                                break;
                            }
                        }
                        String title = placeTitle.getText().toString();
                        String description = placeDesc.getText().toString();
                        String address = placeAddress.getText().toString();
                        boolean showFriend = placeFriend.isChecked();
                        Log.d(TAG, String.format("color=%s: title=%s: desc=%s: address=%s: showFriend=%s", color, title, description, address, showFriend ? "true" : "false"));

                        int colorId = mPlaceColorMap.get(color);
                        Drawable drawable = Utils.getIconDrawable(context, R.drawable.ic_place_white_48dp, colorId);
                        BitmapDescriptor icon = Utils.getBitmapDescriptor(drawable);

                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(Utils.getLatLng(location))
                                .draggable(true)
                                .icon(icon));
                        dropPinEffect(marker);

                        PlaceInfoFragment placeInfoFragment = new PlaceInfoFragment();
                        placeInfoFragment.setTitle(title);
                        placeInfoFragment.setLocation(location);
                        placeInfoFragment.setAddress(address);
                        placeInfoFragment.setStreetViewPicture(bitmap);
                        placeInfoFragment.setPlaceManager(PlaceManager.this);
                        if (FB.getUser() != null && FB.getUser().getDisplayName() != null) {
                            placeInfoFragment.setPoiCreator(FB.getUser().getDisplayName());
                        }
                        InfoWindow.MarkerSpecification mMarkerOffset = new InfoWindow.MarkerSpecification(0, 128);
                        InfoWindow window = new InfoWindow(marker, mMarkerOffset, placeInfoFragment);
                        mPlaceMarkers.put(marker, window);
                    }
                })
                .show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        for (Iterator<Map.Entry<Marker, InfoWindow>> it = mPlaceMarkers.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Marker, InfoWindow> entry = it.next();
            Marker m = entry.getKey();
            if (marker.getId().equals(m.getId())) {
                InfoWindow window = entry.getValue();
                if (window.getWindowFragment() instanceof PlaceInfoFragment) {
                    PlaceInfoFragment fragment = (PlaceInfoFragment) window.getWindowFragment();
                    // Adjust the info window location when the marker is moved by drag
                    if (!marker.getPosition().equals(Utils.getLatLng(fragment.getLocation()))) {
                        fragment.setLocation(Utils.getLocation(marker.getPosition()));
                        window.setPosition(marker.getPosition());
                    }
                }
                mInfoWindowManager.toggle(window, true);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onWindowHidden(InfoWindow infoWindow) {
        Fragment fragment = infoWindow.getWindowFragment();
        if (fragment != null) {
            if (fragment instanceof PoiInfoFragment) {
                ;
            }
        }
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng dragPosition = marker.getPosition();
        double dragLat = dragPosition.latitude;
        double dragLong = dragPosition.longitude;
        Log.i("info", "on drag end :" + dragLat + " dragLong :" + dragLong);
        Toast.makeText(MainApplication.getContext(), "Marker Dragged..!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        int colorId = R.color.steelblue;
        Drawable drawable = Utils.getIconDrawable(MainApplication.getContext(), R.drawable.ic_place_white_48dp, colorId);
        BitmapDescriptor icon = Utils.getBitmapDescriptor(drawable);
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .icon(icon));
        dropPinEffect(marker);

        PlaceInfoFragment placeInfoFragment = new PlaceInfoFragment();
        placeInfoFragment.setAddress("TBD");
        placeInfoFragment.setTitle("Place Name");
        placeInfoFragment.setLocation(Utils.getLocation(latLng));
        placeInfoFragment.setPlaceManager(PlaceManager.this);
        if (FB.getUser() != null && FB.getUser().getDisplayName() != null) {
            placeInfoFragment.setPoiCreator(FB.getUser().getDisplayName());
        }
        InfoWindow.MarkerSpecification mMarkerOffset = new InfoWindow.MarkerSpecification(0, 128);
        InfoWindow window = new InfoWindow(marker, mMarkerOffset, placeInfoFragment);
        mPlaceMarkers.put(marker, window);
    }

    void hideInfoWindow(PlaceInfoFragment fragment) {
        for (Iterator<Map.Entry<Marker, InfoWindow>> it = mPlaceMarkers.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Marker, InfoWindow> entry = it.next();
            InfoWindow infoWindow = entry.getValue();
            if (infoWindow.getWindowFragment() == fragment) {
                mInfoWindowManager.hide(infoWindow, true);
                return;
            }
        }
    }

    private void dropPinEffect(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator =
                new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15);
                } else { // done elapsing, show window
                    marker.showInfoWindow();
                }
            }
        });
    }

    private static class PlaceColorButton {
        int id;
        int imageId;
        int colorId;
        String colorName;
        RadioButton radioButton;
        ImageView imageView;

        PlaceColorButton(int id, int imageId, int colorId, String colorName) {
            this.id = id;
            this.imageId = imageId;
            this.colorId = colorId;
            this.colorName = colorName;
        }
    }
}

