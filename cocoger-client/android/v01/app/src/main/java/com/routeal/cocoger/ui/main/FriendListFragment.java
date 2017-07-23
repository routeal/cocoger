package com.routeal.cocoger.ui.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.routeal.cocoger.R;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.util.CircleTransform;
import com.routeal.cocoger.util.SnappingSeekBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by nabe on 7/22/17.
 */

public class FriendListFragment extends Fragment {
    private final static String TAG = "FriendListFragment";

    class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

        List<Friend> friendList;

        class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView image;
            public TextView name;
            public SnappingSeekBar seekbar;

            public ViewHolder(View view) {
                super(view);
                image = (ImageView) view.findViewById(R.id.picture);
                name = (TextView) view.findViewById(R.id.name);
                seekbar = (SnappingSeekBar) view.findViewById(R.id.seekbar);
            }
        }

        public FriendListAdapter(List<Friend> friendList) {
            this.friendList = friendList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_friend, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Friend friend = friendList.get(position);
            holder.name.setText(friend.getName());
            holder.seekbar.setProgressToIndex(3);
            Picasso.with(getApplicationContext())
                    .load(friend.getPicture())
                    .transform(new CircleTransform())
                    .into(holder.image);
        }

        @Override
        public int getItemCount() {
            Log.d(TAG, "getItemCount:" + friendList.size());
            return friendList.size();
        }
    }

    public FriendListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private FriendListAdapter mAdapter;
    private List<Friend> friendList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);

        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(mLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));

        mAdapter = new FriendListAdapter(friendList);
        rv.setAdapter(mAdapter);

        prepareData();

        // Inflate the layout for this fragment
        return view;
    }

    private void prepareData() {
        Friend f = new Friend();
        f.setName("Hiroshi Watanabe");
        f.setPicture("https://scontent.xx.fbcdn.net/v/t1.0-1/p50x50/12239505_1621359888117408_5802586652468635863_n.jpg?oh=c668dc917adb3dd1c952c261197bc222&oe=5A0EAEA4");
        friendList.add(f);

        f = new Friend();
        f.setName("Pure Leaf");
        f.setPicture("https://scontent.xx.fbcdn.net/v/t1.0-1/p50x50/12239505_1621359888117408_5802586652468635863_n.jpg?oh=c668dc917adb3dd1c952c261197bc222&oe=5A0EAEA4");
        friendList.add(f);

        mAdapter.notifyDataSetChanged();
    }
}
