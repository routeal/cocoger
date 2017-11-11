package com.routeal.cocoger.manager;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Group;
import com.routeal.cocoger.model.Member;
import com.routeal.cocoger.service.LocationUpdateService;
import com.routeal.cocoger.ui.main.PanelMapActivity;
import com.routeal.cocoger.util.Notifi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.routeal.cocoger.fb.FB.ACTION_GROUP_JOIN_ACCEPTED;
import static com.routeal.cocoger.fb.FB.ACTION_GROUP_JOIN_DECLINED;
import static com.routeal.cocoger.fb.FB.NOTIFI_GROUP_INVITE;

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

        // added as a invite
        for (Map.Entry<String, Member> entry : group.getMembers().entrySet()) {
            String uid = entry.getKey();
            Member member = entry.getValue();
            if (uid.equals(FB.getUid()) && member.getStatus() == Member.INVITED) {
                sendInvitedNotification(key, group);
            }
        }
    }

    public static void change(String key, Group group) {
        /*
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
                    sendInvitedNotification(key, group);
                }
            }
        }
        */
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

    private static void sendInvitedNotification(String key, Group group) {
        Context context = MainApplication.getContext();

        // accept starts the main activity with the friend view
        Intent acceptIntent = new Intent(context, PanelMapActivity.class);
        acceptIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        acceptIntent.setAction(ACTION_GROUP_JOIN_ACCEPTED);
        acceptIntent.putExtra(NOTIFI_GROUP_INVITE, key);

        Intent declineIntent = new Intent(context, LocationUpdateService.class);
        declineIntent.setAction(ACTION_GROUP_JOIN_DECLINED);
        declineIntent.putExtra(NOTIFI_GROUP_INVITE, key);

        String pattern = context.getResources().getString(R.string.receive_group_join);
        String content = String.format(pattern, group.getName());

        int nid = Math.abs((int) group.getCreated());

        Notifi.send(nid, key, group.getName(), content, acceptIntent, declineIntent);
    }
}
