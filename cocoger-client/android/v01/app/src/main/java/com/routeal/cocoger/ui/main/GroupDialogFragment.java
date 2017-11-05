package com.routeal.cocoger.ui.main;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.franmontiel.fullscreendialog.FullScreenDialogContent;
import com.franmontiel.fullscreendialog.FullScreenDialogController;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.manager.FriendManager;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.Group;
import com.routeal.cocoger.model.Member;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hwatanabe on 10/18/17.
 */

public class GroupDialogFragment extends Fragment implements FullScreenDialogContent {

    private static GroupColorButton mGroupColorButtons[] = {
            new GroupColorButton(R.id.group_color_1, R.id.group_color_image_1,
                    R.color.indigo_500, R.color.white, "indigo_500"),
            new GroupColorButton(R.id.group_color_2, R.id.group_color_image_2,
                    R.color.red_900, R.color.white, "red_900"),
            new GroupColorButton(R.id.group_color_3, R.id.group_color_image_3,
                    R.color.teal_a_700, R.color.black, "teal_a_700"),
            new GroupColorButton(R.id.group_color_4, R.id.group_color_image_4,
                    R.color.amber_a_400, R.color.black, "amber_a_400"),
            new GroupColorButton(R.id.group_color_5, R.id.group_color_image_5,
                    R.color.pink_a_400, R.color.white, "pink_a_400"),
    };
    private static String DEFAULT_GROUP_COLOR = mGroupColorButtons[0].colorName;
    List<String> mGroupKeys;
    List<Friend> mGroupList;
    List<String> mFriendKeys;
    List<Friend> mFriendList;
    private RecyclerView mGroupRecyclerView;
    private GroupAdapter mGroupAdapter;
    private RecyclerView mFriendRecyclerView;
    private FriendAdapter mFriendAdapter;
    private TextInputEditText mGroupName;
    private String mKey;
    private Group mGroup;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_dialog, container, false);

        mKey = getArguments().getString("key");
        mGroup = (Group) getArguments().getSerializable("group");

        mGroupName = (TextInputEditText) view.findViewById(R.id.group_name);

        if (mGroup != null) {
            mGroupName.setText(mGroup.getName());
        }

        for (GroupColorButton pc : mGroupColorButtons) {
            int size = (int) (24 * Resources.getSystem().getDisplayMetrics().density);
            Bitmap bitmap = Utils.createCircleNumberImage(size, size, ContextCompat.getColor(getContext(), pc.bgColorId), "");
            pc.imageView = (ImageView) view.findViewById(pc.imageId);
            pc.imageView.setImageBitmap(bitmap);
            pc.radioButton = (RadioButton) view.findViewById(pc.id);
            pc.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    for (GroupColorButton pc : mGroupColorButtons) {
                        if (isChecked) {
                            if (pc.radioButton != null && pc.radioButton != buttonView) {
                                pc.radioButton.setChecked(false);
                            }
                        }
                    }
                }
            });
            if (mGroup != null) {
                pc.radioButton.setChecked(pc.colorName.equals(mGroup.getColor()));
            }
        }

        mGroupRecyclerView = (RecyclerView) view.findViewById(R.id.group_list);
        mGroupRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mGroupKeys = new ArrayList<>();
        mGroupList = new ArrayList<>();

        if (mGroup != null && !mGroup.getMembers().isEmpty()) {
            for (Map.Entry<String, Member> entry : mGroup.getMembers().entrySet()) {
                String key = entry.getKey();
                // do not count myself
                if (key.equals(FB.getUid())) {
                    continue;
                }
                Friend friend = FriendManager.getFriend(key);
                mGroupKeys.add(key);
                mGroupList.add(friend);
            }
        }

        mGroupAdapter = new GroupAdapter();
        mGroupRecyclerView.setAdapter(mGroupAdapter);

        mFriendRecyclerView = (RecyclerView) view.findViewById(R.id.friend_list);
        mFriendRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mFriendKeys = new ArrayList<>();
        mFriendList = new ArrayList<>();

        for (Map.Entry<String, Friend> entry : FriendManager.getFriends().entrySet()) {
            mFriendKeys.add(entry.getKey());
            mFriendList.add(entry.getValue());
        }

        if (mGroup != null && !mGroup.getMembers().isEmpty()) {
            for (Map.Entry<String, Member> entry : mGroup.getMembers().entrySet()) {
                String key = entry.getKey();
                Friend friend = FriendManager.getFriend(key);
                mFriendKeys.remove(key);
                mFriendList.remove(friend);
            }
        }

        mFriendAdapter = new FriendAdapter();
        mFriendRecyclerView.setAdapter(mFriendAdapter);

        return view;
    }

    @Override
    public void onDialogCreated(FullScreenDialogController dialogController) {

    }

    @Override
    public boolean onConfirmClick(FullScreenDialogController dialogController) {
        String groupName = mGroupName.getText().toString();

        if (groupName.isEmpty()) {
            mGroupName.setError("Group name not entered");
            return true;
        }

        mGroupName.setError(null);

        if (mKey == null && mGroup == null) {
            if (mGroupKeys.isEmpty()) {
                Toast.makeText(getActivity(), "Member not selected", Toast.LENGTH_SHORT).show();
                return true;
            }
        }

        String tmp = DEFAULT_GROUP_COLOR;
        for (GroupColorButton pc : mGroupColorButtons) {
            if (pc.radioButton.isChecked()) {
                tmp = pc.colorName;
                break;
            }
        }
        String groupColor = tmp;

        if (mKey == null && mGroup == null) {
            FB.createGroup(groupName, groupColor, mGroupKeys);
        } else {
            FB.updateGroup(mKey, mGroup, groupName, groupColor, mGroupKeys);
        }

        return false;
    }

    @Override
    public boolean onDiscardClick(FullScreenDialogController dialogController) {
        return false;
    }

    private static class GroupColorButton {
        int id;
        int imageId;
        int bgColorId;
        int textColorId;
        String colorName;
        RadioButton radioButton;
        ImageView imageView;

        GroupColorButton(int id, int imageId, int bgColorId, int textColorId, String colorName) {
            this.id = id;
            this.imageId = imageId;
            this.bgColorId = bgColorId;
            this.textColorId = textColorId;
            this.colorName = colorName;
        }
    }

    class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_simple_friend, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final Friend friend = mGroupList.get(position);
            holder.displayName.setText(friend.getDisplayName());
            holder.checkbox.setChecked(true);
            final String key = mGroupKeys.get(position);
            new LoadImage(holder.imageView).loadProfile(key);
            holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isChecked) {
                        mFriendList.add(friend);
                        mFriendKeys.add(key);
                        mGroupList.remove(friend);
                        mGroupKeys.remove(key);
                        notifyDataSetChanged();
                        mFriendAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mGroupList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView displayName;
            ImageView imageView;
            CheckBox checkbox;

            ViewHolder(View itemView) {
                super(itemView);
                displayName = (TextView) itemView.findViewById(R.id.display_name);
                imageView = (ImageView) itemView.findViewById(R.id.image);
                checkbox = (CheckBox) itemView.findViewById(R.id.checkbox);
            }
        }
    }

    class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_simple_friend, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final Friend friend = mFriendList.get(position);
            holder.displayName.setText(friend.getDisplayName());
            holder.checkbox.setChecked(false);
            final String key = mFriendKeys.get(position);
            new LoadImage(holder.imageView).loadProfile(key);
            holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mGroupList.add(friend);
                        mGroupKeys.add(key);
                        mFriendList.remove(friend);
                        mFriendKeys.remove(key);
                        notifyDataSetChanged();
                        mGroupAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mFriendList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView displayName;
            ImageView imageView;
            CheckBox checkbox;

            ViewHolder(View itemView) {
                super(itemView);
                displayName = (TextView) itemView.findViewById(R.id.display_name);
                imageView = (ImageView) itemView.findViewById(R.id.image);
                checkbox = (CheckBox) itemView.findViewById(R.id.checkbox);
            }
        }
    }
}
