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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.routeal.cocoger.R;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.Utils;

/**
 * Created by nabe on 9/10/17.
 */

public class MultiInfoFragment extends Fragment implements View.OnClickListener {
    private AppCompatImageView mStreetImageView;
    private AppCompatTextView mAddressTextView;
    private AppCompatButton mMoreInfoButton;
    private AppCompatButton mSaveMapButton;
    private RecyclerView mFriendList;

    private Location mLocation;
    private Location mRangeLocation;
    private Address mAddress;
    private int mRange;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multi_info, container, false);

        mStreetImageView = (AppCompatImageView) view.findViewById(R.id.street_view);
        mAddressTextView = (AppCompatTextView) view.findViewById(R.id.current_address);
        mMoreInfoButton = (AppCompatButton) view.findViewById(R.id.more_info);
        mSaveMapButton = (AppCompatButton) view.findViewById(R.id.save_to_map);
        mFriendList = (RecyclerView) view.findViewById(R.id.friend_list);

        mStreetImageView.setOnClickListener(this);
        mMoreInfoButton.setOnClickListener(this);
        mSaveMapButton.setOnClickListener(this);

        Bundle bundle = getArguments();
        mLocation = bundle.getParcelable("location");
        mAddress = bundle.getParcelable("address");
        mRangeLocation = bundle.getParcelable("rangeLocation");
        mRange = bundle.getInt("range");

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String url = String.format(getResources().getString(R.string.street_view_image_url),
                mRangeLocation.getLatitude(), mRangeLocation.getLongitude());

        new LoadImage.LoadImageView(mStreetImageView, false).execute(url);

        if (mAddress != null) {
            String address = Utils.getAddressLine(mAddress, mRange);
            if (address != null) {
                mAddressTextView.setText(address);
            }
        }
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

            default:
                break;
        }
    }
}
