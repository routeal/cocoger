package com.routeal.cocoger.manager;

import com.routeal.cocoger.model.Group;
import com.routeal.cocoger.model.Member;

import java.util.ArrayList;
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
    }

    public static void change(String key, Group group) {
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
