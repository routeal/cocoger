package com.routeal.cocoger.ui.main;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

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

public class MultiInfoFragment extends InfoFragment {
    private final static String TAG = "MultiInfoFragment";

    private RecyclerView mFriendList;

    private ComboMarker mMarker;

    void setMarker(ComboMarker marker) {
        mMarker = marker;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multi_info, container, false);

        mFriendList = (RecyclerView) view.findViewById(R.id.friend_list);

        // 4, 16, 32 are from the xml file
        int size = (mMarker.size() > 4) ? 4 : mMarker.size();
        int height = (size * (int) (32 * Resources.getSystem().getDisplayMetrics().density))
                + (int) (16 * Resources.getSystem().getDisplayMetrics().density);

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        void showDialog(final ComboMarker.MarkerInfo info) {
            final Dialog dialog = new Dialog(getActivity());
            dialog.setContentView(R.layout.fragment_one_info);
            dialog.setCancelable(true);
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            setupView(dialog);
            mPlaceCreatorTextView.setVisibility(View.GONE);
            mActionEditPlaceButton.setVisibility(View.GONE);
            enableMessageButton(info.id);
            setStreetViewPicture(info.rangeLocation);
            setTitle(info.name);
            setAddress(Utils.getAddressLine(info.address, info.range));

            mActionAddPlaceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveLocation(info.rangeLocation, Utils.getAddressLine(info.address, info.range), info.name);
                    dialog.dismiss();
                }
            });

            mActionDirectionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDirection(info.rangeLocation);
                    dialog.dismiss();
                }
            });

            mActionMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startMessage();
                    dialog.dismiss();
                }
            });

            mStreetImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openStreetView(info.rangeLocation, mAddressTextView.getText().toString());
                    dialog.dismiss();
                }
            });

            mActionGoogleMapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showGoogleMap(info.rangeLocation, null);
                    dialog.dismiss();
                }
            });

            dialog.show();
        }

        public MarkerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_info_list, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MarkerAdapter.ViewHolder holder, int position) {
            ComboMarker.MarkerInfo info = mMarkerInfoList.get(position);
            new LoadImage(holder.picture).loadProfile(info.id);
            holder.name.setText(info.name);
            String uid = FB.getUid();
            if (uid.equals(info.id)) {
                holder.range.setText(R.string.me);
                holder.pane.setBackgroundColor(ContextCompat.getColor(getParentFragment().getContext(), R.color.teal_50));
            } else {
                holder.range.setText(LocationRange.toString(info.range));
            }
        }

        @Override
        public int getItemCount() {
            return mMarkerInfoList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView picture;
            TextView name;
            TextView range;
            View pane;

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ComboMarker.MarkerInfo info = mMarkerInfoList.get(getLayoutPosition());
                    showDialog(info);
                    mMarker.hide();
                }
            };

            public ViewHolder(View itemView) {
                super(itemView);
                pane = itemView;
                picture = (ImageView) itemView.findViewById(R.id.picture);
                name = (TextView) itemView.findViewById(R.id.title);
                range = (TextView) itemView.findViewById(R.id.range);
                picture.setOnClickListener(listener);
                name.setOnClickListener(listener);
                range.setOnClickListener(listener);
            }
        }
    }
}
