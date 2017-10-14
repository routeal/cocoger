package com.routeal.cocoger.ui.main;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.Utils;

import java.util.Locale;

/**
 * Created by nabe on 9/23/17.
 */

public class InfoFragment extends Fragment {

    protected AppCompatTextView mTitleTextView;
    protected AppCompatImageView mStreetImageView;
    protected AppCompatTextView mAddressTextView;
    protected AppCompatTextView mPlaceCreatorTextView;
    protected ImageButton mActionEditPlaceButton;
    protected ImageButton mActionAddPlaceButton;
    protected ImageButton mActionDirectionButton;
    protected ImageButton mActionMessageButton;
    protected ImageButton mActionGoogleMapButton;

    void setupView(Object parent) {
        if (parent instanceof View) {
            View view = (View) parent;
            mTitleTextView = (AppCompatTextView) view.findViewById(R.id.info_title);
            mStreetImageView = (AppCompatImageView) view.findViewById(R.id.info_street_view);
            mAddressTextView = (AppCompatTextView) view.findViewById(R.id.info_address);
            mPlaceCreatorTextView = (AppCompatTextView) view.findViewById(R.id.info_place_creator);
            mActionAddPlaceButton = (ImageButton) view.findViewById(R.id.action_add_place);
            mActionDirectionButton = (ImageButton) view.findViewById(R.id.action_direction);
            mActionMessageButton = (ImageButton) view.findViewById(R.id.action_message);
            mActionGoogleMapButton = (ImageButton) view.findViewById(R.id.action_googlemap);
            mActionEditPlaceButton = (ImageButton) view.findViewById(R.id.action_edit_place);
        } else if (parent instanceof Dialog) {
            Dialog dialog = (Dialog) parent;
            mTitleTextView = (AppCompatTextView) dialog.findViewById(R.id.info_title);
            mStreetImageView = (AppCompatImageView) dialog.findViewById(R.id.info_street_view);
            mAddressTextView = (AppCompatTextView) dialog.findViewById(R.id.info_address);
            mPlaceCreatorTextView = (AppCompatTextView) dialog.findViewById(R.id.info_place_creator);
            mActionAddPlaceButton = (ImageButton) dialog.findViewById(R.id.action_add_place);
            mActionDirectionButton = (ImageButton) dialog.findViewById(R.id.action_direction);
            mActionMessageButton = (ImageButton) dialog.findViewById(R.id.action_message);
            mActionGoogleMapButton = (ImageButton) dialog.findViewById(R.id.action_googlemap);
            mActionEditPlaceButton = (ImageButton) dialog.findViewById(R.id.action_edit_place);
        }
    }

    void enableMessageButton(String key) {
        if (FB.isCurrentUser(key)) {
            mTitleTextView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.teal300));
            mActionMessageButton.setVisibility(View.GONE);
            mActionDirectionButton.setVisibility(View.GONE);
        } else {
            mActionMessageButton.setVisibility(View.VISIBLE);
            mActionDirectionButton.setVisibility(View.VISIBLE);
        }
    }

    void setTitle(String title) {
        mTitleTextView.setText(title);
    }

    void setAddress(String address) {
        if (address != null) {
            mAddressTextView.setText(address);
        }
    }

    void setStreetViewPicture(Location location) {
        if (location != null) {
            String url = String.format(getResources().getString(R.string.street_view_image_url),
                    location.getLatitude(), location.getLongitude());
            new LoadImage(mStreetImageView).loadUrl(url);
        }
    }

    void setStreetViewPicture(String url) {
        if (url != null) {
            new LoadImage(mStreetImageView).loadUrl(url);
        }
    }

    void setStreetViewPicture(Bitmap picture) {
        if (picture != null) {
            mStreetImageView.setImageBitmap(picture);
        }
    }

    void openStreetView(Location location, String address) {
        Intent intent = new Intent(MainApplication.getContext(), StreetViewActivity.class);
        intent.putExtra("location", Utils.getLatLng(location));
        intent.putExtra("address", address);
        MainApplication.getContext().startActivity(intent);
    }

    void saveLocation(Location location, String address, String title) {
        BitmapDrawable bitmapDrawable = ((BitmapDrawable) mStreetImageView.getDrawable());
        Bitmap bitmap;
        if (bitmapDrawable == null) {
            mStreetImageView.buildDrawingCache();
            bitmap = mStreetImageView.getDrawingCache();
            mStreetImageView.buildDrawingCache(false);
        } else {
            bitmap = bitmapDrawable.getBitmap();
        }
        Intent intent = new Intent(FB.SAVE_PLACE);
        intent.putExtra(FB.LOCATION, location);
        intent.putExtra(FB.ADDRESS, address);
        intent.putExtra(FB.TITLE, title);
        intent.putExtra(FB.IMAGE, bitmap);
        LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
    }

    void showDirection(Location locationTo) {
        Intent intent = new Intent(FB.DIRECTION_ROUTE_ADD);
        intent.putExtra(FB.NEW_LOCATION, locationTo);
        LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
    }

    void processMessage() {
        Toast.makeText(MainApplication.getContext(), "Send Message not implemented", Toast.LENGTH_SHORT).show();
    }

    void showGoogleMap(Location location, String title) {
        Uri gmmIntentUri;
        if (title == null || title.isEmpty()) {
            String url = String.format(Locale.getDefault(), "geo:%f,%f", location.getLatitude(), location.getLongitude());
            gmmIntentUri = Uri.parse(url);
        } else {
            String url = String.format(Locale.getDefault(), "geo:%f,%f?q=", location.getLatitude(), location.getLongitude());
            gmmIntentUri = Uri.parse(url + Uri.encode(title));
        }
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        MainApplication.getContext().startActivity(mapIntent);
    }
}
