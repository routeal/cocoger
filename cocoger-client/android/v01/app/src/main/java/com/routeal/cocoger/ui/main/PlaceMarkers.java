package com.routeal.cocoger.ui.main;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.manager.PlaceManager;
import com.routeal.cocoger.model.Place;
import com.routeal.cocoger.provider.DBUtil;
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

public class PlaceMarkers {
    private final static String TAG = "PlaceMarkers";

    private final static String DEFAULT_MARKER_COLOR = "light_blue_400";

    private final static int MARKER_SIZE = 48;
    private PlaceColorButton mPlaceColorButtons[] = {
            new PlaceColorButton(R.id.place_1, R.id.place_image_1,
                    R.color.light_blue_400, R.color.black, "light_blue_400"),
            new PlaceColorButton(R.id.place_2, R.id.place_image_2,
                    R.color.red_700, R.color.white, "red_700"),
            new PlaceColorButton(R.id.place_3, R.id.place_image_3,
                    R.color.teal_400, R.color.white, "teal_400"),
            new PlaceColorButton(R.id.place_4, R.id.place_image_4,
                    R.color.amber_400, R.color.black, "amber_400"),
            new PlaceColorButton(R.id.place_5, R.id.place_image_5,
                    R.color.pink_400, R.color.white, "pink_400"),
    };
    // this marker list with infowindow should be destroyed when the ui is destroyed
    private Map<Marker, InfoWindow> mPlaceMarkers = new HashMap<Marker, InfoWindow>();
    // TODO: need to be removed but no idea how to do this
    private ImageView cropImageView;
    private MapActivity mActivity;
    private InfoWindowManager mInfoWindowManager;
    private GoogleMap mMap;

    PlaceMarkers(MapActivity activity, GoogleMap map, InfoWindowManager infoWindowManager) {
        mActivity = activity;
        mMap = map;
        mInfoWindowManager = infoWindowManager;
        setup();
    }

    // icons for color selection
    private Drawable getIcon(int colorId) {
        return Utils.getIconDrawable(mActivity, R.drawable.ic_place_white_18dp, colorId);
    }

