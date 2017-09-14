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
import android.widget.ImageView;
import android.widget.TextView;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.RangeRequest;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.LocationRange;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by nabe on 9/9/17.
 */

public class NotifiListFragment extends PagerFragment {

    private final static String TAG = "NotifiListFragment";

    private RecyclerView mRecyclerView;

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

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(layoutManager);

        List<Message> messages = createNotifiList();
        mRecyclerView.setAdapter(new NotifiListAdapter(messages));

        return view;
    }

    void onSelected() {
        Log.d(TAG, "NotifiListFragment selected");
        List<Message> messages = createNotifiList();
        mRecyclerView.setAdapter(new NotifiListAdapter(messages));
    }

    class Message {
        String id;
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
                    messages.add(m);
                }
            }

            Map<String, Friend> friends = user.getFriends();
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
                    messages.add(m);
                }
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
                        //NotifiListAdapter.this.notifyItemRemoved(position);
                        SlidingUpPanelLayout slidingUpPanelLayout = getSlidingUpPanelLayout();
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

                    }
                });
            } else if (m instanceof InviteMessage) {
                InviteMessage im = (InviteMessage) m;
                holder.content.setText(im.message);
                holder.date.setText(im.date);
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
                        //NotifiListAdapter.this.notifyItemRemoved(position);
                        SlidingUpPanelLayout slidingUpPanelLayout = getSlidingUpPanelLayout();
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mMessages.size();
        }
    }
}
