package com.routeal.cocoger.ui.main;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Place;
import com.routeal.cocoger.util.LoadImage;

/**
 * Created by hwatanabe on 10/9/17.
 */

public class PlaceListViewHolder extends RecyclerView.ViewHolder {
    private final static String TAG = "PlaceListViewHolder";

    private View mView;
    private ImageView mPictureImage;
    private TextView mTitleText;
    private TextView mAddressText;
    private ImageButton mEditButton;
    private ImageButton mRemoveButton;

    public PlaceListViewHolder(View itemView) {
        super(itemView);

        mView = itemView;

        mPictureImage = (ImageView) itemView.findViewById(R.id.place_picture);
        mTitleText = (TextView) itemView.findViewById(R.id.place_title);
        mAddressText = (TextView) itemView.findViewById(R.id.place_address);
        mEditButton = (ImageButton) itemView.findViewById(R.id.place_edit);
        mRemoveButton = (ImageButton) itemView.findViewById(R.id.place_remove);

        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FB.EDIT_PLACE);
                //intent.putExtra(FB.LOCATION, location);
                LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
            }
        });
    }

    public void bind(String key, String uid) {
        Log.d(TAG, "key:" + key + " uid=" + uid);

        FB.getPlace(key, new FB.PlaceListener() {
            @Override
            public void onSuccess(Place place) {
                mTitleText.setText(place.getTitle());
                mAddressText.setText(place.getAddress());
                if (place.getPicture() != null) {
                    new LoadImage(mPictureImage).loadPlace(place.getKey(), place.getCreated());
                }
            }

            @Override
            public void onFail(String err) {

            }
        });
    }
}
