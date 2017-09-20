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
import android.widget.ImageButton;
import android.widget.Toast;

import com.franmontiel.fullscreendialog.FullScreenDialogFragment;
import com.google.android.gms.location.places.Place;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
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
    private ImageButton mHistoryButton;
    private ImageButton mSendMessageButton;
    private ImageButton mSendFacebookButton;
    private AppCompatButton mMoreInfoButton;
    private AppCompatButton mSaveMapButton;
    private String mUid;
    private String mName;
    private Location mLocation;
    private Location mRangeLocation;
    private Address mAddress;
    private int mRange;

    private ComboMarker mMarker;
    private ComboMarker.MarkerInfo mMarkerInfo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "OneInfoFragment: onCreateView");

        View view = inflater.inflate(R.layout.fragment_one_info, container, false);
        mNameTextView = (AppCompatTextView) view.findViewById(R.id.title);
        mStreetImageView = (AppCompatImageView) view.findViewById(R.id.street_view);
        mSendMessageButton = (ImageButton) view.findViewById(R.id.send_message);
        mSendFacebookButton = (ImageButton) view.findViewById(R.id.send_facebook);
        mAddressTextView = (AppCompatTextView) view.findViewById(R.id.current_address);
        mMoreInfoButton = (AppCompatButton) view.findViewById(R.id.more_info);
        mSaveMapButton = (AppCompatButton) view.findViewById(R.id.save_to_map);

        mStreetImageView.setOnClickListener(this);
        mMoreInfoButton.setOnClickListener(this);
        mSaveMapButton.setOnClickListener(this);
        mSendMessageButton.setOnClickListener(this);
        mSendFacebookButton.setOnClickListener(this);

        Bundle bundle = getArguments();
        mMarker = bundle.getParcelable("marker");
        mMarkerInfo = mMarker.getOwner();

        mUid = mMarkerInfo.id;
        mName = mMarkerInfo.name;
        mLocation = mMarkerInfo.location;
        mAddress = mMarkerInfo.address;
        mRange = mMarkerInfo.range;
        mRangeLocation = mMarkerInfo.rangeLocation;

        if (FB.isCurrentUser(mUid)) {
            mSendMessageButton.setVisibility(View.GONE);
            mSendFacebookButton.setVisibility(View.VISIBLE);
        } else {
            mSendMessageButton.setVisibility(View.VISIBLE);
            mSendFacebookButton.setVisibility(View.VISIBLE);
        }

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
                mMarker.hide();
                break;
            case R.id.save_to_map:
                Toast.makeText(getContext(), "savetomap", Toast.LENGTH_SHORT).show();
                mMarker.hide();
                break;
        }
    }
}
