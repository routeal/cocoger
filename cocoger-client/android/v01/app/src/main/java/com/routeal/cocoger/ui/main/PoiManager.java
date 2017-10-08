package com.routeal.cocoger.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.SystemClock;
import android.support.annotation.NonNull;
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
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.util.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by hwatanabe on 9/27/17.
 */

class PoiManager implements MapActivity.MarkerInterface, GoogleMap.OnPoiClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {
    private final static String TAG = "PoiManager";
    private static PoiColorButton mColorButtons[] = {
            new PoiColorButton(R.id.poi_1, R.id.poi_image_1, R.color.steelblue, "steelblue"),
            new PoiColorButton(R.id.poi_2, R.id.poi_image_2, R.color.yellowgreen, "yellowgreen"),
            new PoiColorButton(R.id.poi_3, R.id.poi_image_3, R.color.firebrick, "firebrick"),
            new PoiColorButton(R.id.poi_4, R.id.poi_image_4, R.color.gold, "gold"),
            new PoiColorButton(R.id.poi_5, R.id.poi_image_5, R.color.hotpink, "hotpink"),
    };
    private static HashMap<String, Integer> mColorMap = new HashMap<String, Integer>() {{
        put("steelblue", R.color.steelblue);
        put("yellowgreen", R.color.yellowgreen);
        put("firebrick", R.color.firebrick);
        put("gold", R.color.gold);
        put("hotpink", R.color.hotpink);
    }};
    Map<Marker, InfoWindow> mPoiMarkers = new HashMap<Marker, InfoWindow>();
    private GoogleMap mMap;
    private GeoDataClient mGeoDataClient;
    private com.appolica.interactiveinfowindow.InfoWindowManager mInfoWindowManager;
    private Marker mMarker;
    private InfoWindow mWindow;

    PoiManager(GoogleMap googleMap, GeoDataClient geoDataClient, com.appolica.interactiveinfowindow.InfoWindowManager infoWindowManager) {
        mMap = googleMap;
        mMap.setOnPoiClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mGeoDataClient = geoDataClient;
        mInfoWindowManager = infoWindowManager;
    }