    // marker icon
    private void getMarkerIcon(String key, final Place place, Bitmap bitmap, final MarkerListener listener) {
        if (bitmap != null) {
            getMarkerIconImpl(place, Utils.cropCircle(bitmap), listener);
        } else {
            final String name = place.getUid() + "_" + key + "_" + FB.PLACE_IMAGE;
            byte[] bytes = DBUtil.getImage(name);
            if (bytes != null) {
                Bitmap bitmap2 = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                getMarkerIconImpl(place, Utils.cropCircle(bitmap2), listener);
            } else {
                FB.downloadPlaceImage(place.getUid(), key, new FB.DownloadDataListener() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        DBUtil.saveImage(name, bytes);
                        Bitmap bitmap2 = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                        getMarkerIconImpl(place, Utils.cropCircle(bitmap2), listener);
                    }

                    @Override
                    public void onFail(String err) {
                        getMarkerIconImpl(place, null, listener);
                    }
                });
            }
        }
    }

    private int getBackgroundColor(String colorName) {
        for (PlaceColorButton pcb: mPlaceColorButtons) {
            if (pcb.colorName.equals(colorName))
                return pcb.bgColorId;
        }
        return R.color.teal_400;
    }

    private int getTextColor(String colorName) {
        for (PlaceColorButton pcb: mPlaceColorButtons) {
            if (pcb.colorName.equals(colorName))
                return pcb.textColorId;
        }
        return R.color.white;
    }

    // FIXME: too many fixed numbers
    private void getMarkerIconImpl(Place place, Bitmap bitmap, MarkerListener listener) {
        int colorId = getBackgroundColor(place.getMarkerColor());
        Drawable drawable = Utils.getIconDrawable(mActivity, R.drawable.ic_place_white_48dp, colorId);

        float density = mActivity.getResources().getDisplayMetrics().density;

        Paint paint = new Paint();
        paint.setTextSize(14 * density);

        float textWidth = paint.measureText(place.getTitle());

        int width = (int) (MARKER_SIZE * density + textWidth);
        int height = (int) (MARKER_SIZE * density);

        Canvas canvas = new Canvas();
        Bitmap bitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap2);

        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        paint.setAntiAlias(true);
        paint.setColor(ContextCompat.getColor(mActivity, colorId));
        canvas.drawRoundRect(30 * density, 6 * density, width, 28 * density,
                2 * density, 2 * density, paint);

        paint.setColor(ContextCompat.getColor(mActivity, getTextColor(place.getMarkerColor())));
        paint.setTextSize(14 * density);
        canvas.drawText(place.getTitle(), 40 * density, 22 * density, paint);

        if (bitmap != null) {
            Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            Rect dst = new Rect((int) (12 * density), (int) (6 * density),
                    (int) (36 * density), (int) (30 * density));
            canvas.drawBitmap(bitmap, src, dst, null);
        }

        if (listener != null) {
            float anchorU = (float) (MARKER_SIZE) / (float) (bitmap2.getWidth());
            listener.onComplete(anchorU, 1.0f, BitmapDescriptorFactory.fromBitmap(bitmap2));
        }
    }

    private void setup() {
        if (PlaceManager.getPlaces().size() == 0) return;
        for (Map.Entry<String, Place> entry : PlaceManager.getPlaces().entrySet()) {
            String key = entry.getKey();
            Place place = entry.getValue();
            addMarker(key, place, null, false);
        }
    }

    // edit a place or add a new place but the marker is already created by long press
    void updatePlace(final String key, final Place place) {
        if (mActivity.isFinishing()) return;

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
            pc.imageView.setImageDrawable(getIcon(pc.bgColorId));
            pc.radioButton = (RadioButton) view.findViewById(pc.id);
        }
        for (PlaceColorButton pc : mPlaceColorButtons) {
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
                    placeImage.setImageDrawable(getIcon(R.color.indigo_500));
                }
            }
        }).loadPlace(place.getUid(), key);

        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setView(view);
        builder.setCancelable(true);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePlace(key, place, fragment);
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
                    cropImageView = null;
                }
                final Bitmap updateBitmap = bitmap;

                place.setTitle(placeTitle.getText().toString());
                place.setDescription(placeDesc.getText().toString());
                place.setAddress(placeAddress.getText().toString());
                place.setSeenBy(placeFriend.isChecked() ? "friends" : "none");
                place.setMarkerColor(markerColor2);
                place.setUpdated(System.currentTimeMillis());

                byte bytes[] = null;
                if (updateBitmap != null) {
                    bytes = Utils.getBitmapBytes(mActivity, updateBitmap);
                }

                if (isEdit) {
                    // replace the cache image
                    if (bytes != null) {
                        String name = place.getUid() + "_" + key + "_" + FB.PLACE_IMAGE;
                        DBUtil.saveImage(name, bytes);
                    }

                    FB.updatePlace(key, place, bytes, new FB.CompleteListener() {
                        @Override
                        public void onSuccess() {
                            // place
                            fragment.setPlace(place);

                            // marker color
                            final Marker marker = getMarker(fragment);
                            if (marker != null) {
                                getMarkerIcon(key, place, updateBitmap, new MarkerListener() {
                                    @Override
                                    public void onComplete(float anchorU, float anchorV, BitmapDescriptor bitmapDescriptor) {
                                        marker.setIcon(bitmapDescriptor);
                                        marker.setAnchor(anchorU, anchorV);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFail(String err) {
                            Log.d(TAG, "updatePlace: failed:" + err);
                        }
                    });
                } else {
                    final Marker marker = getMarker(fragment);
                    FB.addPlace(place, bytes,
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
                                    Log.d(TAG, "updatePlace: add a place:" + err);
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
    void addPlace(String title, final LatLng location, String address, final Bitmap bitmap) {
        if (mActivity.isFinishing()) return;

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mActivity);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_place, null);

        for (PlaceColorButton pc : mPlaceColorButtons) {
            pc.imageView = (ImageView) view.findViewById(pc.imageId);
            pc.imageView.setImageDrawable(getIcon(pc.bgColorId));
            pc.radioButton = (RadioButton) view.findViewById(pc.id);
        }
        for (PlaceColorButton pc : mPlaceColorButtons) {
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
            placeImage.setImageDrawable(getIcon(R.color.indigo_500));
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
                    cropImageView = null;
                }
                final Bitmap bitmap2 = tmp2;

                final Place place = new Place();
                place.setTitle(placeTitle.getText().toString());
                place.setDescription(placeDesc.getText().toString());
                place.setLatitude(location.latitude);
                place.setLongitude(location.longitude);
                place.setAddress(placeAddress.getText().toString());
                place.setSeenBy(seenFriend.isChecked() ? "friends" : "none");
                place.setMarkerColor(markerColor2);
                place.setCreated(System.currentTimeMillis());
                place.setUid(FB.getUid());

                byte bytes[] = Utils.getBitmapBytes(mActivity, bitmap2);
                FB.addPlace(place, bytes, null);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

    void add(String key, Place place) {
        addMarker(key, place, null, false);
    }

    private void addMarker(String key, Place place, Bitmap bitmap, boolean hasAnimation) {
        final Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(place.getLatitude(), place.getLongitude())));

        getMarkerIcon(key, place, bitmap, new MarkerListener() {
            @Override
            public void onComplete(float anchorU, float anchorV, BitmapDescriptor bitmapDescriptor) {
                marker.setIcon(bitmapDescriptor);
                marker.setAnchor(anchorU, anchorV);
            }
        });

        if (hasAnimation) {
            dropPinEffect(marker);
        }

        // set draggable only on my markers
        marker.setDraggable(place.getUid() != null && place.getUid().equals(FB.getUid()));

        PlaceInfoFragment placeInfoFragment = new PlaceInfoFragment();
        placeInfoFragment.setKey(key);
        placeInfoFragment.setPlace(place);
        placeInfoFragment.setStreetViewPicture(bitmap);
        placeInfoFragment.setPlaceMarkers(this);

        InfoWindow.MarkerSpecification mMarkerOffset = new InfoWindow.MarkerSpecification(0, 86);
        InfoWindow window = new InfoWindow(marker, mMarkerOffset, placeInfoFragment);

        mPlaceMarkers.put(marker, window);
    }

    private void removeFragment(PlaceInfoFragment fragment) {
        Marker marker = getMarker(fragment);
        if (marker != null) {
            InfoWindow window = mPlaceMarkers.get(marker);
            if (window.getWindowState() == InfoWindow.State.SHOWN ||
                    window.getWindowState() == InfoWindow.State.SHOWING) {
                mInfoWindowManager.hide(window, false);
            }
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

    void change(String key, Place place) {
        final Marker marker = getMarker(key);
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
            getMarkerIcon(key, place, null, new MarkerListener() {
                @Override
                public void onComplete(float anchorU, float anchorV, BitmapDescriptor bitmapDescriptor) {
                    marker.setIcon(bitmapDescriptor);
                    marker.setAnchor(anchorU, anchorV);
                }
            });
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

    void deletePlace(String key, Place place) {
        Marker marker = getMarker(key);
        if (marker != null) {
            InfoWindow infoWindow = mPlaceMarkers.get(marker);
//            mInfoWindowManager.hide(infoWindow, false);
            PlaceInfoFragment fragment = (PlaceInfoFragment) infoWindow.getWindowFragment();
            if (fragment != null) {
                deletePlace(key, place, fragment);
            }
        }
    }

    private void deletePlace(final String key, final Place place, final PlaceInfoFragment fragment) {
        if (place != null && !mActivity.isFinishing()) {
            String title = place.getTitle();
            String defaultTitle = mActivity.getResources().getString(R.string.place_remove);
            new AlertDialog.Builder(mActivity)
                    .setTitle((title != null) ? title : defaultTitle)
                    .setMessage(R.string.confirm_place_remove)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deletePlaceImpl(key, place, fragment);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        } else {
            deletePlaceImpl(key, null, fragment);
        }
    }

    // key might be null before the place is added to the server
    private void deletePlaceImpl(final String key, final Place place, PlaceInfoFragment fragment) {
        removeFragment(fragment);
        if (key != null && place != null) {
            FB.deletePlace(key, place, new FB.CompleteListener() {
                @Override
                public void onSuccess() {
                    String name = place.getUid() + "_" + key + "_" + FB.PLACE_IMAGE;
                    DBUtil.deleteImage(name);
                }

                @Override
                public void onFail(String err) {

                }
            });
        }
    }

    void showPlace(GoogleMap map, String key) {
        Marker marker = getMarker(key);
        if (marker == null) {
            return;
        }
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                marker.getPosition(), map.getCameraPosition().zoom));
    }

    private PlaceInfoFragment getPlaceInfoFragment(Place place) {
        for (Map.Entry<Marker, InfoWindow> entry : mPlaceMarkers.entrySet()) {
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
        for (Map.Entry<Marker, InfoWindow> entry : mPlaceMarkers.entrySet()) {
            InfoWindow window = entry.getValue();
            if (window.getWindowFragment() == fragment) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Marker getMarker(String key) {
        for (Map.Entry<Marker, InfoWindow> entry : mPlaceMarkers.entrySet()) {
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

    boolean onMarkerClick(Marker marker) {
        for (Map.Entry<Marker, InfoWindow> entry : mPlaceMarkers.entrySet()) {
            Marker m = entry.getKey();
            if (marker.getId().equals(m.getId())) {
                InfoWindow window = entry.getValue();
                PlaceInfoFragment placeInfoFragment = (PlaceInfoFragment) window.getWindowFragment();
                placeInfoFragment.setPlaceMarkers(this);
                mInfoWindowManager.toggle(window, true);
                return true;
            }
        }
        return false;
    }

    void onMarkerDragEnd(Marker marker) {
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

        place.setUpdated(System.currentTimeMillis());

        String key = fragment.getKey();
        if (key == null || key.isEmpty()) {
            // not saved yet to the database
            return;
        }

        FB.updatePlace(key, place, null, null);
    }

    void onMapLongClick(LatLng latLng) {
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

    void setCropImage(Uri uri) {
        if (cropImageView != null) {
            try {
                InputStream inputStream = mActivity.getContentResolver().openInputStream(uri);
                Drawable drawable = Drawable.createFromStream(inputStream, uri.toString());
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                cropImageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "setCropImage:", e);
            }
        }
    }

    void hideInfoWindow(PlaceInfoFragment fragment) {
        for (Map.Entry<Marker, InfoWindow> entry : mPlaceMarkers.entrySet()) {
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

    interface MarkerListener {
        void onComplete(float anchorU, float anchorV, BitmapDescriptor bitmapDescriptor);
    }

    private class PlaceColorButton {
        int id;
        int imageId;
        int bgColorId;
        int textColorId;
        String colorName;
        RadioButton radioButton;
        ImageView imageView;

        PlaceColorButton(int id, int imageId, int bgColorId, int textColorId, String colorName) {
            this.id = id;
            this.imageId = imageId;
            this.bgColorId = bgColorId;
            this.textColorId = textColorId;
            this.colorName = colorName;
        }
    }
}
