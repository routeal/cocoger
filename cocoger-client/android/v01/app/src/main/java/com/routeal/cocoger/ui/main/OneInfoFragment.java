package com.routeal.cocoger.ui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.routeal.cocoger.R;
import com.routeal.cocoger.util.Utils;

/**
 * Created by nabe on 9/5/17.
 */

public class OneInfoFragment extends InfoFragment implements View.OnClickListener {
    private final static String TAG = "OneInfoFragment";

    private ComboMarker mMarker;
    private ComboMarker.MarkerInfo mMarkerInfo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "OneInfoFragment: onCreateView");

        View view = inflater.inflate(R.layout.fragment_one_info, container, false);
        setupView(view);

        mStreetImageView.setOnClickListener(this);
        mActionInfoButton.setOnClickListener(this);
        mActionLocationButton.setOnClickListener(this);
        mActionDirectionButton.setOnClickListener(this);
        mActionMessageButton.setOnClickListener(this);

        Bundle bundle = getArguments();
        mMarker = bundle.getParcelable("marker");
        mMarkerInfo = mMarker.getOwner();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "OneInfoFragment: onViewCreated");
        enableMessageButton(mMarkerInfo.id);
        setStreetViewPicture(mMarkerInfo.location);
        setTitle(mMarkerInfo.name);
        setAddress(Utils.getAddressLine(mMarkerInfo.address, mMarkerInfo.range));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.info_street_view:
                processStreetView(mMarkerInfo.rangeLocation, mAddressTextView.getText().toString());
                mMarker.hide();
                break;
            case R.id.action_info:
                processInfo(mMarkerInfo.rangeLocation);
                mMarker.hide();
                break;
            case R.id.action_location:
                processLocation();
                mMarker.hide();
                break;
            case R.id.action_direction:
                processDirection(mMarkerInfo.rangeLocation);
                mMarker.hide();
                break;
            case R.id.action_message:
                processMessage();
                mMarker.hide();
                break;
        }
    }
}
