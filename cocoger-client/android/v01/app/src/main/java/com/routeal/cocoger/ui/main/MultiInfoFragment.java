package com.routeal.cocoger.ui.main;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
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

public class MultiInfoFragment extends Fragment {
    private final static String TAG = "MultiInfoFragment";

    private RecyclerView mFriendList;
    private ComboMarker mMarker;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multi_info, container, false);

        mFriendList = (RecyclerView) view.findViewById(R.id.friend_list);

        Bundle bundle = getArguments();
        mMarker = bundle.getParcelable("marker");

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

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView picture;
            TextView name;
            TextView range;

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
                picture = (ImageView) itemView.findViewById(R.id.picture);
                name = (TextView) itemView.findViewById(R.id.title);
                range = (TextView) itemView.findViewById(R.id.range);
                picture.setOnClickListener(listener);
                name.setOnClickListener(listener);
                range.setOnClickListener(listener);
            }
        }

        void showDialog(final ComboMarker.MarkerInfo info) {
            final Dialog dialog = new Dialog(getActivity());
            dialog.setContentView(R.layout.fragment_one_info);
            //dialog.setCancelable(true);
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            AppCompatTextView mNameTextView = (AppCompatTextView) dialog.findViewById(R.id.title);
            AppCompatImageView mStreetImageView = (AppCompatImageView) dialog.findViewById(R.id.street_view);
            ImageButton mSendMessageButton = (ImageButton) dialog.findViewById(R.id.send_message);
            ImageButton mSendFacebookButton = (ImageButton) dialog.findViewById(R.id.send_facebook);
            AppCompatTextView mAddressTextView = (AppCompatTextView) dialog.findViewById(R.id.current_address);
            AppCompatButton mMoreInfoButton = (AppCompatButton) dialog.findViewById(R.id.more_info);
            AppCompatButton mSaveMapButton = (AppCompatButton) dialog.findViewById(R.id.save_to_map);

            if (FB.isCurrentUser(info.id)) {
                mSendMessageButton.setVisibility(View.GONE);
                mSendFacebookButton.setVisibility(View.VISIBLE);
            } else {
                mSendMessageButton.setVisibility(View.VISIBLE);
                mSendFacebookButton.setVisibility(View.VISIBLE);
            }

            String url = String.format(getResources().getString(R.string.street_view_image_url),
                    info.rangeLocation.getLatitude(), info.rangeLocation.getLongitude());

            new LoadImage.LoadImageView(mStreetImageView, false).execute(url);

            mNameTextView.setText(info.name);

            final String addressText = Utils.getAddressLine(info.address, info.range);

            mAddressTextView.setText(addressText);

            mMoreInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            mSaveMapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            mStreetImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(dialog.getContext(), StreetViewActivity.class);
                    intent.putExtra("location", Utils.getLatLng(info.rangeLocation));
                    intent.putExtra("address", addressText);
                    dialog.getContext().startActivity(intent);
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
            new LoadImage.LoadImageView(holder.picture).execute(info.picture);
            holder.name.setText(info.name);
            String uid = FB.getUid();
            if (uid != null && uid.equals(info.id)) {
                //holder.range.setText("me");
                holder.range.setText("");
            } else {
                holder.range.setText(LocationRange.toString(info.range));
            }
        }

        @Override
        public int getItemCount() {
            return mMarkerInfoList.size();
        }
    }
}
