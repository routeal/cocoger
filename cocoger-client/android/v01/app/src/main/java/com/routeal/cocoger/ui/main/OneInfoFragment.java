package com.routeal.cocoger.ui.main;

import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
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

import com.google.android.gms.location.places.Place;
import com.routeal.cocoger.R;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.Utils;

/**
 * Created by nabe on 9/5/17.
 */

public class OneInfoFragment extends Fragment implements View.OnClickListener {
    private final static String TAG = "OneInfoFragment";

    private AppCompatTextView mNameTextView;
    private AppCompatImageView mStreetImageView;
    private AppCompatTextView mAddressTextView;
    private AppCompatButton mPostFacebookButton;
    private AppCompatButton mMoreInfoButton;
    private AppCompatButton mSaveMapButton;
    private String mUid;
    private String mName;
    private Location mLocation;
    private Location mRangeLocation;
    private Address mAddress;
    private int mRange;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "MyInfoFragment: onCreateView");

        View view = inflater.inflate(R.layout.fragment_one_info, container, false);
        mNameTextView = (AppCompatTextView) view.findViewById(R.id.name);
        mStreetImageView = (AppCompatImageView) view.findViewById(R.id.street_view);
        mAddressTextView = (AppCompatTextView) view.findViewById(R.id.current_address);
        mPostFacebookButton = (AppCompatButton) view.findViewById(R.id.post_facebook);
        mMoreInfoButton = (AppCompatButton) view.findViewById(R.id.more_info);
        mSaveMapButton = (AppCompatButton) view.findViewById(R.id.save_to_map);

        mStreetImageView.setOnClickListener(this);
        mPostFacebookButton.setOnClickListener(this);
        mMoreInfoButton.setOnClickListener(this);
        mSaveMapButton.setOnClickListener(this);

        Bundle bundle = getArguments();
        mUid = bundle.getString("id");
        mName = bundle.getString("name");
        mLocation = bundle.getParcelable("location");
        mAddress = bundle.getParcelable("address");
        mRange = bundle.getInt("range");
        mRangeLocation = bundle.getParcelable("rangeLocation");

        return view;
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "MyInfoFragment: onViewCreated");

        String url = String.format(getResources().getString(R.string.street_view_image_url),
                mLocation.getLatitude(), mLocation.getLongitude());

        new LoadImage.LoadImageView(mStreetImageView, false).execute(url);

        if (mName != null) {
            mNameTextView.setText(mName);
        }

        if (mAddress != null) {
            String address = Utils.getAddressLine(mAddress, mRange);
            if (address != null) {
                mAddressTextView.setText(address);
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
            case R.id.street_view:
                Intent intent = new Intent(getContext(), StreetViewActivity.class);
                intent.putExtra("location", Utils.getLatLng(mRangeLocation));
                intent.putExtra("address", mAddressTextView.getText().toString());
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
