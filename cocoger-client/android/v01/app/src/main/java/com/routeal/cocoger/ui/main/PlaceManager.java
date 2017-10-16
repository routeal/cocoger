package com.routeal.cocoger.ui.main;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Place;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.Utils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by hwatanabe on 10/8/17.
 */

public class PlaceManager implements MarkerInterface, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {
    private final static String TAG = "PlaceManager";

    private final static String DEFAULT_MARKER_COLOR = "steelblue";

    private final static PlaceColorButton mPlaceColorButtons[] = {
            new PlaceColorButton(R.id.place_1, R.id.place_image_1, R.color.steelblue, "steelblue"),
            new PlaceColorButton(R.id.place_2, R.id.place_image_2, R.color.yellowgreen, "yellowgreen"),
            new PlaceColorButton(R.id.place_3, R.id.place_image_3, R.color.firebrick, "firebrick"),
            new PlaceColorButton(R.id.place_4, R.id.place_image_4, R.color.gold, "gold"),
            new PlaceColorButton(R.id.place_5, R.id.place_image_5, R.color.hotpink, "hotpink"),
    };

    private final static HashMap<String, Integer> mPlaceColorMap = new HashMap<String, Integer>() {{
        put("steelblue", R.color.steelblue);
        put("yellowgreen", R.color.yellowgreen);
        put("firebrick", R.color.firebrick);
        put("gold", R.color.gold);
        put("hotpink", R.color.hotpink);
    }};

    private Map<Marker, InfoWindow> mPlaceMarkers = new HashMap<Marker, InfoWindow>();
    private GoogleMap mMap;
    private InfoWindowManager mInfoWindowManager;
    private ImageView cropImageView;
    private MapActivity mActivity;

    PlaceManager(MapActivity activity, GoogleMap googleMap, InfoWindowManager infoWindowManager) {
        mActivity = activity;
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mInfoWindowManager = infoWindowManager;
    }

    private Drawable getIcon(int colorId) {
        return Utils.getIconDrawable(mActivity, R.drawable.ic_place_white_18dp, colorId);
    }

    private BitmapDescriptor getIcon(String colorName) {
        int colorId = mPlaceColorMap.get(colorName);
        Drawable drawable = Utils.getIconDrawable(mActivity, R.drawable.ic_place_white_48dp, colorId);
        return Utils.getBitmapDescriptor(drawable);
    }

    void add(String key, Place place) {
        addMarker(key, place, null, false);
    }

