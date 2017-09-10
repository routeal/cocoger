package com.routeal.cocoger.ui.main;

import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.routeal.cocoger.R;
import com.routeal.cocoger.service.MainService;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.Utils;

/**
 * Created by nabe on 9/5/17.
 */

public class SingleInfoFragment extends Fragment implements View.OnClickListener {
    private final static String TAG = "SingleInfoFragment";

    private AppCompatTextView name;
    private AppCompatImageView street_snapshot;
    private AppCompatTextView current_address;
    private AppCompatButton post_facebook;
    private AppCompatButton more_info;
    private AppCompatButton save_to_map;
    private String mId;
    private String mName;
    private Location mLocation;
    private Address mAddress;
    private int mRange;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "MyInfoFragment: onCreateView");

        View view = inflater.inflate(R.layout.infowindow_me, container, false);
        name = (AppCompatTextView) view.findViewById(R.id.name);
        street_snapshot = (AppCompatImageView) view.findViewById(R.id.street_snapshot);
        current_address = (AppCompatTextView) view.findViewById(R.id.current_address);
        post_facebook = (AppCompatButton) view.findViewById(R.id.post_facebook);
        more_info = (AppCompatButton) view.findViewById(R.id.more_info);
        save_to_map = (AppCompatButton) view.findViewById(R.id.save_to_map);
        street_snapshot.setOnClickListener(this);
        post_facebook.setOnClickListener(this);
        more_info.setOnClickListener(this);
        save_to_map.setOnClickListener(this);

        Bundle bundle = getArguments();
        mId = bundle.getString("id");
        mName = bundle.getString("name");
        mLocation = bundle.getParcelable("location");
        mAddress = bundle.getParcelable("address");
        mRange = bundle.getInt("range");

        return view;
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "MyInfoFragment: onViewCreated");

        String url = String.format(getResources().getString(R.string.street_view_image_url),
                mLocation.getLatitude(), mLocation.getLongitude());

        new LoadImage.LoadImageView(street_snapshot, false).execute(url);

        if (mName != null) {
            name.setText(mName);
        }

        if (mAddress != null) {
            String address = Utils.getAddressLine(mAddress, mRange);
            if (address != null) {
                current_address.setText(address);
            }
        }

/*
        GoogleApiClient mGoogleApiClient = MainService.getGoogleApiClient();

        if (mGoogleApiClient != null) {
            PendingResult<PlaceLikelihoodBuffer> result =
                    Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                    for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
                        Log.i(TAG, String.format("Place '%s' has likelihood: %g",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood()));
                        if (placeLikelihood.getLikelihood() >= 0.9 &&
                                placeLikelihood.getPlace().getWebsiteUri() != null &&
                                placeLikelihood.getPlace().getPhoneNumber() != null) {
                            setPlace(placeLikelihood.getPlace());
                            break;
                        }
                    }
                    placeLikelihoods.release();
                }
            });
        }
*/
    }

    // replace this in the info window
    void setPlace(Place place) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.street_snapshot:
                Intent intent = new Intent(getContext(), StreetViewActivity.class);
                intent.putExtra("location", Utils.getLatLng(mLocation));
                startActivity(intent);
                break;
            case R.id.post_facebook:
                break;
            case R.id.more_info:
                Toast.makeText(getContext(), "moreinfo", Toast.LENGTH_SHORT).show();
                break;
            case R.id.save_to_map:
                Toast.makeText(getContext(), "savetomap", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
