package com.routeal.cocoger.ui.main;

import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.ImageButton;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.Utils;

/**
 * Created by nabe on 9/23/17.
 */

public class InfoFragment extends Fragment {
    protected AppCompatTextView mTitleTextView;
    protected AppCompatImageView mStreetImageView;
    protected AppCompatTextView mAddressTextView;
    protected ImageButton mActionInfoButton;
    protected ImageButton mActionLocationButton;
    protected ImageButton mActionDirectionButton;
    protected ImageButton mActionMessageButton;

    void setupView(Object parent) {
        if (parent instanceof View) {
            View view = (View) parent;
            mTitleTextView = (AppCompatTextView) view.findViewById(R.id.info_title);
            mStreetImageView = (AppCompatImageView) view.findViewById(R.id.info_street_view);
            mAddressTextView = (AppCompatTextView) view.findViewById(R.id.info_address);
            mActionInfoButton = (ImageButton) view.findViewById(R.id.action_info);
            mActionLocationButton = (ImageButton) view.findViewById(R.id.action_location);
            mActionDirectionButton = (ImageButton) view.findViewById(R.id.action_direction);
            mActionMessageButton = (ImageButton) view.findViewById(R.id.action_message);
        } else if (parent instanceof Dialog) {
            Dialog dialog = (Dialog) parent;
            mTitleTextView = (AppCompatTextView) dialog.findViewById(R.id.info_title);
            mStreetImageView = (AppCompatImageView) dialog.findViewById(R.id.info_street_view);
            mAddressTextView = (AppCompatTextView) dialog.findViewById(R.id.info_address);
            mActionInfoButton = (ImageButton) dialog.findViewById(R.id.action_info);
            mActionLocationButton = (ImageButton) dialog.findViewById(R.id.action_location);
            mActionDirectionButton = (ImageButton) dialog.findViewById(R.id.action_direction);
            mActionMessageButton = (ImageButton) dialog.findViewById(R.id.action_message);
        }

    }

    void enableMessageButton(String key) {
        if (FB.isCurrentUser(key)) {
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
        String url = String.format(getResources().getString(R.string.street_view_image_url),
                location.getLatitude(), location.getLongitude());
        new LoadImage.LoadImageView(mStreetImageView, false).execute(url);
    }

    void processStreetView(Location location, String address) {
        Intent intent = new Intent(getContext(), StreetViewActivity.class);
        intent.putExtra("location", Utils.getLatLng(location));
        intent.putExtra("address", address);
        MainApplication.getContext().startActivity(intent);
    }

    void processInfo(Location location) {
        String url = String.format("google.streetview:cbll=%s,%s",
                location.getLatitude(), location.getLongitude());
        Uri gmmIntentUri = Uri.parse(url);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        MainApplication.getContext().startActivity(mapIntent);
    }

    void processLocation() {

    }

    void processDirection(Location location) {
        String url = String.format("google.navigation:q=%s,%s",
                location.getLatitude(), location.getLongitude());
        Uri gmmIntentUri = Uri.parse(url);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        MainApplication.getContext().startActivity(mapIntent);
    }

    void processMessage() {

    }
}
