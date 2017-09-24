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

import com.routeal.cocoger.MainApplication;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
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

        onSelected();

        return view;
    }

    void onSelected() {
        Log.d(TAG, "NotifiListFragment selected");
        List<Message> messages = createNotifiList();
        if (messages == null || messages.isEmpty()) {
            mEmptyText.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mEmptyText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        mRecyclerView.setAdapter(new NotifiListAdapter(messages));
    }

    class Message {
        String id;
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
        String picture;
        int rangeTo;
        int rangeFrom;
    }

    class InfoMessage extends Message {
        long dbId;
        String title;
        String picture;
        int resourceId;
    }

    List<Message> createNotifiList() {
        List<Message> messages = new ArrayList<>();

        User user = MainApplication.getUser();
        if (user != null) {

            if (user.getInvitees() != null) {
                Map<String, Long> invitees = user.getInvitees();
                for (Map.Entry<String, Long> entry : invitees.entrySet()) {
                    InviteeMessage m = new InviteeMessage();
                    m.id = entry.getKey();
                    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                    m.date = df.format(new Date(entry.getValue()));
                    m.message = "Friend Invited";
                    m.nid = Math.abs((int) entry.getValue().longValue());
                    messages.add(m);
                }
            }

            if (user.getInvites() != null) {
                Map<String, Long> invites = user.getInvites();
                for (Map.Entry<String, Long> entry : invites.entrySet()) {
                    InviteMessage m = new InviteMessage();
                    m.id = entry.getKey();
                    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                    m.date = df.format(new Date(entry.getValue()));
                    m.message = "Friend Invite";
                    m.nid = Math.abs((int) entry.getValue().longValue());
                    messages.add(m);
                }
            }

            Map<String, Friend> friends = user.getFriends();
            if (friends != null) {
                for (Map.Entry<String, Friend> entry : friends.entrySet()) {
                    String id = entry.getKey();
                    Friend friend = entry.getValue();
                    if (friend.getRangeRequest() != null) {
                        RangeRequest request = friend.getRangeRequest();
                        RangeMessage m = new RangeMessage();
                        m.id = id;
                        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                        m.date = df.format(new Date(request.getCreated()));
                        m.rangeTo = request.getRange();
                        m.rangeFrom = friend.getRange();
                        m.name = friend.getDisplayName();
                        m.picture = friend.getPicture();
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
                m.dbId = nm.getId();
                m.title = nm.getTitle();
                m.message = nm.getMessage();
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                m.date = df.format(nm.getDate());
                m.picture = nm.getPicture();
                m.resourceId = nm.getResourceId();
                messages.add(m);
            }
        }

        return messages;
    }

    class NotifiListAdapter extends RecyclerView.Adapter<NotifiListAdapter.ViewHolder> {
        List<Message> mMessages;

        NotifiListAdapter(List<Message> messages) {
            mMessages = messages;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            AppCompatImageView action;
            AppCompatImageView picture;
            AppCompatTextView title;
            AppCompatTextView date;
            AppCompatTextView content;
            AppCompatButton ok;
            AppCompatButton no;

            public ViewHolder(View itemView) {
                super(itemView);
                //action = (AppCompatImageView) itemView.findViewById(R.id.action_icon);
                picture = (AppCompatImageView) itemView.findViewById(R.id.picture);
                title = (AppCompatTextView) itemView.findViewById(R.id.title);
                date = (AppCompatTextView) itemView.findViewById(R.id.date);
                content = (AppCompatTextView) itemView.findViewById(R.id.content);
                ok = (AppCompatButton) itemView.findViewById(R.id.ok);
                no = (AppCompatButton) itemView.findViewById(R.id.no);
            }
        }

        public NotifiListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_notifi_list, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final NotifiListAdapter.ViewHolder holder, final int position) {
            Message m = mMessages.get(position);
            if (m instanceof InviteeMessage) {
                final InviteeMessage im = (InviteeMessage) m;
                holder.content.setText(im.message);
                holder.date.setText(im.date);
                FB.getUser(im.id, new FB.UserListener() {
                    @Override
                    public void onSuccess(User user) {
                        new LoadImage.LoadImageView(holder.picture).execute(user.getPicture());
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
                        FB.acceptFriendRequest(im.id);
                        Notifi.remove(im.nid);
                        //NotifiListAdapter.this.notifyItemRemoved(position);
                        ViewPager page = getViewPager();
                        page.setCurrentItem(1);
                    }
                });
                holder.no.setText(R.string.decline);
                holder.no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FB.declineFriendRequest(im.id);
                        Notifi.remove(im.nid);
                        //NotifiListAdapter.this.notifyItemRemoved(position);
                        SlidingUpPanelLayout slidingUpPanelLayout = getSlidingUpPanelLayout();
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

                    }
                });
            } else if (m instanceof InviteMessage) {
                final InviteMessage im = (InviteMessage) m;
                holder.content.setText(im.message);
                holder.date.setText(im.date);
                FB.getUser(im.id, new FB.UserListener() {
                    @Override
                    public void onSuccess(User user) {
                        new LoadImage.LoadImageView(holder.picture).execute(user.getPicture());
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
                        FB.cancelFriendRequest(im.id);
                        Notifi.remove(im.nid);
                        ViewPager page = getViewPager();
                        page.setCurrentItem(1);
                    }
                });
                holder.no.setVisibility(View.INVISIBLE);
            } else if (m instanceof RangeMessage) {
                final RangeMessage rm = (RangeMessage) m;
                new LoadImage.LoadImageView(holder.picture).execute(rm.picture);
                holder.title.setText(rm.name);
                holder.date.setText(rm.date);
                holder.content.setText(rm.message);
                holder.ok.setText(R.string.accept);
                holder.ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FB.acceptRangeRequest(rm.id, rm.rangeTo);
                        Notifi.remove(rm.nid);
                        //NotifiListAdapter.this.notifyItemRemoved(position);
                        ViewPager page = getViewPager();
                        page.setCurrentItem(1);
                    }
                });
                holder.no.setText(R.string.decline);
                holder.no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FB.declineRangeRequest(rm.id);
                        Notifi.remove(rm.nid);
                        //NotifiListAdapter.this.notifyItemRemoved(position);
                        //SlidingUpPanelLayout slidingUpPanelLayout = getSlidingUpPanelLayout();
                        //slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        ViewPager page = getViewPager();
                        page.setCurrentItem(1);
                    }
                });
            } else if (m instanceof InfoMessage) {
                final InfoMessage im = (InfoMessage) m;
                if (im.picture != null && !im.picture.isEmpty()) {
                    new LoadImage.LoadImageView(holder.picture).execute(im.picture);
                }
                if (im.resourceId > 0) {
                    holder.picture.setBackgroundResource(im.resourceId);
                }
                holder.title.setText(im.title);
                holder.date.setText(im.date);
                holder.content.setText(im.message);
                holder.ok.setText(R.string.delete);
                holder.ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DBUtil.deleteMessage(im.dbId);
                        onSelected();
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
    }
}