    @Override
    public void onPoiClick(PointOfInterest pointOfInterest) {
        mGeoDataClient.getPlaceById(pointOfInterest.placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    Place place = places.get(0);
                    addPoiInfoWindow(place);
                    places.release();
                }
            }
        });
    }

    // One time exclusive info window for clicking on Poi in the map, which creates
    // a transparent marker and adds an info window on it.
    private void addPoiInfoWindow(Place place) {
        LatLng pos = place.getLatLng();
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        BitmapDescriptor transparent = BitmapDescriptorFactory.fromBitmap(bitmap);

        MarkerOptions options = new MarkerOptions()
                .position(pos)
                .icon(transparent)
                .anchor((float) 0.5, (float) 0.5);
        mMarker = mMap.addMarker(options);

        final PoiInfoFragment poiInfoFragment = new PoiInfoFragment();
        poiInfoFragment.setTitle(place.getName().toString());
        poiInfoFragment.setLocation(Utils.getLocation(place.getLatLng()));
        poiInfoFragment.setAddress(place.getAddress().toString());
        poiInfoFragment.setPoiManager(PoiManager.this);
        InfoWindow.MarkerSpecification mMarkerOffset = new InfoWindow.MarkerSpecification(0, 0);
        mWindow = new InfoWindow(mMarker, mMarkerOffset, poiInfoFragment);
        mInfoWindowManager.setHideOnFling(true);
        mInfoWindowManager.toggle(mWindow, true);

        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(place.getId());
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                if (photos == null) return;
                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                if (photoMetadataBuffer == null || photoMetadataBuffer.getCount() == 0) return;
                // Get the first photo in the list.
                PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                if (photoMetadata == null || !photoMetadata.isDataValid()) return;
                // Get the attribution text.
                CharSequence attribution = photoMetadata.getAttributions();
                if (attribution == null || attribution.length() == 0) return;
                // Get a full-size bitmap for the photo.
                Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getScaledPhoto(photoMetadata, 128, 128);
                if (!photoMetadata.isDataValid()) return;
                photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                        PlacePhotoResponse photo = task.getResult();
                        if (photo == null) return;
                        Bitmap bitmap = photo.getBitmap();
                        if (bitmap == null) return;
                        poiInfoFragment.setStreetViewPicture(bitmap);
                    }
                });
            }
        });
    }

    void removePoiInfoWindow() {
        if (mWindow != null) {
            mInfoWindowManager.hide(mWindow);
            Fragment fragment = mWindow.getWindowFragment();
            FragmentManager fragmentManager = fragment.getFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction trans = fragmentManager.beginTransaction();
                trans.remove(fragment);
                trans.commit();
            }
            mWindow = null;
        }
        if (mMarker != null) {
            mMarker.remove();
            mMarker = null;
        }
    }

    void addPlace(final Context context, final Location location, String address, String title, final Bitmap bitmap) {
        Toast.makeText(MainApplication.getContext(), "Save To Map:" + title, Toast.LENGTH_SHORT).show();

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_poi, null);

        for (PoiColorButton pc : mColorButtons) {
            pc.imageView = (ImageView) view.findViewById(pc.imageId);
            Drawable drawable = Utils.getIconDrawable(context, R.drawable.ic_place_white_18dp, pc.colorId);
            pc.imageView.setImageDrawable(drawable);
            pc.radioButton = (RadioButton) view.findViewById(pc.id);
            pc.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    for (PoiColorButton pc : mColorButtons) {
                        if (isChecked) {
                            if (pc.radioButton != buttonView) {
                                pc.radioButton.setChecked(false);
                            }
                        }
                    }
                }
            });
        }

        final TextView poiTitle = (TextView) view.findViewById(R.id.poi_title);
        poiTitle.setText(title);

        final TextView poiAddress = (TextView) view.findViewById(R.id.poi_address);
        poiAddress.setText(address);

        final TextView poiDesc = (TextView) view.findViewById(R.id.poi_description);

        final SwitchCompat poiFriend = (SwitchCompat) view.findViewById(R.id.poi_friend);

        ImageView poiImage = (ImageView) view.findViewById(R.id.poi_image);

        if (bitmap != null) {
            poiImage.setImageBitmap(bitmap);
        } else {
            Drawable drawable = Utils.getIconDrawable(context, R.drawable.ic_place_white_48dp, R.color.steelblue);
            poiImage.setImageDrawable(drawable);
        }

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String color = "";
                        for (PoiColorButton pc : mColorButtons) {
                            if (pc.radioButton.isChecked()) {
                                color = pc.colorName;
                                break;
                            }
                        }
                        String title = poiTitle.getText().toString();
                        String description = poiDesc.getText().toString();
                        String address = poiAddress.getText().toString();
                        boolean showFriend = poiFriend.isChecked();
                        Log.d(TAG, String.format("color=%s: title=%s: desc=%s: address=%s: showFriend=%s", color, title, description, address, showFriend ? "true" : "false"));

                        int colorId = mColorMap.get(color);
                        Drawable drawable = Utils.getIconDrawable(context, R.drawable.ic_place_white_48dp, colorId);
                        BitmapDescriptor icon = Utils.getBitmapDescriptor(drawable);

                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(Utils.getLatLng(location))
                                .draggable(true)
                                .icon(icon));
                        dropPinEffect(marker);

                        User user = FB.getUser();

                        MyPoiInfoFragment poiInfoFragment = new MyPoiInfoFragment();
                        poiInfoFragment.setTitle(title);
                        poiInfoFragment.setLocation(location);
                        poiInfoFragment.setAddress(address);
                        poiInfoFragment.setPoiCreator(user.getDisplayName());
                        poiInfoFragment.setStreetViewPicture(bitmap);
                        InfoWindow.MarkerSpecification mMarkerOffset = new InfoWindow.MarkerSpecification(0, 128);
                        InfoWindow window = new InfoWindow(marker, mMarkerOffset, poiInfoFragment);
                        mPoiMarkers.put(marker, window);
                    }
                })
                .show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        for (Iterator<Map.Entry<Marker, InfoWindow>> it = mPoiMarkers.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Marker, InfoWindow> entry = it.next();
            Marker m = entry.getKey();
            if (marker.getId().equals(m.getId())) {
                InfoWindow window = entry.getValue();
                if (window.getWindowFragment() instanceof  MyPoiInfoFragment) {
                    MyPoiInfoFragment fragment = (MyPoiInfoFragment) window.getWindowFragment();
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
                removePoiInfoWindow();
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

        String creator = "";
        if (FB.getUser() != null && FB.getUser().getDisplayName() != null) {
            creator = FB.getUser().getDisplayName();
        }

        MyPoiInfoFragment poiInfoFragment = new MyPoiInfoFragment();
        poiInfoFragment.setAddress("TBD");
        poiInfoFragment.setTitle("Place Name");
        poiInfoFragment.setPoiCreator(creator);
        poiInfoFragment.setLocation(Utils.getLocation(latLng));
        InfoWindow.MarkerSpecification mMarkerOffset = new InfoWindow.MarkerSpecification(0, 128);
        InfoWindow window = new InfoWindow(marker, mMarkerOffset, poiInfoFragment);
        mPoiMarkers.put(marker, window);
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

    private static class PoiColorButton {
        int id;
        int imageId;
        int colorId;
        String colorName;
        RadioButton radioButton;
        ImageView imageView;

        PoiColorButton(int id, int imageId, int colorId, String colorName) {
            this.id = id;
            this.imageId = imageId;
            this.colorId = colorId;
            this.colorName = colorName;
        }
    }
}
