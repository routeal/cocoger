package com.routeal.cocoger.fb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.Device;
import com.routeal.cocoger.model.Feedback;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.Group;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.model.Member;
import com.routeal.cocoger.model.Place;
import com.routeal.cocoger.model.RangeRequest;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.service.LocationUpdateService;
import com.routeal.cocoger.ui.main.FriendListViewHolder;
import com.routeal.cocoger.ui.main.FriendManager;
import com.routeal.cocoger.ui.main.GroupListViewHolder;
import com.routeal.cocoger.ui.main.GroupManager;
import com.routeal.cocoger.ui.main.PagerFragment;
import com.routeal.cocoger.ui.main.PanelMapActivity;
import com.routeal.cocoger.ui.main.PlaceListViewHolder;
import com.routeal.cocoger.ui.main.RecyclerAdapterListener;
import com.routeal.cocoger.ui.main.UserDialogViewHolder;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.Notifi;
import com.routeal.cocoger.util.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FB {

    public final static String USER_AVAILABLE = "user_available";
    public final static String USER_UPDATED = "user_updated";
    public final static String USER_LOCATION_UPDATE = "user_location_update";
    public final static String FRIEND_LOCATION_ADD = "friend_location_add";
    public final static String FRIEND_LOCATION_UPDATE = "friend_location_update";
    public final static String FRIEND_LOCATION_REMOVE = "friend_location_remove";
    public final static String FRIEND_RANGE_UPDATE = "friend_range_update";
    public final static String FRIEND_MARKER_SHOW = "friend_marker_show";
    public final static String DIRECTION_ROUTE_ADD = "direction_route_add";
    public final static String DIRECTION_ROUTE_REMOVE = "direction_route_remove";
    public final static String PLACE_SAVE = "place_save";
    public final static String PLACE_EDIT = "place_edit";
    public final static String PLACE_REMOVE = "place_remove";
    public final static String PLACE_SHOW = "place_show";
    public final static String GROUP_CREATE = "group_create";
    public final static String GROUP_EDIT = "group_edit";

    public final static String KEY = "key";
    public final static String LOCATION = "location";
    public final static String ADDRESS = "address";
    public final static String TITLE = "title";
    public final static String IMAGE = "image";
    public final static String PLACE = "place";

    public final static String ACTION_FRIEND_REQUEST_ACCEPTED = "FRIEND_REQUEST_ACCEPTED";
    public final static String ACTION_FRIEND_REQUEST_DECLINED = "FRIEND_REQUEST_DECLINED";
    public final static String ACTION_RANGE_REQUEST_ACCEPTED = "RANGE_REQUEST_ACCEPTED";
    public final static String ACTION_RANGE_REQUEST_DECLINED = "RANGE_REQUEST_DECLINED";
    public final static String NOTIFI_RANGE_REQUESTER = "range_requester";
    public final static String NOTIFI_RANGE = "range";
    public final static String NOTIFI_FRIEND_INVITE = "friend_invite";

    public final static String USER_CATEGORY = "users";
    public final static String PLACE_CATEGORY = "places";

    public final static String PROFILE_IMAGE = "profile.jpg";
    public final static String PLACE_IMAGE = "place.jpg";

    private final static String TAG = "FB";

    private static boolean mHasMonitoringStarted = false;

    private static User mUser;

    public static User getUser() {
        return mUser;
    }

    public static void setUser(User user) {
        mUser = user;
    }

    public static String getUid() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser == null) {
            throw new RuntimeException("Fatal error: current user not available.");
        }
        return fUser.getUid();
    }

    private static DatabaseReference getFeedbackDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference().child("feedbacks");
    }

    private static DatabaseReference getDeviceDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference().child("devices");
    }

    private static DatabaseReference getUserDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference().child("users");
    }

    private static DatabaseReference getFriendDatabaseReference(String user) {
        return FirebaseDatabase.getInstance().getReference().child("friends").child(user);
    }

    private static DatabaseReference getFriendDatabaseReference(String user, String friend) {
        return FirebaseDatabase.getInstance().getReference().child("friends").child(user).child(friend);
    }

    private static DatabaseReference getLocationDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference().child("locations");
    }

    private static DatabaseReference getUserLocationDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference().child("user_locations");
    }

    public static boolean isAuthenticated() {
        return (FirebaseAuth.getInstance().getCurrentUser() != null);
    }

    public static boolean isCurrentUser(String key) {
        return (key != null && key.equals(getUid()));
    }

    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    // monitoring authentication starts normally in background without UI
    public static void monitorAuthentication() {
        Log.d(TAG, "monitorAuthentication");

        if (mHasMonitoringStarted) return;

        mHasMonitoringStarted = true;

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "Firebase User authenticated:" + user.getEmail());
                    monitorUserDatabase();
                } else {
                    Log.d(TAG, "Firebase User invalidated");
                    FB.setUser(null);
                    LocationUpdateService.stop();
                }
            }
        });
    }

    // Even without UI, monitor the user(myself), my friends, and my groups.
    private static void monitorUserDatabase() {
        String key = getUid();

        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("users").child(key);
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                User newUser = dataSnapshot.getValue(User.class);
                User oldUser = FB.getUser();
                if (newUser == null && oldUser == null) {
                    onCreateUser(key);
                } else if (oldUser == null){
                    onSignIn(key, newUser);
                } else {
                    onUpdateUser(newUser, oldUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        db = FirebaseDatabase.getInstance().getReference().child("friends").child(key);
        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                Friend friend = dataSnapshot.getValue(Friend.class);
                if (!key.equals(FB.getUid())) {
                    FriendManager.add(key, friend);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                Friend friend = dataSnapshot.getValue(Friend.class);
                FriendManager.change(key, friend);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                Friend friend = dataSnapshot.getValue(Friend.class);
                FriendManager.remove(key);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        db = FirebaseDatabase.getInstance().getReference().child("user_groups").child(key);
        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("groups").child(key);
                db.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String key = dataSnapshot.getKey();
                        Group group = dataSnapshot.getValue(Group.class);
                        Log.d(TAG, "key = " + key + " " + group.getName());
                        GroupManager.add(key, group);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("groups").child(key);
                db.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String key = dataSnapshot.getKey();
                        Group group = dataSnapshot.getValue(Group.class);
                        Log.d(TAG, "key = " + key + " " + group.getName());
                        GroupManager.change(key, group);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                GroupManager.remove(key);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void signIn(Activity activity, String email, String password, final SignInListener listener) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (listener != null) listener.onSuccess();
                        } else {
                            Exception e = task.getException();
                            Log.d(TAG, "Failed to sign in", e);
                            if (listener != null) {
                                if (e != null) {
                                    listener.onFail(e.getLocalizedMessage());
                                } else {
                                    listener.onFail("Failed to sign in.  Try again.");
                                }
                            }
                        }
                    }
                });
    }

    public static void createUser(Activity activity, String email, String password, final CreateUserListener listener) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            if (listener != null) listener.onSuccess(user.getUid());
                        } else {
                            Exception e = task.getException();
                            Log.d(TAG, "Failed to create a user", e);
                            if (listener != null) {
                                if (e != null) {
                                    listener.onFail(e.getLocalizedMessage());
                                } else {
                                    listener.onFail("Failed to create a user.  Try again.");
                                }
                            }
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
                            Exception e = task.getException();
                            Log.d(TAG, "Failed to reset a password", e);
                            if (listener != null) {
                                if (e != null) {
                                    listener.onFail(e.getLocalizedMessage());
                                } else {
                                    listener.onFail("Failed to reset a password.  Try again.");
                                }
                            }
                        }
                    }
                });
    }

    public static void saveLocation(Location location, Address address) {
        if (location != null && address != null) {
            saveLocation(location, address, null);
        }
    }

    public static void saveLocation(Location location, Address address, SaveLocationListener listener) {
        if (location != null && address != null) {
            saveLocation(location, address, true, listener);
        }
    }

    public static void saveLocation(Location location, Address address, boolean notifyFriend,
                                    final SaveLocationListener listener) {
        Log.d(TAG, "saveLocation:");

        if (location == null || address == null) {
            return;
        }

        // very first location update comes before getting the user
        if (getUser() == null) {
            return;
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LocationAddress loc = new LocationAddress();
        loc.setUid(getUid());
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

        // uid
        String uid = getUid();

        // top level database reference
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        // location key
        final String key = db.child("locations").push().getKey();

        GeoHash geoHash = new GeoHash(new GeoLocation(latitude, longitude));

        Map<String, Object> updates = new HashMap<>();
        // location
        updates.put("locations/" + key, loc);
        // geo location
        updates.put("geo_locations/" + key + "/g", geoHash.getGeoHashString());
        updates.put("geo_locations/" + key + "/l", Arrays.asList(latitude, longitude));
        // user locations
        updates.put("user_locations/" + uid + "/" + key, loc.getTimestamp());
        updates.put("users/" + uid + "/location/", key);

        if (notifyFriend) {
            if (FriendManager.getFriends().size() == 0) {
                Log.d(TAG, "savelocation: no friend");
            }
            for (Map.Entry<String, Friend> entry : FriendManager.getFriends().entrySet()) {
                updates.put("friends/" + entry.getKey() + "/" + uid + "/location", key);
            }
        }

        db.updateChildren(updates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    if (listener != null) listener.onSuccess(key);
                } else {
                    if (listener != null) listener.onFail(databaseError.getMessage());
                }
            }
        });
    }

    public static void updateUser(String displayName, String gender, String birthYear, final CompleteListener listener) {
        String uid = getUid();

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> updates = new HashMap<>();

        if (displayName != null) {
            displayName = displayName.trim();
            updates.put("users/" + uid + "/displayName/", displayName);
            updates.put("users/" + uid + "/searchedName/", displayName.toLowerCase());
        }
        if (gender != null) {
            gender = gender.trim();
            updates.put("users/" + uid + "/gender/", gender.toLowerCase());
        }
        if (birthYear != null) {
            birthYear = birthYear.trim();
            updates.put("users/" + uid + "/birthYear/", birthYear.toLowerCase());
        }
        if (updates.size() > 0) {
            db.updateChildren(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (listener != null) listener.onSuccess();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (listener != null) listener.onFail(e.getLocalizedMessage());
                        }
                    });
        }
    }

    private static void onSignIn(String uid, User user) {
        Log.d(TAG, "onSignIn:" + user.toString());

        // save the user in the memory
        FB.setUser(user);

        // let the map activity know that the user becomes available
        // so that the map can really get started.  Sometimes the map
        // comes faster than the user available from the database.
        Intent intent = new Intent(FB.USER_AVAILABLE);
        LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);

        // the current device
        Device currentDevice = Utils.getDevice();

        String key = null;

        // find the device key to match the device id in the user database
        Map<String, String> devices = user.getDevices();
        if (devices != null && !devices.isEmpty()) {
            for (Map.Entry<String, String> entry : devices.entrySet()) {
                // the values is a device id
                if (entry.getValue().equals(currentDevice.getDeviceId())) {
                    key = entry.getKey();
                }
            }
        }

        DatabaseReference deviceDb = getDeviceDatabaseReference();

        if (key != null) {
            // update the timestamp of the device
            deviceDb.child(key).child("timestamp").setValue(currentDevice.getTimestamp());
        } else {
            // set the uid to the device before save
            currentDevice.setUid(uid);

            // add it as a new device
            String newKey = deviceDb.push().getKey();
            deviceDb.child(newKey).setValue(currentDevice);

            // also add it to the user database under 'devices'
            DatabaseReference userDb = getUserDatabaseReference();
            userDb.child(uid).child("devices").child(newKey).setValue(currentDevice.getDeviceId());
        }
    }

    private static void onCreateUser(String uid) {
        Log.d(TAG, "onCreateUser: uid=" + uid);

        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

        User user = new User();
        if (fbUser != null) {
            user.setEmail(fbUser.getEmail());
        }

        // save the user in the memory
        FB.setUser(user);

        // save the device info
        Device device = Utils.getDevice();
        device.setUid(uid);

        DatabaseReference deviceDb = getDeviceDatabaseReference();
        String key = deviceDb.push().getKey();
        deviceDb.child(key).setValue(device);

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

    private static void onUpdateUser(User newUser, User oldUser) {
        Log.d(TAG, "onUpdateUser");

        Map<String, Long> invitees = newUser.getInvitees();
        if (invitees != null && !invitees.isEmpty()) {
            for (Map.Entry<String, Long> entry : invitees.entrySet()) {
                if (entry.getValue() > 0) {
                    if (oldUser.getInvitees() == null ||
                            oldUser.getInvitees().get(entry.getKey()) == null) {
                        Log.d(TAG, "received new friends request");
                        sendInviteNotification(entry.getKey(), entry.getValue());
                    } else {
                        Log.d(TAG, "received old friends request");
                    }
                }
            }
        }

        FB.setUser(newUser);
    }

    public static void initUser(User user) {
        String uid = getUid();
        DatabaseReference db = getUserDatabaseReference();
        db.child(uid).setValue(user);
    }

    private static void sendEmailVerification() {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser != null) {
            fbUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "sendEmailVerification: successfully sent");
                    }
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean checkFriendRequest(RecyclerView.Adapter a) {
        FirebaseRecyclerAdapter<User, UserDialogViewHolder> adapter =
                (FirebaseRecyclerAdapter<User, UserDialogViewHolder>) a;
        boolean accepted = false;

        Map<String, Long> invitees = FB.getUser().getInvitees();

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

    @SuppressWarnings("unchecked")
    public static boolean sendFriendRequest(RecyclerView.Adapter a) {
        FirebaseRecyclerAdapter<User, UserDialogViewHolder> adapter =
                (FirebaseRecyclerAdapter<User, UserDialogViewHolder>) a;

        boolean modified = false;

        String uid = getUid();
        DatabaseReference userDb = getUserDatabaseReference();

        for (int i = 0; i < adapter.getItemCount(); i++) {
            DatabaseReference friendDb = adapter.getRef(i);
            if (friendDb.getKey().equals(uid)) {
                // trying to add myself to friends
                continue;
            }

            String key = adapter.getRef(i).getKey();
            if (FriendManager.getFriend(key) != null) {
                // already being friend
                continue;
            }

            // set the timestamp when the friend request is added.
            // When the requested user approved, the timestamp will be changed to true.
            long timestamp = System.currentTimeMillis();

            // add myself to friend
            friendDb.child("invitees").child(uid).setValue(timestamp);

            // add friends to myself
            userDb.child(uid).child("invites").child(key).setValue(timestamp);

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
                        friend.setLocation(inviteUser.getLocation());

                        DatabaseReference friendDb = getFriendDatabaseReference(invitee, invite);
                        friendDb.setValue(friend);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        // invite - me added to invite
        Friend myInfo = new Friend();
        myInfo.setCreated(timestamp);
        myInfo.setRange(defaultLocationChange);
        myInfo.setDisplayName(FB.getUser().getDisplayName());
        myInfo.setLocation(FB.getUser().getLocation());

        DatabaseReference friendDb = getFriendDatabaseReference(invite, invitee);
        friendDb.setValue(myInfo);
    }

    public static void declineFriendRequest(String invite) {
        // delete the invite and invitee from the database
        String invitee = getUid();
        DatabaseReference userDb = getUserDatabaseReference();
        userDb.child(invitee).child("invitees").child(invite).removeValue();
        userDb.child(invite).child("invites").child(invitee).removeValue();
    }

    public static void cancelFriendRequest(String invitee) {
        String invite = getUid();
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

        DatabaseReference myDb = getFriendDatabaseReference(uid, fid);
        myDb.child("range").setValue(range);

        DatabaseReference friendDb = getFriendDatabaseReference(fid, uid);
        friendDb.child("range").setValue(range);
    }

    public static void sendChangeRequest(String fid, int range) {
        String uid = getUid();

        RangeRequest rangeRequest = new RangeRequest();
        rangeRequest.setCreated(System.currentTimeMillis());
        rangeRequest.setRange(range);

        DatabaseReference friendDb = getFriendDatabaseReference(fid, uid);
        friendDb.child("rangeRequest").setValue(rangeRequest);
    }

    public static void unfriend(String fid) {
        String uid = getUid();

        DatabaseReference friendDb = getFriendDatabaseReference(fid, uid);
        friendDb.removeValue();

        DatabaseReference myDb = getFriendDatabaseReference(uid, fid);
        myDb.removeValue();
    }

    public static void uploadPlaceImage(byte[] bytes, String key, UploadDataListener listener) {
        String filename = key + "_" + FB.PLACE_IMAGE;
        String refName = FB.PLACE_CATEGORY + "/" + FB.getUid() + "/" + filename;
        uploadData(bytes, refName, listener);
    }

    public static void downloadPlaceImage(String uid, String key, DownloadDataListener listener) {
        String filename = key + "_" + FB.PLACE_IMAGE;
        String refName = FB.PLACE_CATEGORY + "/" + uid + "/" + filename;
        downloadData(refName, listener);
    }

    public static void deletePlaceImage(String uid, String key, DeleteDataListener listener) {
        String filename = key + "_" + FB.PLACE_IMAGE;
        String refName = FB.PLACE_CATEGORY + "/" + uid + "/" + filename;
        deleteData(refName, listener);
    }

    public static void uploadProfileImage(byte[] bytes, UploadDataListener listener) {
        String refName = FB.USER_CATEGORY + "/" + FB.getUid() + "/" + FB.PROFILE_IMAGE;
        uploadData(bytes, refName, listener);
    }

    public static void downloadProfileImage(String key, DownloadDataListener listener) {
        String refName = FB.USER_CATEGORY + "/" + key + "/" + FB.PROFILE_IMAGE;
        downloadData(refName, listener);
    }

    public static void deleteProfileImage(String key, DeleteDataListener listener) {
        String refName = FB.USER_CATEGORY + "/" + key + "/" + FB.PROFILE_IMAGE;
        deleteData(refName, listener);
    }

    // delete the existing image first and then upload
    public static void uploadData(final byte[] bytes, String refName, final UploadDataListener listener) {
        final StorageReference ref = FirebaseStorage.getInstance().getReference().child(refName);
        ref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        String url = null;
                        if (downloadUrl != null) {
                            url = downloadUrl.toString();
                        }
                        if (listener != null) listener.onSuccess(url);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (listener != null) listener.onFail(e.getLocalizedMessage());
                    }
                });
            }
        });
    }

    public static void downloadData(String refName, final DownloadDataListener listener) {
        long ONE_MEGABYTE = 1024 * 1024;
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(refName);
        ref.getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        if (listener != null) listener.onSuccess(bytes);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (listener != null) listener.onFail(e.getLocalizedMessage());
                    }
                });
    }

    public static void deleteData(String refName, final DeleteDataListener listener) {
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(refName);
        ref.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (listener != null) listener.onDone(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (listener != null) listener.onDone(e.getLocalizedMessage());
                    }
                });
    }

    public static void sendRangeNotification(String uid, Friend friend, int requestRange, int currentRange) {
        Context context = MainApplication.getContext();

        Intent acceptIntent = new Intent(context, PanelMapActivity.class);
        acceptIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        acceptIntent.setAction(ACTION_RANGE_REQUEST_ACCEPTED);
        acceptIntent.putExtra(NOTIFI_RANGE_REQUESTER, uid);
        acceptIntent.putExtra(NOTIFI_RANGE, requestRange);

        Intent declineIntent = new Intent(context, LocationUpdateService.class);
        declineIntent.setAction(ACTION_RANGE_REQUEST_DECLINED);
        declineIntent.putExtra(NOTIFI_RANGE_REQUESTER, uid);

        String to = LocationRange.toString(requestRange);
        String from = LocationRange.toString(currentRange);
        String pattern = context.getResources().getString(R.string.receive_range_request);
        String content = String.format(pattern, to, from);
        int nid = Math.abs((int) friend.getRangeRequest().getCreated());

        Notifi.send(nid, uid, friend.getDisplayName(), content, acceptIntent, declineIntent);
    }

    private static void sendInviteNotification(final String invite, final long timestamp) {
        DatabaseReference userRef = getUserDatabaseReference();

        userRef.child(invite).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User inviteUser = dataSnapshot.getValue(User.class);

                String displayName = "";

                if (inviteUser == null) {
                    Log.d(TAG, "Failed to get a friend info for friend request");
                } else {
                    displayName = inviteUser.getDisplayName();
                }

                Context context = MainApplication.getContext();

                // accept starts the main activity with the friend view
                Intent acceptIntent = new Intent(context, PanelMapActivity.class);
                acceptIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                acceptIntent.setAction(ACTION_FRIEND_REQUEST_ACCEPTED);
                acceptIntent.putExtra(NOTIFI_FRIEND_INVITE, invite);

                Intent declineIntent = new Intent(context, LocationUpdateService.class);
                declineIntent.setAction(ACTION_FRIEND_REQUEST_DECLINED);
                declineIntent.putExtra(NOTIFI_FRIEND_INVITE, invite);

                String pattern = context.getResources().getString(R.string.receive_friend_request);
                String content = String.format(pattern, displayName);

                int nid = Math.abs((int) timestamp);

                Notifi.send(nid, invite, displayName, content, acceptIntent, declineIntent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public static FirebaseRecyclerAdapter<Place, PlaceListViewHolder> getPlaceRecyclerAdapter(
            final PagerFragment.ChangeListener changeListener,
            final RecyclerAdapterListener<Place> listener) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        Query keyQuery = db.child("user_places").child(getUid()).orderByValue();

        DatabaseReference dbRef = db.child("places");

        FirebaseRecyclerOptions<Place> options =
                new FirebaseRecyclerOptions.Builder<Place>()
                        .setIndexedQuery(keyQuery, dbRef, Place.class)
                        .build();

        return new FirebaseRecyclerAdapter<Place, PlaceListViewHolder>(options) {
            @Override
            public PlaceListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listview_place_list, parent, false);
                return new PlaceListViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(PlaceListViewHolder holder, int position, Place model) {
                holder.bind(getRef(position).getKey(), model);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                if (changeListener != null) changeListener.onEmpty(getItemCount() == 0);
            }

            @Override
            public void onChildChanged(ChangeEventType type, DataSnapshot snapshot, int newIndex, int oldIndex) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex);
                Place place = snapshot.getValue(Place.class);
                String key = snapshot.getKey();
                if (type == ChangeEventType.ADDED) {
                    Log.d(TAG, "Place added");
                    if (listener != null) {
                        listener.onAdded(key, place);
                    }
                } else if (type == ChangeEventType.CHANGED) {
                    Log.d(TAG, "Place changed");
                    if (listener != null) {
                        listener.onChanged(key, place);
                    }
                } else if (type == ChangeEventType.MOVED) {
                    Log.d(TAG, "Place moved");
                } else if (type == ChangeEventType.REMOVED) {
                    Log.d(TAG, "Place removed");
                    if (listener != null) {
                        listener.onRemoved(key);
                    }
                }
            }
        };
    }

    public static FirebaseRecyclerAdapter<User, UserDialogViewHolder> getUserRecyclerAdapter(String text, final View view) {
        DatabaseReference db = getUserDatabaseReference();

        Query query = db
                .orderByChild("searchedName")
                .limitToFirst(40)
                .startAt(text)
                .endAt(text + "~");

        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(query, User.class)
                        .build();

        return new FirebaseRecyclerAdapter<User, UserDialogViewHolder>(options) {
            @Override
            public UserDialogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listview_user_list, parent, false);
                return new UserDialogViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(UserDialogViewHolder holder, int position, User model) {
                holder.bind(model, getRef(position).getKey());
            }

            @Override
            public void onDataChanged() {
                // NOTE: this gets called when new people are added to the friends
                TextView emptyListMessage = (TextView) view.findViewById(R.id.empty_list_text);
                emptyListMessage.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onChildChanged(ChangeEventType type, DataSnapshot snapshot, int newIndex, int oldIndex) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex);
            }
        };
    }

    public static void getUser(String key, final UserListener listener) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("users");
        db.child(key).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            if (listener != null) {
                                listener.onSuccess(user);
                            }
                        } else {
                            if (listener != null) {
                                listener.onFail("Failed to get a User object");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Exception e = databaseError.toException();
                        Log.d(TAG, "Failed to get a User object", e);
                        if (listener != null) {
                            if (e != null) {
                                listener.onFail(e.getLocalizedMessage());
                            } else {
                                listener.onFail("Failed to get a User object.");
                            }
                        }
                    }
                }
        );
    }

    // return the latest location of the specified user
    public static void getUserLocation(String key, final LocationListener listener) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("user_locations").child(key);
        Query query = db.orderByKey().limitToLast(1);
        query.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String locKey = null;
                        // there should be only one
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            locKey = snapshot.getKey();
                        }
                        if (locKey == null) {
                            if (listener != null) {
                                listener.onFail("Failed to get a location object.");
                            }
                            return;
                        }
                        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("locations").child(locKey);
                        db.addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        LocationAddress la = dataSnapshot.getValue(LocationAddress.class);
                                        if (la != null) {
                                            if (listener != null) {
                                                Location location = Utils.getLocation(la);
                                                Address address = Utils.getAddress(location);
                                                listener.onSuccess(location, address);
                                            }
                                        } else {
                                            if (listener != null) {
                                                listener.onFail("Failed to get a location object.");
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Exception e = databaseError.toException();
                                        Log.d(TAG, "Failed to get a location object", e);
                                        if (listener != null) {
                                            if (e != null) {
                                                listener.onFail(e.getLocalizedMessage());
                                            } else {
                                                listener.onFail("Failed to get a location object.");
                                            }
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Exception e = databaseError.toException();
                        Log.d(TAG, "Failed to get a location object", e);
                        if (listener != null) {
                            if (e != null) {
                                listener.onFail(e.getLocalizedMessage());
                            } else {
                                listener.onFail("Failed to get a location object.");
                            }
                        }
                    }
                });
    }

    public static void getLocation(String key, final LocationListener listener) {
        DatabaseReference db = getLocationDatabaseReference();
        db.child(key).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        LocationAddress la = dataSnapshot.getValue(LocationAddress.class);
                        if (la != null) {
                            if (listener != null) {
                                Location location = Utils.getLocation(la);
                                Address address = Utils.getAddress(location);
                                listener.onSuccess(location, address);
                            }
                        } else {
                            if (listener != null) {
                                listener.onFail("Failed to get a location object.");
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Exception e = databaseError.toException();
                        Log.d(TAG, "Failed to get a location object", e);
                        if (listener != null) {
                            if (e != null) {
                                listener.onFail(e.getLocalizedMessage());
                            } else {
                                listener.onFail("Failed to get a location object.");
                            }
                        }
                    }
                }
        );
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
                    String locationKey = childDataSnapshot.getKey();
                    getLocation(locationKey, new LocationListener() {
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

    public static void addPlace(final String key, final Place place, final PlaceListener listener) {
        String uid = FB.getUid();

        Map<String, Object> updates = new HashMap<>();

        updates.put("places/" + key, place);
        updates.put("user_places/" + uid + "/" + key, uid);

        if (place.getSeenBy().equals("friends")) {
            for (Map.Entry<String, Friend> entry : FriendManager.getFriends().entrySet()) {
                updates.put("user_places/" + entry.getKey() + "/" + key, uid);
            }
        }

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.updateChildren(updates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    if (listener != null) listener.onSuccess(key, place);
                } else {
                    // error
                    if (listener != null) listener.onFail(databaseError.getMessage());
                }
            }
        });
    }

    public static void addPlace(final Place place, final Bitmap bitmap, final PlaceListener listener) {
        if (place == null) return;

        DatabaseReference placeDb = FirebaseDatabase.getInstance().getReference().child("places");
        final String key = placeDb.push().getKey();

        if (bitmap == null) {
            addPlace(key, place, listener);
        } else {
            byte bytes[] = Utils.getBitmapBytes(MainApplication.getContext(), bitmap);
            if (bytes != null) {
                // no error handling
                uploadPlaceImage(bytes, key, new UploadDataListener() {
                    @Override
                    public void onSuccess(String url) {
                        addPlace(key, place, listener);
                    }

                    @Override
                    public void onFail(String err) {
                        addPlace(key, place, listener);
                    }
                });
            }
        }
    }

    public static void editPlace(final String key, Place place, Bitmap bitmap, final CompleteListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("places/" + key, place);
        for (Map.Entry<String, Friend> entry : FriendManager.getFriends().entrySet()) {
            if (place.getSeenBy().equals("friends")) {
                updates.put("user_places/" + entry.getKey() + "/" + key, place.getUid());
            } else {
                updates.put("user_places/" + entry.getKey() + "/" + key, null);
            }
        }
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.updateChildren(updates);

        if (bitmap == null) {
            if (listener != null) listener.onSuccess();
            return;
        }

        final byte bytes[] = Utils.getBitmapBytes(MainApplication.getContext(), bitmap);
        if (bytes == null) {
            if (listener != null) listener.onSuccess();
            return;
        }

        deletePlaceImage(getUid(), key, null);

        uploadPlaceImage(bytes, key, null);

        if (listener != null) listener.onSuccess();
    }

    public static void deletePlace(final String key, Place place, final CompleteListener listener) {
        final String uid = getUid();

        Map<String, Object> updates = new HashMap<>();

        updates.put("places/" + key, null);
        updates.put("user_places/" + uid + "/" + key, null);

        if (place.getSeenBy().equals("friends")) {
            for (Map.Entry<String, Friend> entry : FriendManager.getFriends().entrySet()) {
                updates.put("user_places/" + entry.getKey() + "/" + key, null);
            }
        }

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.updateChildren(updates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    // success
                    deletePlaceImage(uid, key, null);
                    if (listener != null) listener.onSuccess();
                } else {
                    // error
                    if (listener != null) listener.onFail(databaseError.getMessage());
                }
            }
        });
    }

    public static void saveFeedback(Feedback feedback, final CompleteListener listner) {
        DatabaseReference db = getFeedbackDatabaseReference();

        String newKey = db.push().getKey();
        db.child(newKey).setValue(feedback, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    if (listner != null) listner.onFail(databaseError.getMessage());
                } else {
                    if (listner != null) listner.onSuccess();
                }
            }
        });
    }

    public static void createGroup(String name, String color, List<String> keys) {
        Group group = new Group();
        group.setName(name);
        group.setColor(color);

        Map<String, Member> members = new HashMap<>();

        for (String key : keys) {
            Member member = new Member();
            member.setStatus(Member.INVITED);
            member.setTimestamp(System.currentTimeMillis());
            members.put(key, member);
        }

        Member member = new Member();
        member.setStatus(Member.CREATED);
        member.setTimestamp(System.currentTimeMillis());
        members.put(FB.getUid(), member);

        group.setMembers(members);

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        String key = db.child("groups").push().getKey();

        Map<String, Object> updates = new HashMap<>();
        updates.put("groups/" + key, group);

        for (Map.Entry<String, Member> entry : group.getMembers().entrySet()) {
            updates.put("user_groups/" + entry.getKey() + "/" + key, false);
        }

        db.updateChildren(updates);
    }

    public static void updateGroup(String name, String color, List<String> keys) {

    }

    public static void deleteGroup(String key, Group group) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("groups/" + key, null);
        for (Map.Entry<String, Member> entry : group.getMembers().entrySet()) {
            updates.put("user_groups/" + entry.getKey() + "/" + key, null);
        }
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.updateChildren(updates);
    }

    public static void joinGroup(String key) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.child("groups").child(key).child("members").child(getUid()).child("status").setValue(Member.JOINED);
    }

    public static void removeGroup(String key) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("groups/" + key + "/members/" + getUid(), null);
        updates.put("user_groups/" + getUid() + "/" + key, null);
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.updateChildren(updates);
    }

    public interface UploadDataListener {
        void onSuccess(String url);

        void onFail(String err);
    }

    public interface DownloadDataListener {
        void onSuccess(byte[] bytes);

        void onFail(String err);
    }

    public interface DeleteDataListener {
        void onDone(String err);
    }

    public interface SignInListener {
        void onSuccess();

        void onFail(String err);
    }


    public interface CreateUserListener {
        void onSuccess(String key);

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

    public interface PlaceListener {
        void onSuccess(String key, Place place);

        void onFail(String err);
    }

    public interface CompleteListener {
        void onSuccess();

        void onFail(String err);
    }

    public interface SaveLocationListener {
        void onSuccess(String key);

        void onFail(String err);
    }
}
