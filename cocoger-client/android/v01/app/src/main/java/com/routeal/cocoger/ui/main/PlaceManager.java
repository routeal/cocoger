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

    private Map<Marker, InfoWindow> mPlaceMarkers = new HashMap<Marker, InfoWindow>();
    private GoogleMap mMap;
    private GeoDataClient mGeoDataClient;
    private InfoWindowManager mInfoWindowManager;
    private ImageView cropImageView;

    PlaceManager(GoogleMap googleMap, GeoDataClient geoDataClient, InfoWindowManager infoWindowManager) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mGeoDataClient = geoDataClient;
        mInfoWindowManager = infoWindowManager;
    }

    void setup() {
        if (!mPlaceMarkers.isEmpty()) {
            return;
        }

        User user = FB.getUser();
        if (user == null) {
            return; // not available yet
        }

        Map<String, String> places = user.getPlaces();
        if (places == null || places.isEmpty()) {
            return; // empty
        }

        for (Iterator<Map.Entry<String, String>> it = places.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, String> entry = it.next();
            String key = entry.getKey();
            FB.getPlace(key, new FB.PlaceListener() {
                @Override
                public void onSuccess(String key, Place place) {
                    addMarker(place, key, null);
                }

                @Override
                public void onFail(String err) {
                }
            });
        }
    }

    // edit a place or add a new place but the marker is already created by long press
    void editPlace(final Activity activity, final Place place, final String key) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        final PlaceInfoFragment fragment = getPlaceInfoFragment(place);
        if (fragment == null) {
            return;
        }

        // place is from database or not
        final boolean isEdit = (key != null && !key.isEmpty());

        LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());
        Location location = Utils.getLocation(latLng);

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(activity);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_place, null);

        for (PlaceColorButton pc : mPlaceColorButtons) {
            pc.imageView = (ImageView) view.findViewById(pc.imageId);
            Drawable drawable = Utils.getIconDrawable(activity, R.drawable.ic_place_white_18dp, pc.colorId);
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
                        .setActivityTitle(activity.getResources().getString(R.string.picture))
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setRequestedSize(128, 128)
                        .setFixAspectRatio(true)
                        .setAspectRatio(100, 100)
                        .start(activity);
            }
        });

        new LoadImage(new LoadImage.LoadImageListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                if (bitmap != null) {
                    placeImage.setImageBitmap(bitmap);
                } else {
                    Drawable drawable = Utils.getIconDrawable(activity, R.drawable.ic_place_white_48dp, R.color.steelblue);
                    placeImage.setImageDrawable(drawable);
                }
            }
        }).loadPlace(place.getUid(), key);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setCancelable(true);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removePlace(place, key, fragment);
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
                final Bitmap updateBitmap = bitmap;

                place.setTitle(placeTitle.getText().toString());
                place.setDescription(placeDesc.getText().toString());
                place.setAddress(placeAddress.getText().toString());
                place.setSeenBy(placeFriend.isChecked() ? "friends" : "none");
                place.setMarkerColor(markerColor2);

                Marker tmpMarker = null;
                for (Iterator<Map.Entry<Marker, InfoWindow>> it = mPlaceMarkers.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<Marker, InfoWindow> entry = it.next();
                    InfoWindow infoWindow = entry.getValue();
                    if (infoWindow.getWindowFragment() == fragment) {
                        tmpMarker = entry.getKey();
                        break;
                    }
                }
                final Marker marker = tmpMarker;

                if (isEdit) {
                    for (Iterator<Map.Entry<Marker, InfoWindow>> it = mPlaceMarkers.entrySet().iterator(); it.hasNext(); ) {
                        Map.Entry<Marker, InfoWindow> entry = it.next();
                        InfoWindow infoWindow = entry.getValue();
                        if (infoWindow.getWindowFragment() == fragment) {
                            FB.editPlace(key, place, updateBitmap, new FB.CompleteListener() {
                                @Override
                                public void onSuccess() {
                                    if (marker != null) {
                                        int colorId = mPlaceColorMap.get(place.getMarkerColor());
                                        Drawable drawable = Utils.getIconDrawable(activity, R.drawable.ic_place_white_48dp, colorId);
                                        BitmapDescriptor icon = Utils.getBitmapDescriptor(drawable);
                                        marker.setIcon(icon);
                                    }
                                }

                                @Override
                                public void onFail(String err) {

                                }
                            });
                        }
                    }
                } else {
                    FB.addPlace(place, updateBitmap,
                            new FB.PlaceListener() {
                                @Override
                                public void onSuccess(String key, Place place) {
                                    if (marker != null) {
                                        int colorId = mPlaceColorMap.get(place.getMarkerColor());
                                        Drawable drawable = Utils.getIconDrawable(activity, R.drawable.ic_place_white_48dp, colorId);
                                        BitmapDescriptor icon = Utils.getBitmapDescriptor(drawable);
                                        marker.setIcon(icon);
                                    }
                                }

                                @Override
                                public void onFail(String err) {

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
    void addPlace(final Activity activity, String title, final Location location, String address, final Bitmap bitmap) {
        if (activity.isFinishing()) {
            return;
        }

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(activity);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_place, null);

        for (PlaceColorButton pc : mPlaceColorButtons) {
            pc.imageView = (ImageView) view.findViewById(pc.imageId);
            Drawable drawable = Utils.getIconDrawable(activity, R.drawable.ic_place_white_18dp, pc.colorId);
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
                        .setActivityTitle(activity.getResources().getString(R.string.picture))
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setRequestedSize(128, 128)
                        .setFixAspectRatio(true)
                        .setAspectRatio(100, 100)
                        .start(activity);
            }
        });
        if (bitmap != null) {
            placeImage.setImageBitmap(bitmap);
        } else {
            Drawable drawable = Utils.getIconDrawable(activity, R.drawable.ic_place_white_48dp, R.color.steelblue);
            placeImage.setImageDrawable(drawable);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setCancelable(true);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tmp = "steelblue";
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

                FB.addPlace(place, bitmap2,
                        new FB.PlaceListener() {
                            @Override
                            public void onSuccess(String key, Place place) {
                                addMarker(place, key, bitmap2);
                            }

                            @Override
                            public void onFail(String err) {
                            }
                        });
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    private void addMarker(Place place, String key, Bitmap bitmap) {
        int colorId = mPlaceColorMap.get(place.getMarkerColor());
        Drawable drawable = Utils.getIconDrawable(MainApplication.getContext(), R.drawable.ic_place_white_48dp, colorId);
        BitmapDescriptor icon = Utils.getBitmapDescriptor(drawable);

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(place.getLatitude(), place.getLongitude()))
                .draggable(true)
                .icon(icon));
        dropPinEffect(marker);

        PlaceInfoFragment placeInfoFragment = new PlaceInfoFragment();
        placeInfoFragment.setKey(key);
        placeInfoFragment.setPlace(place);
        placeInfoFragment.setStreetViewPicture(bitmap);
        placeInfoFragment.setPlaceManager(PlaceManager.this);
        InfoWindow.MarkerSpecification mMarkerOffset = new InfoWindow.MarkerSpecification(0, 128);
        InfoWindow window = new InfoWindow(marker, mMarkerOffset, placeInfoFragment);
        mPlaceMarkers.put(marker, window);
    }

    void removeFragment(PlaceInfoFragment fragment) {
        for (Iterator<Map.Entry<Marker, InfoWindow>> it = mPlaceMarkers.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Marker, InfoWindow> entry = it.next();
            InfoWindow infoWindow = entry.getValue();
            if (infoWindow.getWindowFragment() == fragment) {
                Marker marker = entry.getKey();
                mInfoWindowManager.hide(infoWindow, true);
                FragmentManager fragmentManager = fragment.getFragmentManager();
                if (fragmentManager != null) {
                    FragmentTransaction trans = fragmentManager.beginTransaction();
                    trans.remove(fragment);
                    trans.commit();
                }
                marker.remove();
                it.remove();
                break;
            }
        }
    }

    void removePlace(Place place, String key, final PlaceInfoFragment fragment) {
        if (key == null || key.isEmpty()) {
            removeFragment(fragment);
            return;
        }

        FB.deletePlace(key, place, new FB.CompleteListener() {
            @Override
            public void onSuccess() {
                removeFragment(fragment);
            }

            @Override
            public void onFail(String err) {

            }
        });
    }

    PlaceInfoFragment getPlaceInfoFragment(Place place) {
        for (Iterator<Map.Entry<Marker, InfoWindow>> it = mPlaceMarkers.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Marker, InfoWindow> entry = it.next();
            InfoWindow window = entry.getValue();
            if (window.getWindowFragment() instanceof PlaceInfoFragment) {
                PlaceInfoFragment fragment = (PlaceInfoFragment) window.getWindowFragment();
                Place place2 = fragment.getPlace();
                if (place.getUid().equals(place2.getUid()) &&
                        place.getCreated() == place2.getCreated() &&
                        place.getLatitude() == place2.getLatitude() &&
                        place.getLongitude() == place2.getLongitude()) {
                    return fragment;
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
                if (window.getWindowFragment() instanceof PlaceInfoFragment) {
                    PlaceInfoFragment fragment = (PlaceInfoFragment) window.getWindowFragment();
                    Place place = fragment.getPlace();
                    // Adjust the info window location when the marker is moved by drag
                    /*
                    if (!marker.getPosition().equals(Utils.getLatLng(place.getLocation()))) {
                        fragment.setLocation(Utils.getLocation(marker.getPosition()));
                        window.setPosition(marker.getPosition());
                    }
                    */
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
        Place place = new Place();
        place.setMarkerColor("steelblue");
        place.setTitle("Place Name");
        place.setDescription("Place Description");
        place.setLatitude(latLng.latitude);
        place.setLongitude(latLng.longitude);
        place.setUid(FB.getUid());

        addMarker(place, null, null);
    }

    void setCropImage(Activity activity, Uri uri) {
        if (cropImageView != null) {
            try {
                InputStream inputStream = activity.getContentResolver().openInputStream(uri);
                Drawable drawable = Drawable.createFromStream(inputStream, uri.toString());
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                cropImageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {

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
}

