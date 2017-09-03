package com.routeal.cocoger.fb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.Device;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.model.RangeRequest;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.service.MainReceiver;
import com.routeal.cocoger.ui.main.FriendListViewHolder;
import com.routeal.cocoger.ui.main.MapActivity;
import com.routeal.cocoger.ui.main.PanelMapActivity;
import com.routeal.cocoger.ui.main.UserListViewHolder;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.NotificationHelper;
import com.routeal.cocoger.util.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FB {

    private final static String TAG = "FB";

    public static final String ACTION_FRIEND_REQUEST_ACCEPTED = "FRIEND_REQUEST_ACCEPTED";

    public static final String ACTION_FRIEND_REQUEST_DECLINED = "FRIEND_REQUEST_DECLINED";

    public static final String ACTION_RANGE_REQUEST_ACCEPTED = "RANGE_REQUEST_ACCEPTED";

    public static final String ACTION_RANGE_REQUEST_DECLINED = "RANGE_REQUEST_DECLINED";

    public interface SignInListener {
        void onSuccess();

        void onFail(String err);
    }

    public interface CreateUserListener {
        void onSuccess();

        void onFail(String err);
    }

    public interface ResetPasswordListener {
        void onSuccess();

        void onFail(String err);
    }

    public interface UploadImageListener {
        void onSuccess(String url);

        void onFail(String err);
    }

    static String getUid() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        return fUser.getUid();
    }

    static DatabaseReference getDeviceDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference().child("devices");
    }

    static DatabaseReference getUserDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference().child("users");
    }

    static DatabaseReference getFriendDatabaseReference(String user) {
        return getUserDatabaseReference().child(user).child("friends");
    }

    static DatabaseReference getFriendDatabaseReference(String user, String friend) {
        return getUserDatabaseReference().child(user).child("friends").child(friend);
    }

    static DatabaseReference getLocationDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference().child("locations");
    }

    public static boolean isAuthenticated() {
        return (FirebaseAuth.getInstance().getCurrentUser() != null);
    }

    public static boolean isCurrentUser(String key) throws Exception {
        return (key == getUid());
    }

    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    public static void monitorAuthentication() throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth == null) return;

        // setting up a listener when the user is authenticated
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "Firebase User authenticated");
                    try {
                        onUserAuth();
                    } catch (Exception e) {
                    }
                } else {
                    Log.d(TAG, "Firebase User invalidated");
                    MainApplication.setUser(null);

                    // cleanup the app config
                    MainApplication.permitLocation(false);
                }
            }
        });
    }

    private static void onUserAuth() throws Exception {
        DatabaseReference userDb = getUserDatabaseReference();

        // called whenever the user database is updated
        userDb.child(getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "User database changed:" + dataSnapshot.toString());

                User newUser = dataSnapshot.getValue(User.class);
                User oldUser = MainApplication.getUser();

                if (newUser == null) {
                    if (oldUser == null) {
                        onCreateUser(dataSnapshot.getKey());
                    }
                } else {
                    if (oldUser != null) {
                        onUpdateUser(newUser, oldUser);
                    } else {
                        onSignIn(dataSnapshot.getKey(), newUser);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void signIn(Activity ctx, String email, String password,
                              final SignInListener listener) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(ctx, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (listener != null) listener.onSuccess();
                        } else {
                            if (listener != null)
                                listener.onFail(task.getException().getLocalizedMessage());
                        }
                    }
                });
    }


    public static void createUser(Activity activity, String email, String password,
                                  final CreateUserListener listener) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (listener != null) listener.onSuccess();
                        } else {
                            if (listener != null)
                                listener.onFail(task.getException().getLocalizedMessage());
                        }
                    }
                });
    }

    public static void resetPassword(String email, final ResetPasswordListener listener) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (listener != null) listener.onSuccess();
                        } else {
                            if (listener != null)
                                listener.onFail(task.getException().getLocalizedMessage());
                        }
                    }
                });
    }

    public static void saveLocation(Location location, Address address) throws Exception {
        Log.d(TAG, "saveLocation:");

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LocationAddress loc = new LocationAddress();
        loc.setTimestamp(location.getTime());
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        loc.setAltitude(location.getAltitude());
        loc.setSpeed(location.getSpeed());
        loc.setPostalCode(address.getPostalCode());
        loc.setCountryName(address.getCountryName());
        loc.setAdminArea(address.getAdminArea());
        loc.setSubAdminArea(address.getSubAdminArea());
        loc.setlocality(address.getLocality());
        loc.setSubLocality(address.getSubLocality());
        loc.setThoroughfare(address.getThoroughfare());
        loc.setSubThoroughfare(address.getSubThoroughfare());

        // top level database reference
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        // location key
        String key = db.child("locations").push().getKey();

        GeoHash geoHash = new GeoHash(new GeoLocation(latitude, longitude));

        Map<String, Object> updates = new HashMap<>();
        // location detail
        updates.put("locations/" + key, loc);
        // geo location
        updates.put("geo_locations/" + key + "/g", geoHash.getGeoHashString());
        // geo location
        updates.put("geo_locations/" + key + "/l", Arrays.asList(latitude, longitude));
        // user locations
        //updates.put("users/" + getUid() + "/locations/" + key, loc.getTimestamp());
        updates.put("user_locations/" + getUid() + "/" + key, loc.getTimestamp());
        updates.put("users/" + getUid() + "/locations/", key);

        db.updateChildren(updates);
    }

    private static void onSignIn(String uid, User user) {
        Log.d(TAG, "onSignIn:" + user.toString());

        // save the user in the memory
        MainApplication.setUser(user);

        // let the map activity know that the user becomes available
        // so that it can have the user pictures.  Sometimes the map
        // comes faster than the user info from the firebase.
        if (MainApplication.getContext() != null) {
            Intent intent = new Intent(MapActivity.USER_AVAILABLE);
            LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
        }

        // the current device
        Device currentDevice = Utils.getDevice();

        String devKey = null;

        // get the device key to match the device id in the user database
        Map<String, String> devList = user.getDevices();
        if (devList != null && !devList.isEmpty()) {
            for (Map.Entry<String, String> entry : devList.entrySet()) {
                // the values is a device id
                if (entry.getValue().equals(currentDevice.getDeviceId())) {
                    devKey = entry.getKey();
                }
            }
        }

        DatabaseReference devDb = getDeviceDatabaseReference();

        if (devKey != null) {
            // update the timestamp of the device
            devDb.child(devKey).child("timestamp").setValue(currentDevice.getTimestamp());
        } else {
            // set the uid to the device before save
            currentDevice.setUid(uid);

            // add it as a new device
            String newKey = devDb.push().getKey();
            devDb.child(newKey).setValue(currentDevice);

            // also add it to the user database under 'devices'
            DatabaseReference userDb = getUserDatabaseReference();
            userDb.child(uid).child("devices").child(newKey).setValue(currentDevice.getDeviceId());
        }
    }

    private static void onCreateUser(String uid) {
        Log.d(TAG, "signup: uid=" + uid);

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        User user = new User();
        user.setEmail(fUser.getEmail());

        // save the user in the memory
        MainApplication.setUser(user);

        FirebaseDatabase db = FirebaseDatabase.getInstance();

        // save the device info
        Device device = Utils.getDevice();
        device.setUid(uid);
        DatabaseReference devDb = getDeviceDatabaseReference();
        String key = devDb.push().getKey();
        devDb.child(key).setValue(device);

        // save the user info to the remote database
        DatabaseReference userDb = getUserDatabaseReference();
        userDb.child(uid).setValue(user);

        // add the device key to the user info
        Map<String, String> devices = new HashMap<>();
        devices.put(key, device.getDeviceId());
        userDb.child(uid).child("devices").setValue(devices);

        // Send an email verification
        //sendEmailVerification();
    }

    private static void onUpdateUser(User nu /*newUser*/, User ou /*oldUser*/) {
        Log.d(TAG, "updateUser");

        Map<String, Long> invitees = nu.getInvitees();
        if (invitees != null && !invitees.isEmpty()) {
            for (Map.Entry<String, Long> entry : invitees.entrySet()) {
                if (entry.getValue() > 0) {
                    if (ou.getInvitees() == null ||
                            ou.getInvitees().get(entry.getKey()) == null) {
                        Log.d(TAG, "received new friends request");
                        //ou.setInvitees(invitees);
                        sendInviteNotification(entry.getKey());
                    } else {
                        Log.d(TAG, "received old friends request");
                    }
                }
            }
        }

        Map<String, Friend> newFriends = nu.getFriends();
        Map<String, Friend> oldFriends = ou.getFriends();

        if (newFriends == null || newFriends.size() == 0 || oldFriends == null) {
            MainApplication.setUser(nu);
            return;
        }

        // added new friends
        if (newFriends.size() > oldFriends.size()) {
        }
        // deleted friends
        else if (newFriends.size() < oldFriends.size()) {
        }
        // range request
        else {
            for (Map.Entry<String, Friend> entry : newFriends.entrySet()) {
                String friendUid = entry.getKey();
                Friend newFriend = entry.getValue();
                Friend oldFriend = oldFriends.get(friendUid);

                // possible new range request
                if (newFriend.getRangeRequest() != null) {
                    int requestRange = newFriend.getRangeRequest().getRange();
                    int currentRange = newFriend.getRange();

                    // new range request found
                    if (oldFriend.getRangeRequest() == null) {
                        sendRangeNotification(friendUid, newFriend, requestRange, currentRange);
                    }
                    // existing range request, show the notification if the timestamp is old
                    else {

                    }
                }
                // the range has been update, notify the map acitivity to change the marker location
                else if (newFriend.getRange() != oldFriend.getRange()) {
                }
            }
        }

        MainApplication.setUser(nu);
    }

    public static void initUser(User user) throws Exception {
        String uid = getUid();
        DatabaseReference userDb = getUserDatabaseReference();
        userDb.child(uid).setValue(user);
    }

    private static void sendEmailVerification() {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        fbUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "sendEmailVerification");
                }
            }
        });
    }

    public static boolean sendFriendRequest(RecyclerView.Adapter a) {
        FirebaseRecyclerAdapter<User, UserListViewHolder> adapter =
                (FirebaseRecyclerAdapter<User, UserListViewHolder>) a;

        String uid = getUid();
        DatabaseReference userDb = getUserDatabaseReference();

        boolean modified = false;

        for (int i = 0; i < adapter.getItemCount(); i++) {
            DatabaseReference fDb = adapter.getRef(i);
            if (fDb.getKey().equals(uid)) {
                // trying to add myself to friends
                continue;
            }

            // set the timestamp when the friend request is added.
            // When the requested user approved, the timestamp will be changed to true.
            long timestamp = System.currentTimeMillis();

            Map<String, Long> friend = new HashMap<>();

            // add myself to friend
            friend.clear();
            friend.put(uid, timestamp);
            fDb.child("invitees").setValue(friend);

            // add friends to myself
            String key = adapter.getRef(i).getKey();
            friend.clear();
            friend.put(key, timestamp);
            userDb.child(uid).child("invites").setValue(friend);

            modified = true;
        }

        return modified;
    }

    public static void acceptFriendRequest(final String invite) throws Exception {
        final String invitee = getUid();

        DatabaseReference userDb = getUserDatabaseReference();
        userDb.child(invitee).child("invitees").child(invite).removeValue();
        userDb.child(invite).child("invites").child(invitee).removeValue();

        final long timestamp = System.currentTimeMillis();
        final int defaultLocationChange = LocationRange.SUBADMINAREA.toInt();

        // invitee - me invited by invite, get the information of the invite
        userDb.child(invite).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User inviteUser = dataSnapshot.getValue(User.class);
                        Friend friend = new Friend();
                        friend.setCreated(timestamp);
                        friend.setRange(defaultLocationChange);
                        friend.setDisplayName(inviteUser.getDisplayName());
                        friend.setPicture(inviteUser.getPicture());

                        DatabaseReference fDb = getFriendDatabaseReference(invitee, invite);
                        fDb.setValue(friend);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        // invite - me added to invite
        Friend myInfo = new Friend();
        myInfo.setCreated(timestamp);
        myInfo.setRange(defaultLocationChange);
        myInfo.setDisplayName(MainApplication.getUser().getDisplayName());
        myInfo.setPicture(MainApplication.getUser().getPicture());

        DatabaseReference fDb = getFriendDatabaseReference(invite, invitee);
        fDb.setValue(myInfo);
    }

    public static void declineFriendRequest(String invite) throws Exception {
        // delete the invite and invitee from the database
        String invitee = getUid();

        DatabaseReference userDb = getUserDatabaseReference();
        userDb.child(invitee).child("invitees").child(invite).removeValue();
        userDb.child(invite).child("invites").child(invitee).removeValue();
    }

    public static void acceptRangeRequest(String requester, int range) throws Exception {
        String responder = getUid();

        DatabaseReference resDb = getFriendDatabaseReference(responder, requester);
        resDb.child("range").setValue(range);
        resDb.child("rangeRequest").removeValue();

        DatabaseReference reqDb = getFriendDatabaseReference(requester, responder);
        reqDb.child("range").setValue(range);
    }

    public static void declineRangeRequest(String requester) throws Exception {
        String responder = getUid(); // myself

        DatabaseReference resDb = getFriendDatabaseReference(responder, requester);
        resDb.child("rangeRequest").removeValue();
    }

    public static void changeRange(String fid, int range) throws Exception {
        String uid = getUid();

        DatabaseReference myside = getFriendDatabaseReference(uid, fid);
        myside.child("range").setValue(range);

        DatabaseReference hisside = getFriendDatabaseReference(fid, uid);
        hisside.child("range").setValue(range);
    }

    public static void sendChangeRequest(String fid, int range) throws Exception {
        String uid = getUid();

        RangeRequest rangeRequest = new RangeRequest();
        rangeRequest.setCreated(System.currentTimeMillis());
        rangeRequest.setRange(range);

        DatabaseReference hisside = getFriendDatabaseReference(fid, uid);
        hisside.child("rangeRequest").setValue(rangeRequest);
    }

    public static void uploadImageFile(Uri localFile, String name,
                                       final UploadImageListener listener) throws Exception {
        String uid = getUid();
        String refName = "users/" + uid + "/image/" + name;
        FirebaseStorage.getInstance().getReference().child(refName).putFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        String picture = downloadUrl.toString();
                        if (listener != null) listener.onSuccess(picture);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (listener != null) listener.onFail(e.getLocalizedMessage());
                    }
                });

    }

    private static void sendRangeNotification(String uid, Friend friend,
                                              int requestRange, int currentRange) {
        Context context = MainApplication.getContext();

        Intent acceptIntent = new Intent(context, PanelMapActivity.class);
        acceptIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        acceptIntent.setAction(ACTION_RANGE_REQUEST_ACCEPTED);
        acceptIntent.putExtra("range_requester", uid);
        acceptIntent.putExtra("range", requestRange);

        Intent declineIntent = new Intent(context, MainReceiver.class);
        declineIntent.setAction(ACTION_RANGE_REQUEST_DECLINED);
        declineIntent.putExtra("range_requester", uid);

        String to = LocationRange.toString(requestRange);
        String from = LocationRange.toString(currentRange);
        String content = "You received a range request to " + to + " from " + from;

        NotificationHelper.send(friend.getDisplayName(), content, friend.getPicture(),
                acceptIntent, declineIntent);
    }

    private static void sendInviteNotification(final String invite) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

        userRef.child(invite).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User inviteUser = dataSnapshot.getValue(User.class);

                Context context = MainApplication.getContext();

                // accept starts the main activity with the friend view
                Intent acceptIntent = new Intent(context, PanelMapActivity.class);
                acceptIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                acceptIntent.setAction(ACTION_FRIEND_REQUEST_ACCEPTED);
                acceptIntent.putExtra("friend_invite", invite);

                Intent declineIntent = new Intent(context, MainReceiver.class);
                declineIntent.setAction(ACTION_FRIEND_REQUEST_DECLINED);
                declineIntent.putExtra("friend_invite", invite);

                String content = "You received a friend request from " + inviteUser.getDisplayName() + ".";

                NotificationHelper.send(inviteUser.getDisplayName(), content, inviteUser.getPicture(),
                        acceptIntent, declineIntent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public static FirebaseRecyclerAdapter<Friend, FriendListViewHolder> getFriendRecyclerAdapter()
            throws Exception {
        DatabaseReference fDb = getFriendDatabaseReference(getUid());

        FirebaseRecyclerAdapter<Friend, FriendListViewHolder> adapter =
                new FirebaseRecyclerAdapter<Friend, FriendListViewHolder>(
                        Friend.class,
                        R.layout.listview_friend_list,
                        FriendListViewHolder.class,
                        fDb
                ) {
                    @Override
                    protected void populateViewHolder(FriendListViewHolder viewHolder,
                                                      Friend model, int position) {
                        viewHolder.bind(model, getRef(position).getKey());
                    }
                };

        return adapter;
    }


    public static FirebaseRecyclerAdapter<User, UserListViewHolder>
    getUserRecyclerAdapter(String text, final View view) throws Exception {
        // TODO:
        // Exclude 1) myself, 2) current friends but being added to friend

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

        Query query = userRef
                .orderByChild("searchedName")
                .startAt(text)
                .endAt(text + "~");

        FirebaseRecyclerAdapter<User, UserListViewHolder> adapter =
                new FirebaseRecyclerAdapter<User, UserListViewHolder>(
                        User.class,
                        R.layout.listview_user_list,
                        UserListViewHolder.class,
                        query) {

                    @Override
                    public void populateViewHolder(UserListViewHolder holder, User user, int position) {
                        holder.bind(user, getRef(position).getKey());
                    }

                    @Override
                    public void onDataChanged() {
                        // NOTE: this gets called when new people are added to the friends
                        TextView emptyListMessage = (TextView) view.findViewById(R.id.empty_list_text);
                        emptyListMessage.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    }
                };

        return adapter;
    }
}
