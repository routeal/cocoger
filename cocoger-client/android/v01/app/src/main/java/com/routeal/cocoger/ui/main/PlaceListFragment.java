package com.routeal.cocoger.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.Place;
import com.routeal.cocoger.util.LoadImage;

import java.util.Set;

/**
 * Created by hwatanabe on 10/8/17.
 */

public class PlaceListFragment extends PagerFragment {

    private RecyclerView mRecyclerView;
    private TextView mEmptyTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_place_list, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        layoutManager.setReverseLayout(false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(layoutManager);

        mEmptyTextView = (TextView) view.findViewById(R.id.empty_view);

        final PlaceListAdapter placeListAdapter = new PlaceListAdapter();
        PlaceManager.setRecyclerAdapterListener(new RecyclerAdapterListener<Place>() {
            @Override
            public void onAdded(String key, Place object) {
                placeListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChanged(String key, Place object) {
                placeListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onRemoved(String key) {
                placeListAdapter.notifyDataSetChanged();
            }
        });

        mRecyclerView.setAdapter(placeListAdapter);

        return view;
    }

    class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.ViewHolder> {
        @Override
        public PlaceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_place_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PlaceListAdapter.ViewHolder holder, int position) {
            Set<String> keySet = PlaceManager.getPlaces().keySet();
            String[] keys = keySet.toArray(new String[0]);
            String key = keys[position];
            Place place = PlaceManager.getPlace(key);
            holder.bind(place, key);
        }

        @Override
        public int getItemCount() {
            int size = PlaceManager.getPlaces().size();
            if (size == 0) {
                mEmptyTextView.setVisibility(View.VISIBLE);
            } else {
                mEmptyTextView.setVisibility(View.GONE);
            }
            return size;
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final static String TAG = "PlaceListViewHolder";

            private View mView;
            private ImageView mPictureImage;
            private TextView mTitleText;
            private TextView mAddressText;
            private TextView mCreatorText;
            private ImageButton mEditButton;
            private ImageButton mRemoveButton;
            private Place mPlace;
            private String mKey;

            ViewHolder(View itemView) {
                super(itemView);
                mView = itemView;
                mPictureImage = (ImageView) itemView.findViewById(R.id.place_picture);
                mTitleText = (TextView) itemView.findViewById(R.id.place_title);
                mAddressText = (TextView) itemView.findViewById(R.id.place_address);
                mCreatorText = (TextView) itemView.findViewById(R.id.place_creator);
                mEditButton = (ImageButton) itemView.findViewById(R.id.place_edit);
                mRemoveButton = (ImageButton) itemView.findViewById(R.id.place_remove);

                mPictureImage.setOnClickListener(this);
                mTitleText.setOnClickListener(this);
                mAddressText.setOnClickListener(this);
                mCreatorText.setOnClickListener(this);
                mEditButton.setOnClickListener(this);
                mRemoveButton.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (v == mEditButton) {
                    Intent intent = new Intent(FB.PLACE_EDIT);
                    intent.putExtra(FB.KEY, mKey);
                    intent.putExtra(FB.PLACE, mPlace);
                    LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
                } else if (v == mRemoveButton) {
                    Intent intent = new Intent(FB.PLACE_DELETE);
                    intent.putExtra(FB.KEY, mKey);
                    intent.putExtra(FB.PLACE, mPlace);
                    LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
                } else if (v == mTitleText || v == mAddressText || v == mCreatorText || v == mPictureImage) {
                    Intent intent = new Intent(FB.PLACE_SHOW);
                    intent.putExtra(FB.KEY, mKey);
                    LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
                }
            }

            public void bind(Place place, String key) {
                mKey = key;
                mPlace = place;
                if (!mPlace.getUid().equals(FB.getUid())) {
                    mEditButton.setVisibility(View.INVISIBLE);
                    mRemoveButton.setVisibility(View.INVISIBLE);
                    Friend friend = FriendManager.getFriend(mPlace.getUid());
                    if (friend != null) {
                        String str = String.format(mView.getResources().getString(R.string.by_creator), friend.getDisplayName());
                        mCreatorText.setText(str);
                    }
                } else {
                    mEditButton.setVisibility(View.VISIBLE);
                    mRemoveButton.setVisibility(View.VISIBLE);
                    String str = String.format(mView.getResources().getString(R.string.by_creator), FB.getUser().getDisplayName());
                    mCreatorText.setText(str);
                }
                mTitleText.setText(place.getTitle());
                mAddressText.setText(place.getAddress());
                new LoadImage(mPictureImage).loadPlace(place.getUid(), key);
            }
        }
    }
}