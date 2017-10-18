package com.routeal.cocoger.ui.main;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.NoticeMessage;
import com.routeal.cocoger.model.RangeRequest;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.provider.DBUtil;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.Notifi;
import com.routeal.cocoger.util.Utils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by nabe on 9/9/17.
 */

public class NotifiListFragment extends PagerFragment {

    private final static String TAG = "NotifiListFragment";

    private RecyclerView mRecyclerView;
    private TextView mEmptyText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifi_list, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());

        mEmptyText = (TextView) view.findViewById(R.id.empty_view);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(layoutManager);

        return view;
    }

    @Override
    void empty(boolean v) {
        // empty
    }

    @Override
    void onViewPageSelected() {
        Log.d(TAG, "NotifiListFragment selected");
        reset();
    }

    void reset() {
        List<Message> messages = createNotifiList();
        if (messages.isEmpty()) {
            mEmptyText.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mEmptyText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        mRecyclerView.setAdapter(new NotifiListAdapter(messages));
    }

    List<Message> createNotifiList() {
        List<Message> messages = new ArrayList<>();

        User user = FB.getUser();
        if (user != null) {

            if (user.getInvitees() != null) {
                Map<String, Long> invitees = user.getInvitees();
                for (Map.Entry<String, Long> entry : invitees.entrySet()) {
                    InviteeMessage m = new InviteeMessage();
                    m.key = entry.getKey();
                    m.date = Utils.getShortDateTime(entry.getValue());
                    m.nid = Math.abs((int) entry.getValue().longValue());
                    messages.add(m);
                }
            }

            if (user.getInvites() != null) {
                Map<String, Long> invites = user.getInvites();
                for (Map.Entry<String, Long> entry : invites.entrySet()) {
                    InviteMessage m = new InviteMessage();
                    m.key = entry.getKey();
                    m.date = Utils.getShortDateTime(entry.getValue());
                    m.nid = Math.abs((int) entry.getValue().longValue());
                    messages.add(m);
                }
            }

            Map<String, Friend> friends = FriendManager.getFriends();
            if (!friends.isEmpty()) {
                for (Map.Entry<String, Friend> entry : friends.entrySet()) {
                    String key = entry.getKey();
                    Friend friend = entry.getValue();
                    if (friend.getRangeRequest() != null) {
                        RangeRequest request = friend.getRangeRequest();
                        RangeMessage m = new RangeMessage();
                        m.key = key;
                        m.date = Utils.getShortDateTime(request.getCreated());
                        m.rangeTo = request.getRange();
                        m.rangeFrom = friend.getRange();
                        m.name = friend.getDisplayName();
                        String to = LocationRange.toString(m.rangeTo);
                        String from = LocationRange.toString(m.rangeFrom);
                        String pattern = getResources().getString(R.string.receive_range_request);
                        m.message = String.format(pattern, to, from);
                        m.nid = Math.abs((int) request.getCreated());
                        messages.add(m);
                    }
                }
            }

            List<NoticeMessage> noticeMessages = DBUtil.getMessages();
            for (NoticeMessage nm : noticeMessages) {
                InfoMessage m = new InfoMessage();
                m.key = nm.getKey();
                m.id = nm.getId();
                m.title = nm.getTitle();
                m.message = nm.getMessage();
                m.date = Utils.getShortDateTime(nm.getCreated());
                m.resourceId = nm.getResourceId();
                messages.add(m);
            }
        }

        return messages;
    }

    class Message {
        String key;
        int nid;
        String date;
        String message;
    }

    class OkMessage extends Message {
        String ok;
    }

    class OkNoMessage extends OkMessage {
        String no;
    }

    class InviteeMessage extends OkNoMessage {
    }

    class InviteMessage extends OkNoMessage {
    }

    class RangeMessage extends OkNoMessage {
        String name;
        int rangeTo;
        int rangeFrom;
    }

    class InfoMessage extends Message {
        long id;
        String title;
        int resourceId;
    }

    class NotifiListAdapter extends RecyclerView.Adapter<NotifiListAdapter.ViewHolder> {
        List<Message> mMessages;

        NotifiListAdapter(List<Message> messages) {
            mMessages = messages;
        }

        public NotifiListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_notifi_list, parent, false);
            return new ViewHolder(itemView);
        }

        void showFriendViewPage() {
            ViewPager page = getViewPager();
            page.setCurrentItem(1);
        }

        void closeViewPage() {
            SlidingUpPanelLayout slidingUpPanelLayout = getSlidingUpPanelLayout();
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }

        @Override
        public void onBindViewHolder(final NotifiListAdapter.ViewHolder holder, final int position) {
            Message m = mMessages.get(position);
            if (m instanceof InviteeMessage) {
                final InviteeMessage im = (InviteeMessage) m;
                holder.content.setText(im.message);
                holder.date.setText(im.date);
                FB.getUser(im.key, new FB.UserListener() {
                    @Override
                    public void onSuccess(User user) {
                        new LoadImage(holder.picture).loadProfile(im.key);
                        holder.title.setText(user.getDisplayName());
                        String pattern = getResources().getString(R.string.receive_friend_request);
                        String content = String.format(pattern, user.getDisplayName());
                        holder.content.setText(content);
                    }

                    @Override
                    public void onFail(String err) {
                    }
                });
                holder.ok.setText(R.string.accept);
                holder.ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FB.acceptFriendRequest(im.key);
                        Notifi.remove(im.nid);
                        showFriendViewPage();
                        reset();
                    }
                });
                holder.no.setText(R.string.decline);
                holder.no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FB.declineFriendRequest(im.key);
                        Notifi.remove(im.nid);
                        closeViewPage();
                        reset();
                    }
                });
            } else if (m instanceof InviteMessage) {
                final InviteMessage im = (InviteMessage) m;
                holder.content.setText(im.message);
                holder.date.setText(im.date);
                FB.getUser(im.key, new FB.UserListener() {
                    @Override
                    public void onSuccess(User user) {
                        new LoadImage(holder.picture).loadProfile(im.key);
                        holder.title.setText(user.getDisplayName());
                        String pattern = getResources().getString(R.string.send_friend_request);
                        String content = String.format(pattern, user.getDisplayName());
                        holder.content.setText(content);
                    }

                    @Override
                    public void onFail(String err) {
                    }
                });
                holder.ok.setText(android.R.string.cancel);
                holder.ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FB.cancelFriendRequest(im.key);
                        Notifi.remove(im.nid);
                        showFriendViewPage();
                        reset();
                    }
                });
                holder.no.setVisibility(View.INVISIBLE);
            } else if (m instanceof RangeMessage) {
                final RangeMessage rm = (RangeMessage) m;
                new LoadImage(holder.picture).loadProfile(rm.key);
                holder.title.setText(rm.name);
                holder.date.setText(rm.date);
                holder.content.setText(rm.message);
                holder.ok.setText(R.string.accept);
                holder.ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FB.acceptRangeRequest(rm.key, rm.rangeTo);
                        Notifi.remove(rm.nid);
                        showFriendViewPage();
                        reset();
                    }
                });
                holder.no.setText(R.string.decline);
                holder.no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FB.declineRangeRequest(rm.key);
                        Notifi.remove(rm.nid);
                        showFriendViewPage();
                        reset();
                    }
                });
            } else if (m instanceof InfoMessage) {
                final InfoMessage im = (InfoMessage) m;
                new LoadImage(holder.picture).loadProfile(im.key);
                holder.title.setText(im.title);
                holder.date.setText(im.date);
                holder.content.setText(im.message);
                holder.ok.setText(R.string.delete);
                holder.ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DBUtil.deleteMessage(im.id);
                        closeViewPage();
                        reset();
                    }
                });
                holder.no.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            Log.d(TAG, "getItemCount:" + mMessages.size());
            return mMessages.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            AppCompatImageView picture;
            AppCompatTextView title;
            AppCompatTextView date;
            AppCompatTextView content;
            AppCompatButton ok;
            AppCompatButton no;

            ViewHolder(View itemView) {
                super(itemView);
                picture = (AppCompatImageView) itemView.findViewById(R.id.picture);
                title = (AppCompatTextView) itemView.findViewById(R.id.title);
                date = (AppCompatTextView) itemView.findViewById(R.id.date);
                content = (AppCompatTextView) itemView.findViewById(R.id.content);
                ok = (AppCompatButton) itemView.findViewById(R.id.ok);
                no = (AppCompatButton) itemView.findViewById(R.id.no);
            }
        }
    }
}
