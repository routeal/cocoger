package com.routeal.cocoger.fb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
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
import com.routeal.cocoger.service.MainService;
import com.routeal.cocoger.ui.main.FriendListViewHolder;
import com.routeal.cocoger.ui.main.MapActivity;
import com.routeal.cocoger.ui.main.PanelMapActivity;
import com.routeal.cocoger.ui.main.UserListViewHolder;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.Notifi;
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

    public static final String NOTIFI_RANGE_REQUETER = "range_requester";
    public static final String NOTIFI_RANGE = "range";
    public static final String NOTIFI_FRIEND_INVITE = "friend_invite";

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

    public interface LocationListener {
        void onSuccess(Location location, Address address);

        void onFail(String err);
    }

    public interface UserListener {
        void onSuccess(User user);

        void onFail(String err);
    }

    public interface CompleteListener {
        void onSuccess();

        void onFail(String err);
    }

    public static String getUid() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser == null) return null;
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

    static DatabaseReference getUserLocationDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference().child("user_locations");
    }

    public static boolean isAuthenticated() {
        return (FirebaseAuth.getInstance().getCurrentUser() != null);
    }

    public static boolean isCurrentUser(String key) {
        if (key == null) return false;
        return key.equals(getUid());
    }

    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    static boolean monitored = false;

    public static void monitorAuthentication() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        Log.d(TAG, "monitorAuthentication");

        if (auth == null) return;

        if (monitored) return;

        monitored = true;

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
                    if (MainApplication.getUser() != null) {
                        MainApplication.setUser(null);
                        // stop the service
                        MainService.stop();
                    }
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

    public static void saveLocation(Location location, Address address) {
        saveLocation(location, address, null);
    }

    // Note: this may call before login
    public static void saveLocation(Location location, Address address, final CompleteListener listener) {
        saveLocation(location, address, true, listener);
    }

    public static void saveLocation(Location location, Address address, boolean notifyFriend,
                                    final CompleteListener listener) {
        Log.d(TAG, "saveLocation:");

        String uid = getUid();
        if (uid == null) return;

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
        loc.setLocality(address.getLocality());
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
        updates.put("user_locations/" + uid + "/" + key, loc.getTimestamp());
        updates.put("users/" + uid + "/location/", key);

        if (notifyFriend) {
            User user = MainApplication.getUser();
            if (user != null) {
                Map<String, Friend> friends = user.getFriends();
                if (friends != null) {
                    for (Map.Entry<String, Friend> entry : friends.entrySet()) {
                        updates.put("users/" + entry.getKey() + "/friends/" + uid + "/location", key);
                    }
                }
            }
        }

        db.updateChildren(updates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    // success
                    if (listener != null) listener.onSuccess();
                } else {
                    // error
                    if (listener != null) listener.onFail(databaseError.getMessage());
                }
            }
        });
    }

    public static void updateUser(String name, String gender, String bob, String url) {
        String uid = getUid();
        if (uid == null) return;

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> updates = new HashMap<>();
        if (name != null) {
            name = name.trim();
            updates.put("users/" + uid + "/displayName/", name);
            updates.put("users/" + uid + "/searchedName/", name.toLowerCase());
        }
        if (gender != null) {
            gender = gender.trim();
            updates.put("users/" + uid + "/gender/", gender.toLowerCase());
        }
        if (bob != null) {
            bob = bob.trim();
            updates.put("users/" + uid + "/birthYear/", bob.toLowerCase());
        }
        if (url != null) {
            updates.put("users/" + uid + "/picture/", url);
        }
        if (updates.size() > 0) {
            db.updateChildren(updates);
        }
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
        Device currentDevice = getDevice();

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
        Device device = getDevice();
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
        Log.d(TAG, "onUpdateUser");

        Map<String, Long> invitees = nu.getInvitees();
        if (invitees != null && !invitees.isEmpty()) {
            for (Map.Entry<String, Long> entry : invitees.entrySet()) {
                if (entry.getValue() > 0) {
                    if (ou.getInvitees() == null ||
                            ou.getInvitees().get(entry.getKey()) == null) {
                        Log.d(TAG, "received new friends request");
                        //ou.setInvitees(invitees);
                        sendInviteNotification(entry.getKey(), entry.getValue());
                    } else {
                        Log.d(TAG, "received old friends request");
                    }
                }
            }
        }

        Map<String, Friend> newFriends = nu.getFriends();
        Map<String, Friend> oldFriends = ou.getFriends();

        if (newFriends == null && oldFriends == null) {
            MainApplication.setUser(nu);
            return;
        }

        if (oldFriends == null && newFriends != null) {
            for (Map.Entry<String, Friend> entry : newFriends.entrySet()) {
                Log.d(TAG, "Friend added: " + entry.getKey());
                Intent intent = new Intent(MapActivity.FRIEND_LOCATION_UPDATE);
                intent.putExtra(MapActivity.FRIEND_KEY, entry.getKey());
                LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
            }
        } else if (oldFriends != null && newFriends == null) {
            for (Map.Entry<String, Friend> entry : oldFriends.entrySet()) {
                Log.d(TAG, "Friend deleted: " + entry.getKey());
                Intent intent = new Intent(MapActivity.FRIEND_LOCATION_REMOVE);
                intent.putExtra(MapActivity.FRIEND_KEY, entry.getKey());
                LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
            }
        } else if (newFriends.size() != oldFriends.size()) {
            Log.d(TAG, "Friend size changed");
            Map<String, Friend> diffs = Utils.diffMaps(newFriends, oldFriends);
            for (Map.Entry<String, Friend> entry : diffs.entrySet()) {
                // deleted
                if (newFriends.get(entry.getKey()) == null) {
                    Log.d(TAG, "Friend deleted: " + entry.getKey());
                    Intent intent = new Intent(MapActivity.FRIEND_LOCATION_REMOVE);
                    intent.putExtra(MapActivity.FRIEND_KEY, entry.getKey());
                    LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
                }
                // added
                else if (oldFriends.get(entry.getKey()) == null) {
                    Log.d(TAG, "Friend added: " + entry.getKey());
                    Intent intent = new Intent(MapActivity.FRIEND_LOCATION_UPDATE);
                    intent.putExtra(MapActivity.FRIEND_KEY, entry.getKey());
                    LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
                }
            }
        }
        // range request, location change
        else {
            Log.d(TAG, "Friend size unchanged");
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
                    // existing range request, show the notification
                    // if the timestamp is old
                    else {

                    }
                }

                // the range has been update, notify the map acitivity
                // to change the marker location
                if (newFriend.getRange() != oldFriend.getRange()) {
                    Intent intent = new Intent(MapActivity.FRIEND_RANGE_UPDATE);
                    intent.putExtra(MapActivity.FRIEND_KEY, friendUid);
                    LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
                }

                if (newFriend.getLocation() != null && oldFriend.getLocation() != null &&
                        !newFriend.getLocation().equals(oldFriend.getLocation())) {
                    Intent intent = new Intent(MapActivity.FRIEND_LOCATION_UPDATE);
                    intent.putExtra(MapActivity.FRIEND_KEY, friendUid);
                    LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
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

    public static boolean checkFriendRequest(RecyclerView.Adapter a) {
        FirebaseRecyclerAdapter<User, UserListViewHolder> adapter =
                (FirebaseRecyclerAdapter<User, UserListViewHolder>) a;
        boolean accepted = false;

        User user = MainApplication.getUser();

        Map<String, Long> invitees = user.getInvitees();

        if (invitees != null) {
            for (int i = 0; i < adapter.getItemCount(); i++) {
                String key = adapter.getRef(i).getKey();
                if (invitees.get(key) != null) {
                    acceptFriendRequest(key);
                    accepted = true;
                }
            }
        }

        return accepted;
    }

    public static boolean sendFriendRequest(RecyclerView.Adapter a) {
        FirebaseRecyclerAdapter<User, UserListViewHolder> adapter =
                (FirebaseRecyclerAdapter<User, UserListViewHolder>) a;

        if (adapter == null) return false;

        String uid = getUid();
        DatabaseReference userDb = getUserDatabaseReference();

        User user = MainApplication.getUser();
        Map<String, Friend> friends = user.getFriends();

        boolean modified = false;

        for (int i = 0; i < adapter.getItemCount(); i++) {
            DatabaseReference fDb = adapter.getRef(i);
            if (fDb.getKey().equals(uid)) {
                // trying to add myself to friends
                continue;
            }

            String key = adapter.getRef(i).getKey();
            if (friends != null && friends.get(key) != null) {
                // already being friend
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
            friend.clear();
            friend.put(key, timestamp);
            userDb.child(uid).child("invites").setValue(friend);

            modified = true;
        }

        return modified;
    }

    public static void acceptFriendRequest(final String invite) {
        final String invitee = getUid();

        DatabaseReference userDb = getUserDatabaseReference();
        userDb.child(invitee).child("invitees").child(invite).removeValue();
        userDb.child(invite).child("invites").child(invitee).removeValue();

        final long timestamp = System.currentTimeMillis();
        final int defaultLocationChange = LocationRange.SUBADMINAREA.range;

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
                        friend.setLocation(inviteUser.getLocation());

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
        myInfo.setLocation(MainApplication.getUser().getLocation());

        DatabaseReference fDb = getFriendDatabaseReference(invite, invitee);
        fDb.setValue(myInfo);
    }

    public static void declineFriendRequest(String invite) {
        // delete the invite and invitee from the database
        String invitee = getUid();

        DatabaseReference userDb = getUserDatabaseReference();
        userDb.child(invitee).child("invitees").child(invite).removeValue();
        userDb.child(invite).child("invites").child(invitee).removeValue();
    }

    public static void acceptRangeRequest(String requester, int range) {
        String responder = getUid();

        DatabaseReference resDb = getFriendDatabaseReference(responder, requester);
        resDb.child("range").setValue(range);
        resDb.child("rangeRequest").removeValue();

        DatabaseReference reqDb = getFriendDatabaseReference(requester, responder);
        reqDb.child("range").setValue(range);
    }

    public static void declineRangeRequest(String requester) {
        String responder = getUid(); // myself

        DatabaseReference resDb = getFriendDatabaseReference(responder, requester);
        resDb.child("rangeRequest").removeValue();
    }

    public static void changeRange(String fid, int range) {
        String uid = getUid();

        DatabaseReference myside = getFriendDatabaseReference(uid, fid);
        myside.child("range").setValue(range);

        DatabaseReference hisside = getFriendDatabaseReference(fid, uid);
        hisside.child("range").setValue(range);
    }

    public static void sendChangeRequest(String fid, int range) {
        String uid = getUid();

        RangeRequest rangeRequest = new RangeRequest();
        rangeRequest.setCreated(System.currentTimeMillis());
        rangeRequest.setRange(range);

        DatabaseReference hisside = getFriendDatabaseReference(fid, uid);
        hisside.child("rangeRequest").setValue(rangeRequest);
    }

    public static void unfriend(String fid) {
        String uid = getUid();
        DatabaseReference hisside = getFriendDatabaseReference(fid, uid);
        hisside.removeValue();

        DatabaseReference myside = getFriendDatabaseReference(uid, fid);
        myside.removeValue();
    }

    public static void uploadImageFile(Uri localFile, String name,
                                       final UploadImageListener listener) {
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
        acceptIntent.addFlags(//Intent.FLAG_ACTIVITY_CLEAR_TOP
//                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        acceptIntent.setAction(ACTION_RANGE_REQUEST_ACCEPTED);
        acceptIntent.putExtra(NOTIFI_RANGE_REQUETER, uid);
        acceptIntent.putExtra(NOTIFI_RANGE, requestRange);

        Intent declineIntent = new Intent(context, MainReceiver.class);
        declineIntent.setAction(ACTION_RANGE_REQUEST_DECLINED);
        declineIntent.putExtra(NOTIFI_RANGE_REQUETER, uid);

        String to = LocationRange.toString(requestRange);
        String from = LocationRange.toString(currentRange);
        String pattern = context.getResources().getString(R.string.receive_range_request);
        String content = String.format(pattern, to, from);
        int nid = Math.abs((int) friend.getRangeRequest().getCreated());

        Notifi.send(nid, friend.getDisplayName(), content, friend.getPicture(),
                acceptIntent, declineIntent);
    }

    private static void sendInviteNotification(final String invite, final long timestamp) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

        userRef.child(invite).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User inviteUser = dataSnapshot.getValue(User.class);

                Context context = MainApplication.getContext();

                // accept starts the main activity with the friend view
                Intent acceptIntent = new Intent(context, PanelMapActivity.class);
                acceptIntent.addFlags(//Intent.FLAG_ACTIVITY_CLEAR_TOP
//                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                acceptIntent.setAction(ACTION_FRIEND_REQUEST_ACCEPTED);
                acceptIntent.putExtra(NOTIFI_FRIEND_INVITE, invite);

                Intent declineIntent = new Intent(context, MainReceiver.class);
                declineIntent.setAction(ACTION_FRIEND_REQUEST_DECLINED);
                declineIntent.putExtra(NOTIFI_FRIEND_INVITE, invite);

                String pattern = context.getResources().getString(R.string.receive_friend_request);
                String content = String.format(pattern, inviteUser.getDisplayName());

                int nid = Math.abs((int) timestamp);

                Notifi.send(nid, inviteUser.getDisplayName(), content, inviteUser.getPicture(),
                        acceptIntent, declineIntent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public static FirebaseRecyclerAdapter<Friend, FriendListViewHolder> getFriendRecyclerAdapter() {
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
    getUserRecyclerAdapter(String text, final View view) {
        // FIXME:
        // Need flexible search

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

        Query query = userRef
                .orderByChild("searchedName")
                .limitToFirst(20)
                .startAt(text)
                .endAt(text + "~");

        FirebaseRecyclerAdapter<User, UserListViewHolder> adapter =
                new FirebaseRecyclerAdapter<User, UserListViewHolder>(
                        User.class,
                        R.layout.listview_user_list,
                        UserListViewHolder.class,
                        query) {

                    @Override
                    public void populateViewHolder(UserListViewHolder holder,
                                                   User user, int position) {
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

    public static void getUser(String key, final UserListener listener) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
        userRef.child(key).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (listener != null) {
                            listener.onSuccess(user);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        if (listener != null) {
                            listener.onFail(databaseError.toException().getLocalizedMessage());
                        }
                    }
                }
        );
    }

    public static void getLocation(String location, final LocationListener listener) {
        DatabaseReference locDb = getLocationDatabaseReference();
        locDb.child(location).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        LocationAddress la = dataSnapshot.getValue(LocationAddress.class);
                        if (listener != null) {
                            Location location = Utils.getLocation(la);
                            Address address = Utils.getAddress(location);
                            listener.onSuccess(location, address);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        if (listener != null) {
                            listener.onFail(databaseError.toException().getLocalizedMessage());
                        }
                    }
                });
    }

    public static void getTimelineLocations(long start, long end, final LocationListener listener) {
        String uid = getUid();
        DatabaseReference locDb = getUserLocationDatabaseReference();
        Query query = locDb.child(uid).orderByValue().startAt(start).endAt(end);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "getTimelineLocations: " + childDataSnapshot.getKey());
                    String location = childDataSnapshot.getKey();
                    getLocation(location, new LocationListener() {
                        @Override
                        public void onSuccess(Location location, Address address) {
                            if (listener != null) {
                                listener.onSuccess(location, address);
                            }
                        }

                        @Override
                        public void onFail(String err) {
                            if (listener != null) {
                                listener.onFail(err);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (listener != null) {
                    listener.onFail(databaseError.getMessage());
                }
            }
        });
    }

    // For now, the device is saved into the memory
    static Device getDevice() {
        Device mDevice = new Device();
        mDevice.setDeviceId(getDeviceUniqueId());
        mDevice.setBrand(Build.BRAND);
        mDevice.setModel(Build.MODEL);
        mDevice.setPlatformVersion(Build.VERSION.RELEASE);
        mDevice.setSimulator(isEmulator());
        mDevice.setToken(""); // empty for now
        mDevice.setStatus(Device.FOREGROUND);
        mDevice.setAppVersion(MainApplication.getApplicationVersion());
        mDevice.setTimestamp(System.currentTimeMillis());
        return mDevice;
    }

    static boolean isEmulator() {
        int rating = 0;

        if ((Build.PRODUCT.equals("sdk")) || (Build.PRODUCT.equals("google_sdk"))
                || (Build.PRODUCT.equals("sdk_x86")) || (Build.PRODUCT.equals("vbox86p"))) {
            rating++;
        }
        if ((Build.MANUFACTURER.equals("unknown")) || (Build.MANUFACTURER.equals("Genymotion"))) {
            rating++;
        }
        if ((Build.BRAND.equals("generic")) || (Build.BRAND.equals("generic_x86"))) {
            rating++;
        }
        if ((Build.DEVICE.equals("generic")) || (Build.DEVICE.equals("generic_x86")) ||
                (Build.DEVICE.equals("vbox86p"))) {
            rating++;
        }
        if ((Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk"))
                || (Build.MODEL.equals("Android SDK built for x86"))) {
            rating++;
        }
        if ((Build.HARDWARE.equals("goldfish")) || (Build.HARDWARE.equals("vbox86"))) {
            rating++;
        }
        if ((Build.FINGERPRINT.contains("generic/sdk/generic"))
                || (Build.FINGERPRINT.contains("generic_x86/sdk_x86/generic_x86"))
                || (Build.FINGERPRINT.contains("generic/google_sdk/generic"))
                || (Build.FINGERPRINT.contains("generic/vbox86p/vbox86p"))) {
            rating++;
        }

        return rating > 4;
    }

    static String getDeviceUniqueId() {
        return Settings.Secure.getString(MainApplication.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }
}
