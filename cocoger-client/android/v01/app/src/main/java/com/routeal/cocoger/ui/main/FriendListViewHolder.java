package com.routeal.cocoger.ui.main;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.RangeRequest;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.util.CircleTransform;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.SnappingSeekBar;
import com.squareup.picasso.Picasso;

public class FriendListViewHolder extends RecyclerView.ViewHolder {
    private final ImageView mPicture;
    private final TextView mName;
    private final SnappingSeekBar mSeekBar;
    private final View mView;
    private int currentRangePosition;
    private String fid;

    public FriendListViewHolder(View itemView) {
        super(itemView);

        mView = itemView;
        mPicture = (ImageView) itemView.findViewById(R.id.picture);
        mName = (TextView) itemView.findViewById(R.id.name);
        mSeekBar = (SnappingSeekBar) itemView.findViewById(R.id.seekbar);

        mSeekBar.setOnItemSelectionListener(new SnappingSeekBar.OnItemSelectionListener() {
            @Override
            public void onItemSelected(final int itemIndex, String itemString) {
                if (itemIndex > currentRangePosition) {
                    new AlertDialog.Builder(mView.getContext())
                            .setTitle(R.string.notice)
                            .setMessage(R.string.notice_narrow_range_change)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sendChangeRequest(itemIndex);

                                    mSeekBar.setProgressToIndex(currentRangePosition);

                                    new AlertDialog.Builder(mView.getContext())
                                            .setMessage(R.string.range_changed_narrow)
                                            .setPositiveButton(android.R.string.ok, null)
                                            .show();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mSeekBar.setProgressToIndex(currentRangePosition);
                                }
                            })
                            .show();

                } else if (itemIndex < currentRangePosition) {
                    new AlertDialog.Builder(mView.getContext())
                            .setTitle(R.string.notice)
                            .setMessage(R.string.notice_broad_range_change)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    changeRange(itemIndex);
                                    currentRangePosition = itemIndex;
                                    new AlertDialog.Builder(mView.getContext())
                                            .setMessage(R.string.range_changed_broad)
                                            .setPositiveButton(android.R.string.ok, null)
                                            .show();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mSeekBar.setProgressToIndex(currentRangePosition);
                                }
                            })
                            .show();
                }
            }
        });
    }

    public void bind(Friend friend, String key /* friend's key */) {
        fid = key;

        setRange(friend.getRange());

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

        userRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                setName(user.getDisplayName());
                setPicture(user.getPicture());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setName(String name) {
        mName.setText(name);
    }

    private void setRange(int range) {
        currentRangePosition = LocationRange.toPosition(range);
        mSeekBar.setProgressToIndex(currentRangePosition);
    }

    private void setPicture(String url) {
        Picasso.with(MainApplication.getContext())
                .load(url)
                .transform(new CircleTransform())
                .resize(48, 48)
                .into(mPicture);
    }

    private void changeRange(int index) {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = fbUser.getUid();
        int range = LocationRange.toRange(index);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
        userRef.child(uid).child("friends").child(fid).child("range").setValue(range);
        userRef.child(fid).child("friends").child(uid).child("range").setValue(range);
    }

    private void sendChangeRequest(int index) {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = fbUser.getUid();
        int range = LocationRange.toRange(index);
        RangeRequest rangeRequest = new RangeRequest();
        rangeRequest.setCreated(System.currentTimeMillis());
        rangeRequest.setRange(range);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
        userRef.child(fid).child("friends").child(uid).child("request").setValue(rangeRequest);
    }

}

