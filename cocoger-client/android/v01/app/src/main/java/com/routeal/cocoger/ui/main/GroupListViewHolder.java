package com.routeal.cocoger.ui.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Group;
import com.routeal.cocoger.model.Member;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by hwatanabe on 10/22/17.
 */

public class GroupListViewHolder extends RecyclerView.ViewHolder {

    private final static HashMap<String, Integer> mGroupColorMap = new HashMap<String, Integer>() {{
        put("steelblue", R.color.steelblue);
        put("yellowgreen", R.color.yellowgreen);
        put("firebrick", R.color.firebrick);
        put("gold", R.color.gold);
        put("hotpink", R.color.hotpink);
    }};
    private View mViiew;
    private ImageView mImage;
    private TextView mGroupName;
    private RecyclerView mRecyclerView;
    private String mKey;
    private Group mGroup;
    private Button mJoinButton;
    private Button mDeclineButton;
    private ImageButton mEditButton;
    private ImageButton mRemoveButton;
    private List<String> mMembers;

    public GroupListViewHolder(final View itemView) {
        super(itemView);
        mViiew = itemView;
        mImage = (ImageView) itemView.findViewById(R.id.image);
        mGroupName = (TextView) itemView.findViewById(R.id.name);
        mRecyclerView = (RecyclerView) itemView.findViewById(R.id.list);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mJoinButton = (Button) itemView.findViewById(R.id.join);
        mJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FB.joinGroup(mKey);
            }
        });
        mDeclineButton = (Button) itemView.findViewById(R.id.decline);
        mDeclineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FB.removeGroup(mKey);
            }
        });
        mEditButton = (ImageButton) itemView.findViewById(R.id.edit);
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FB.GROUP_EDIT);
                intent.putExtra("key", mKey);
                intent.putExtra("group", mGroup);
                LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
            }
        });
        mRemoveButton = (ImageButton) itemView.findViewById(R.id.remove);
        mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FB.deleteGroup(mKey, mGroup);
            }
        });
    }

    public void bind(String key, Group group) {
        mKey = key;
        mGroup = group;
        // list of active members
        mMembers = new ArrayList<>();
        boolean isActiveMember = false;
        int colorId = mGroupColorMap.get(group.getColor());
        // show only the current active members, getMembers() returns invited members
        // who do not accept to join the group
        for (Map.Entry<String, Member> entry : mGroup.getMembers().entrySet()) {
            String uid = entry.getKey();
            Member member = entry.getValue();
            if (member.getStatus() == Member.CREATED || member.getStatus() == Member.JOINED) {
                mMembers.add(uid);
                if (!isActiveMember) { // not set false again
                    isActiveMember = uid.equals(FB.getUid());
                }
            }
        }
        if (isActiveMember) {
            mDeclineButton.setVisibility(View.GONE);
            mJoinButton.setVisibility(View.GONE);
            mEditButton.setVisibility(View.VISIBLE);
            mRemoveButton.setVisibility(View.VISIBLE);
        } else {
            mDeclineButton.setVisibility(View.VISIBLE);
            mJoinButton.setVisibility(View.VISIBLE);
            mEditButton.setVisibility(View.INVISIBLE);
            mRemoveButton.setVisibility(View.INVISIBLE);
        }
        String size = String.format(Locale.getDefault(), "%d", mMembers.size());
        Bitmap bitmap = Utils.createImage(48, 48, ContextCompat.getColor(mViiew.getContext(), colorId), size);
        mImage.setImageBitmap(bitmap);
        mGroupName.setText(group.getName());
        mRecyclerView.setAdapter(new UserListAdapter());
    }

    class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {
        @Override
        public UserListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_image_list, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(UserListAdapter.ViewHolder holder, int position) {
            String key = mMembers.get(position);
            new LoadImage(holder.mImage).loadProfile(key);
        }

        @Override
        public int getItemCount() {
            return mMembers.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView mImage;

            ViewHolder(View itemView) {
                super(itemView);
                mImage = (ImageView) itemView.findViewById(R.id.image);
            }
        }
    }
}
