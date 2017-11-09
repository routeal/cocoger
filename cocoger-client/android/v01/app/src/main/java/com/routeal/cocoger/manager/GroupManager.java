package com.routeal.cocoger.manager;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Group;
import com.routeal.cocoger.model.Member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by hwatanabe on 10/22/17.
 */

public class GroupManager {

    private static SortedMap<String, Group> mGroupList = new TreeMap<>();
    private static UpdateListener<Group> mUpdateListener;

    public static Group getGroup(String key) {
        if (key != null && !key.isEmpty()) {
            return mGroupList.get(key);
        }
        return null;
    }

    public static SortedMap<String, Group> getGroups() {
        return mGroupList;
    }

    public static Map<String, Group> getGroups(String uid) {
        Map<String, Group> groups = new HashMap<>();
        for (Map.Entry<String, Group> entry : mGroupList.entrySet()) {
            String key = entry.getKey();
            Group group = entry.getValue();
            for (Map.Entry<String, Member> entry2 : group.getMembers().entrySet()) {
                String uid2 = entry2.getKey();
                if (uid.equals(uid2)) {
                    groups.put(key, group);
                    break;
                }
            }
        }
        return groups;
    }

    public static boolean isEmpty() {
        return mGroupList.isEmpty();
    }

    public static List<Group> getInvited() {
        List<Group> invitedList = new ArrayList<>();
        for (Map.Entry<String, Group> entry : mGroupList.entrySet()) {
            //String key = entry.getKey();
            Group group = entry.getValue();
            for (Map.Entry<String, Member> entry2 : group.getMembers().entrySet()) {
                //String uid = entry2.getKey();
                Member member = entry2.getValue();
                if (member.getStatus() == Member.INVITED) {
                    invitedList.add(group);
                }
            }
        }
        return invitedList;
    }

    public static void add(String key, Group group) {
        mGroupList.put(key, group);
        if (mUpdateListener != null) {
            mUpdateListener.onAdded(key, group);
        }
        Intent intent = new Intent(FB.GROUP_ADD);
        intent.putExtra(FB.KEY, key);
        intent.putExtra(FB.GROUP, group);
        LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
    }

    public static void change(String key, Group group) {
        Group oldGroup = mGroupList.get(key);
        if (oldGroup != null) {
            for (Map.Entry<String, Member> entry : group.getMembers().entrySet()) {
                String uid = entry.getKey();
                Member member = entry.getValue();
                if (oldGroup.getMembers().get(uid) == null && uid.equals(FB.getUid()) &&
                        member.getStatus() == Member.INVITED) {
                    Intent intent = new Intent(FB.GROUP_INVITE);
                    intent.putExtra(FB.KEY, key);
                    intent.putExtra(FB.GROUP, group);
                    LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
                }
            }
        }
        mGroupList.put(key, group);
        if (mUpdateListener != null) {
            mUpdateListener.onChanged(key, group);
        }
    }

    public static void remove(String key) {
        mGroupList.remove(key);
        if (mUpdateListener != null) {
            mUpdateListener.onRemoved(key);
        }
    }

    public static void setUpdateListener(UpdateListener<Group> listener) {
        mUpdateListener = listener;
    }
}