    void change(String key, Place place) {
        Marker marker = getMarker(key);
        if (marker != null) {
            // position
            LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());
            InfoWindow window = mPlaceMarkers.get(marker);
            window.setPosition(latLng);
            marker.setPosition(latLng);

            // place
            PlaceInfoFragment fragment = (PlaceInfoFragment) window.getWindowFragment();
            fragment.setPlace(place);

            // marker color
            marker.setIcon(getIcon(place.getMarkerColor()));
        }
    }

    void remove(String key) {
        Marker marker = getMarker(key);
        if (marker != null) {
            marker.remove();
            InfoWindow window = mPlaceMarkers.get(marker);
            PlaceInfoFragment fragment = (PlaceInfoFragment) window.getWindowFragment();
            removeFragment(fragment);
            mPlaceMarkers.remove(marker);
        }
    }

    // edit a place or add a new place but the marker is already created by long press
    void editPlace(final String key, final Place place) {
        final PlaceInfoFragment fragment = getPlaceInfoFragment(place);
        if (fragment == null) {
            return;
        }

        // place is from database or not
        final boolean isEdit = (key != null && !key.isEmpty());

        LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());
        Location location = Utils.getLocation(latLng);

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mActivity);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_place, null);

        for (PlaceColorButton pc : mPlaceColorButtons) {
            pc.imageView = (ImageView) view.findViewById(pc.imageId);
            pc.imageView.setImageDrawable(getIcon(pc.colorId));
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
            if (isEdit) {
                pc.radioButton.setChecked(pc.colorName.equals(place.getMarkerColor()));
            }
        }

        final TextView placeTitle = (TextView) view.findViewById(R.id.place_title);
        placeTitle.setText(place.getTitle());

        String address = place.getAddress();
        if (address == null) {
            Address a = Utils.getAddress(location);
            address = Utils.getAddressLine(a);
        }

        final TextView placeAddress = (TextView) view.findViewById(R.id.place_address);
        placeAddress.setText(address);

        final TextView placeDesc = (TextView) view.findViewById(R.id.place_description);
        placeDesc.setText(place.getDescription());

        final SwitchCompat placeFriend = (SwitchCompat) view.findViewById(R.id.place_seen_friend);
        placeFriend.setChecked(place.getSeenBy() != null && place.getSeenBy().equals("friends"));

        final ImageView placeImage = (ImageView) view.findViewById(R.id.place_image);
        placeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView = placeImage;
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setActivityTitle(mActivity.getResources().getString(R.string.picture))
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setRequestedSize(128, 128)
                        .setFixAspectRatio(true)
                        .setAspectRatio(100, 100)
                        .start(mActivity);
            }
        });

        new LoadImage(false, new LoadImage.LoadImageListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                if (bitmap != null) {
                    placeImage.setImageBitmap(bitmap);
                } else {
                    placeImage.setImageDrawable(getIcon(R.color.steelblue));
                }
            }
        }).loadPlace(place.getUid(), key);

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setView(view);
        builder.setCancelable(true);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removePlace(key, place, fragment);
            }
        });

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tmp = place.getMarkerColor();
                for (PlaceColorButton pc : mPlaceColorButtons) {
                    if (pc.radioButton.isChecked()) {
                        tmp = pc.colorName;
                        break;
                    }
                }
                String markerColor2 = tmp;

                Bitmap bitmap = null;
                if (cropImageView != null && cropImageView.getDrawable() != null) {
                    bitmap = ((BitmapDrawable) cropImageView.getDrawable()).getBitmap();
                }
                Bitmap updateBitmap = bitmap;

                place.setTitle(placeTitle.getText().toString());
                place.setDescription(placeDesc.getText().toString());
                place.setAddress(placeAddress.getText().toString());
                place.setSeenBy(placeFriend.isChecked() ? "friends" : "none");
                place.setMarkerColor(markerColor2);

                if (isEdit) {
                    FB.editPlace(key, place, updateBitmap, null);
                } else {
                    final Marker marker = getMarker(fragment);
                    FB.addPlace(place, updateBitmap,
                            new FB.PlaceListener() {
                                @Override
                                public void onSuccess(String key, Place place) {
                                    removeFragment(fragment);
                                    if (marker != null) {
                                        marker.remove();
                                    }
                                }

                                @Override
                                public void onFail(String err) {
                                    Log.d(TAG, "editPlace: add a place:" + err);
                                }
                            });
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

    // add a new place to create a new marker
    void addPlace(String title, final Location location, String address, final Bitmap bitmap) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mActivity);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_place, null);

        for (PlaceColorButton pc : mPlaceColorButtons) {
            pc.imageView = (ImageView) view.findViewById(pc.imageId);
            pc.imageView.setImageDrawable(getIcon(pc.colorId));
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

        final TextView placeDesc = (TextView) view.findViewById(R.id.place_description);

        final TextView placeAddress = (TextView) view.findViewById(R.id.place_address);
        String tmpAddress = address;
        if (address == null || !address.isEmpty()) {
            Address a = Utils.getAddress(location);
            tmpAddress = Utils.getAddressLine(a);
        }
        placeAddress.setText(tmpAddress);

        final SwitchCompat seenFriend = (SwitchCompat) view.findViewById(R.id.place_seen_friend);

        final ImageView placeImage = (ImageView) view.findViewById(R.id.place_image);
        placeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView = placeImage;
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setActivityTitle(mActivity.getResources().getString(R.string.picture))
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setRequestedSize(128, 128)
                        .setFixAspectRatio(true)
                        .setAspectRatio(100, 100)
                        .start(mActivity);
            }
        });
        if (bitmap != null) {
            placeImage.setImageBitmap(bitmap);
        } else {
            placeImage.setImageDrawable(getIcon(R.color.steelblue));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setView(view);
        builder.setCancelable(true);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tmp = DEFAULT_MARKER_COLOR;
                for (PlaceColorButton pc : mPlaceColorButtons) {
                    if (pc.radioButton.isChecked()) {
                        tmp = pc.colorName;
                        break;
                    }
                }
                String markerColor2 = tmp;
                Bitmap tmp2 = bitmap;
                if (cropImageView != null && cropImageView.getDrawable() != null) {
                    tmp2 = ((BitmapDrawable) cropImageView.getDrawable()).getBitmap();
                }
                final Bitmap bitmap2 = tmp2;

                final Place place = new Place();
                place.setTitle(placeTitle.getText().toString());
                place.setDescription(placeDesc.getText().toString());
                place.setLatitude(location.getLatitude());
                place.setLongitude(location.getLongitude());
                place.setAddress(placeAddress.getText().toString());
                place.setSeenBy(seenFriend.isChecked() ? "friends" : "none");
                place.setMarkerColor(markerColor2);
                place.setCreated(System.currentTimeMillis());
                place.setUid(FB.getUid());

                FB.addPlace(place, bitmap2, null);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

    private void addMarker(String key, Place place, Bitmap bitmap, boolean hasAnimation) {
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(place.getLatitude(), place.getLongitude()))
                .icon(getIcon(place.getMarkerColor())));

        if (hasAnimation) {
            dropPinEffect(marker);
        }

        // set draggable only on my markers
        marker.setDraggable(place.getUid() != null && place.getUid().equals(FB.getUid()));

        PlaceInfoFragment placeInfoFragment = new PlaceInfoFragment();
        placeInfoFragment.setKey(key);
        placeInfoFragment.setPlace(place);
        placeInfoFragment.setStreetViewPicture(bitmap);
        placeInfoFragment.setPlaceManager(PlaceManager.this);

        InfoWindow.MarkerSpecification mMarkerOffset = new InfoWindow.MarkerSpecification(0, 128);
        InfoWindow window = new InfoWindow(marker, mMarkerOffset, placeInfoFragment);

        mPlaceMarkers.put(marker, window);
    }

    private void removeFragment(PlaceInfoFragment fragment) {
        Marker marker = getMarker(fragment);
        if (marker != null) {
            InfoWindow window = mPlaceMarkers.get(marker);
            mInfoWindowManager.hide(window, true);
            FragmentManager fragmentManager = fragment.getFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction trans = fragmentManager.beginTransaction();
                trans.remove(fragment);
                trans.commit();
            }
            marker.remove();
            mPlaceMarkers.remove(marker);
        }
    }

    void removePlace(String key, Place place) {
        PlaceInfoFragment fragment = getPlaceInfoFragment(place);
        if (fragment != null) {
            removePlace(key, place, fragment);
        }
    }

    private void removePlace(final String key, final Place place, final PlaceInfoFragment fragment) {
        String title = place.getTitle();
        String defaultTitle = mActivity.getResources().getString(R.string.place_remove);
        new AlertDialog.Builder(mActivity)
                .setTitle((title != null) ? title : defaultTitle)
                .setMessage(R.string.confirm_place_remove)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removePlaceImpl(key, place, fragment);
                    }
                })
                .setPositiveButton(android.R.string.no, null)
                .show();
    }

    private void removePlaceImpl(String key, Place place, final PlaceInfoFragment fragment) {
        if (key == null || key.isEmpty()) {
            removeFragment(fragment);
            return;
        }

        FB.deletePlace(key, place, null);
    }

    void showPlace(String key) {
        Marker marker = getMarker(key);
        if (marker == null) {
            return;
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                marker.getPosition(), mMap.getCameraPosition().zoom));
    }

    private PlaceInfoFragment getPlaceInfoFragment(Place place) {
        for (Iterator<Map.Entry<Marker, InfoWindow>> it = mPlaceMarkers.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Marker, InfoWindow> entry = it.next();
            InfoWindow window = entry.getValue();
            if (window.getWindowFragment() instanceof PlaceInfoFragment) {
                PlaceInfoFragment fragment = (PlaceInfoFragment) window.getWindowFragment();
                Place infoPlace = fragment.getPlace();
                if (place.getUid().equals(infoPlace.getUid()) && place.getCreated() == infoPlace.getCreated()) {
                    return fragment;
                }
            }
        }
        return null;
    }

    private Marker getMarker(Fragment fragment) {
        for (Iterator<Map.Entry<Marker, InfoWindow>> it = mPlaceMarkers.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Marker, InfoWindow> entry = it.next();
            InfoWindow window = entry.getValue();
            if (window.getWindowFragment() == fragment) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Marker getMarker(String key) {
        for (Iterator<Map.Entry<Marker, InfoWindow>> it = mPlaceMarkers.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Marker, InfoWindow> entry = it.next();
            InfoWindow window = entry.getValue();
            if (window.getWindowFragment() instanceof PlaceInfoFragment) {
                PlaceInfoFragment fragment = (PlaceInfoFragment) window.getWindowFragment();
                if (key.equals(fragment.getKey())) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        for (Iterator<Map.Entry<Marker, InfoWindow>> it = mPlaceMarkers.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Marker, InfoWindow> entry = it.next();
            Marker m = entry.getKey();
            if (marker.getId().equals(m.getId())) {
                InfoWindow window = entry.getValue();
                mInfoWindowManager.toggle(window, true);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onWindowHidden(InfoWindow infoWindow) {
        // empty
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        // empty
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng position = marker.getPosition();
        InfoWindow window = mPlaceMarkers.get(marker);
        window.setPosition(position);

        PlaceInfoFragment fragment = (PlaceInfoFragment) window.getWindowFragment();

        Place place = fragment.getPlace();
        place.setLatitude(position.latitude);
        place.setLongitude(position.longitude);

        Address address = Utils.getAddress(position);
        String addressLine = Utils.getAddressLine(address);
        place.setAddress(addressLine);

        String key = fragment.getKey();
        if (key == null || key.isEmpty()) {
            // not saved yet to the database
            return;
        }

        FB.editPlace(key, place, null, null);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Place place = new Place();
        place.setMarkerColor(DEFAULT_MARKER_COLOR);
        place.setTitle("Place Name");
        place.setDescription("Place Description");
        place.setLatitude(latLng.latitude);
        place.setLongitude(latLng.longitude);
        place.setUid(FB.getUid());
        place.setCreated(System.currentTimeMillis());
        addMarker(null, place, null, true);
    }

    void setCropImage(Activity activity, Uri uri) {
        if (cropImageView != null) {
            try {
                InputStream inputStream = activity.getContentResolver().openInputStream(uri);
                Drawable drawable = Drawable.createFromStream(inputStream, uri.toString());
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                cropImageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "setCropImage:", e);
            }
        }
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

    public interface PlaceListener {
        void onAdded(String key, Place place);
        void onChanged(String key, Place place);
        void onRemoved(String key);
    }
}
