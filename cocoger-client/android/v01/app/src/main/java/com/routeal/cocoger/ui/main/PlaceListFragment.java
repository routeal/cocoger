package com.routeal.cocoger.ui.main;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.manager.FriendManager;
import com.routeal.cocoger.manager.PlaceManager;
import com.routeal.cocoger.manager.UpdateListener;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.Place;
import com.routeal.cocoger.util.LoadImage;

import java.util.HashMap;
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

        PlaceManager.setUpdateListener(new UpdateListener<Place>() {
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
                mEmptyTextView.setVisibility(View.INVISIBLE);
            }
            return size;
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final static String TAG = "PlaceListViewHolder";
            private final HashMap<String, Integer> mBackgroundColor = new HashMap<String, Integer>() {{
                put("light_blue_400", R.color.light_blue_50);
                put("red_700", R.color.red_50);
                put("teal_400", R.color.teal_50);
                put("amber_400", R.color.amber_50);
                put("pink_400", R.color.pink_50);
            }};
            private View mView;
            private CardView mCardView;
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
                mCardView = (CardView) itemView.findViewById(R.id.card_view);
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
                if (mPlaceMarkers == null) return;
                if (v == mEditButton) {
                    mPlaceMarkers.updatePlace(mKey, mPlace);
                } else if (v == mRemoveButton) {
                    mPlaceMarkers.deletePlace(mKey, mPlace);
                } else if (v == mTitleText || v == mAddressText || v == mCreatorText || v == mPictureImage) {
                    mPlaceMarkers.showPlace(mMap, mKey);
                    mActivity.closeSlidePanel();
                }
            }

            void bind(Place place, String key) {
                mKey = key;
                mPlace = place;
                mCardView.setBackgroundColor(ContextCompat.getColor(getContext(), mBackgroundColor.get(mPlace.getMarkerColor())));
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