package com.routeal.cocoger.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by nabe on 9/10/17.
 */

public class MultiInfoFragment extends Fragment implements View.OnClickListener {
    private final static String TAG = "MultiInfoFragment";

    private AppCompatImageView mStreetImageView;
    private AppCompatTextView mAddressTextView;
    private AppCompatButton mMoreInfoButton;
    private AppCompatButton mSaveMapButton;
    private RecyclerView mFriendList;

    private ComboMarker mMarker;
    private ComboMarker.MarkerInfo mMarkerInfo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multi_info, container, false);

/*
        mStreetImageView = (AppCompatImageView) view.findViewById(R.id.street_view);
        mAddressTextView = (AppCompatTextView) view.findViewById(R.id.current_address);
        mMoreInfoButton = (AppCompatButton) view.findViewById(R.id.more_info);
        mSaveMapButton = (AppCompatButton) view.findViewById(R.id.save_to_map);
*/
        mFriendList = (RecyclerView) view.findViewById(R.id.friend_list);

/*
        mStreetImageView.setOnClickListener(this);
        mMoreInfoButton.setOnClickListener(this);
        mSaveMapButton.setOnClickListener(this);
*/

        Bundle bundle = getArguments();
        mMarker = bundle.getParcelable("marker");
        mMarkerInfo = mMarker.getOwner();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

/*
        String url = String.format(getResources().getString(R.string.street_view_image_url),
                mMarkerInfo.rangeLocation.getLatitude(), mMarkerInfo.rangeLocation.getLongitude());

        new LoadImage.LoadImageView(mStreetImageView, false).execute(url);

        if (mMarkerInfo.address != null) {
            String address = Utils.getAddressLine(mMarkerInfo.address, mMarkerInfo.range);
            if (address != null) {
                mAddressTextView.setText(address);
            }
        }
*/

        Map<String, ComboMarker.MarkerInfo> markerInfoMap = mMarker.getInfo();
        if (markerInfoMap != null) {
            List<ComboMarker.MarkerInfo> markerInfos = new ArrayList<>(markerInfoMap.values());
            /* just for testing
            for (int j = 0; j < 3; j++) {
                int size = markerInfos.size();
                for (int i = 0; i < size; i++) {
                    markerInfos.add(markerInfos.get(i));
                }
            }
            */
            if (markerInfos.size() > 0) {
                MarkerAdapter adapter = new MarkerAdapter(markerInfos);
                mFriendList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                mFriendList.setHasFixedSize(true);
                mFriendList.setAdapter(adapter);
            }
        }

    }

    class MarkerAdapter extends RecyclerView.Adapter<MarkerAdapter.ViewHolder> {
        List<ComboMarker.MarkerInfo> mMarkerInfoList;

        MarkerAdapter(List<ComboMarker.MarkerInfo> markerInfoList) {
            mMarkerInfoList = markerInfoList;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView picture;
            TextView name;
            TextView range;

            public ViewHolder(View itemView) {
                super(itemView);
                picture = (ImageView) itemView.findViewById(R.id.picture);
                name = (TextView) itemView.findViewById(R.id.name);
                range = (TextView) itemView.findViewById(R.id.range);

                picture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "picture onclick");
                        ComboMarker.MarkerInfo info = mMarkerInfoList.get(getLayoutPosition());
                        Intent intent = new Intent(MainApplication.getContext(), PanelMapActivity.class);
                        intent.setAction("show_friend");
                        intent.putExtra("id", info.id);
                        LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);

                        mMarker.hide();
                    }
                });

                name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "name onclick");
                        ComboMarker.MarkerInfo info = mMarkerInfoList.get(getLayoutPosition());
                        Intent intent = new Intent(getContext(), PanelMapActivity.class);
                        intent.setAction("show_friend");
                        intent.putExtra("id", info.id);
                        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

                        mMarker.hide();
                    }
                });
            }
        }

        public MarkerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_info_list, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MarkerAdapter.ViewHolder holder, int position) {
            ComboMarker.MarkerInfo info = mMarkerInfoList.get(position);
            new LoadImage.LoadImageView(holder.picture).execute(info.picture);
            holder.name.setText(info.name);
            String uid = FB.getUid();
            if (uid != null && uid.equals(info.id)) {
                //holder.range.setText("me");
            } else {
                holder.range.setText(LocationRange.toString(info.range));
            }
        }

        @Override
        public int getItemCount() {
            return mMarkerInfoList.size();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.street_view:
                Intent intent = new Intent(getContext(), StreetViewActivity.class);
                intent.putExtra("location", Utils.getLatLng(mMarkerInfo.rangeLocation));
                intent.putExtra("address", mAddressTextView.getText().toString());
                startActivity(intent);
                break;

            default:
                break;
        }
    }
}
