package com.routeal.cocoger.fb;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
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
import com.routeal.cocoger.service.OnBootReceiver;
import com.routeal.cocoger.ui.main.PanelMapActivity;
import com.routeal.cocoger.util.CircleTransform;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.routeal.cocoger.service.MainService.ACTION_RANGE_REQUEST_ACCEPTED;
import static com.routeal.cocoger.service.MainService.ACTION_RANGE_REQUEST_DECLINED;
import static com.routeal.cocoger.service.MainService.USER_AVAILABLE;

public class FB {


    private final static String TAG = "FB";

    public static final String ACTION_FRIEND_REQUEST_ACCEPTED = "FRIEND_REQUEST_ACCEPTED";

    public static final String ACTION_FRIEND_REQUEST_DECLINED = "FRIEND_REQUEST_DECLINED";

    public static final String ACTION_RANGE_REQUEST_ACCEPTED = "RANGE_REQUEST_ACCEPTED";

    public static final String ACTION_RANGE_REQUEST_DECLINED = "RANGE_REQUEST_DECLINED";




    public interface SignInListener {
        void OnSuccess();

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

    static DatabaseReference getFriendDatabaseReference(String user, String friend) {
        return getUserDatabaseReference().child(user).child("friends").child(friend);
    }

    static DatabaseReference getLocationDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference().child("locations");
    }

    public static void monitorAuthentication() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth == null) return;

        // setting up a listener when the user is authenticated
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "Firebase User authenticated");
                    onUserAuth();
                } else {
                    Log.d(TAG, "Firebase User invalidated");
                    MainApplication.setUser(null);
                }
            }
        });
    }

    private static void onUserAuth() {
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

    public static void signIn(Activity ctx, String email, String password, final SignInListener listener) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(ctx, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (listener != null) listener.OnSuccess();
                        } else {
                            if (listener != null)
                                listener.onFail(task.getException().getLocalizedMessage());
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

    public void saveLocation(Location location, Address address) {
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
        updates.put("users/" + getUid() + "/locations/" + key, loc.getTimestamp());

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
            Intent intent = new Intent(USER_AVAILABLE);
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

        if (newFriends == null || oldFriends == null) {
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
            if (newFriends.size() > 0) {
                for (Map.Entry<String, Friend> entry : newFriends.entrySet()) {
                    String friendUid = entry.getKey();
                    Friend newFriend = entry.getValue();
                    Friend oldFriend = oldFriends.get(friendUid);

                    // possible new range request
                    if (newFriend.getRangeRequest() != null) {
                        int requestedRange = newFriend.getRangeRequest().getRange();
                        int currentRange = newFriend.getRange();

                        // new range request found
                        if (oldFriend.getRangeRequest() == null) {
                            // send the notification

                            // notification id
                            int nid = new Random().nextInt();

                            Context context = MainApplication.getContext();

                            // accept starts the main activity with the friend view
                            Intent acceptIntent = new Intent(context, PanelMapActivity.class);
                            acceptIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            acceptIntent.setAction(ACTION_RANGE_REQUEST_ACCEPTED);
                            acceptIntent.putExtra("range_requester", friendUid);
                            acceptIntent.putExtra("range", requestedRange);
                            acceptIntent.putExtra("notification_id", nid);
                            PendingIntent pendingAcceptIntent = PendingIntent.getActivity(context, 1, acceptIntent, 0);
                            NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(
                                    R.drawable.ic_contacts_black_18dp,
                                    "Accept", pendingAcceptIntent).build();

                            Intent declineIntent = new Intent(context, OnBootReceiver.class);
                            declineIntent.setAction(ACTION_RANGE_REQUEST_DECLINED);
                            declineIntent.putExtra("range_requester", friendUid);
                            declineIntent.putExtra("notification_id", nid);
                            PendingIntent pendingDeclineIntent = PendingIntent.getBroadcast(context, 1, declineIntent, 0);
                            NotificationCompat.Action declineAction = new NotificationCompat.Action.Builder(
                                    R.drawable.ic_contacts_black_18dp,
                                    "Decline", pendingDeclineIntent).build();

                            String to = LocationRange.toString(requestedRange);
                            String from = LocationRange.toString(currentRange);
                            String content = "You received a range request to " + to + " from " + from;

                            final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.ic_person_pin_circle_white_48dp)
                                    .setContentTitle(newFriend.getDisplayName())
                                    .setContentText(content)
                                    .setAutoCancel(true)
                                    .addAction(acceptAction)
                                    .addAction(declineAction);

                            // seems not working, use notificationmanager's cancel method
                            Notification notification = mBuilder.build();
                            notification.flags |= Notification.FLAG_AUTO_CANCEL;

                            Picasso.with(MainApplication.getContext()).load(newFriend.getPicture()).transform(new CircleTransform()).into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    mBuilder.setLargeIcon(bitmap);
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {
                                }
                            });

                            NotificationManager mNotificationManager =
                                    (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

                            mNotificationManager.notify(nid, notification);
                        }
                        // existing range request, show the notification if the timestamp is old
                        else {

                        }
                    }
                    // the range has been update, notify the map to change the marker location
                    else if (newFriend.getRange() != oldFriend.getRange()) {

                    }
                }
            }
        }

        MainApplication.setUser(nu);
    }

    public static void initUser(User user) {
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

    public void acceptFriendRequest(final String invite) {
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

    public void declineFriendRequest(String invite) {
        // delete the invite and invitee from the database
        String invitee = getUid();

        DatabaseReference userDb = getUserDatabaseReference();
        userDb.child(invitee).child("invitees").child(invite).removeValue();
        userDb.child(invite).child("invites").child(invitee).removeValue();
    }

    public void acceptRangeRequest(String requester, int range) {
        String responder = getUid();

        DatabaseReference resDb = getFriendDatabaseReference(responder, requester);
        resDb.child("range").setValue(range);
        resDb.child("rangeRequest").removeValue();

        DatabaseReference reqDb = getFriendDatabaseReference(requester, responder);
        reqDb.child("range").setValue(range);
    }

    public void declineRangeRequest(String requester) {
        String responder = getUid(); // myself

        DatabaseReference resDb = getFriendDatabaseReference(responder, requester);
        resDb.child("rangeRequest").removeValue();
    }

    public void changeRange(String fid, int range) {
        String uid = getUid();

        DatabaseReference myside = getFriendDatabaseReference(uid, fid);
        myside.child("range").setValue(range);

        DatabaseReference hisside = getFriendDatabaseReference(fid, uid);
        hisside.child("range").setValue(range);
    }

    public void sendChangeRequest(String fid, int range) {
        String uid = getUid();

        RangeRequest rangeRequest = new RangeRequest();
        rangeRequest.setCreated(System.currentTimeMillis());
        rangeRequest.setRange(range);

        DatabaseReference hisside = getFriendDatabaseReference(fid, uid);
        hisside.child("range").child("rangeRequest").setValue(rangeRequest);
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

    private static void sendInviteNotification(final String invite) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

        userRef.child(invite).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // notification id
                int nid = new Random().nextInt();

                User inviteUser = dataSnapshot.getValue(User.class);

                Context context = MainApplication.getContext();

                // accept starts the main activity with the friend view
                Intent acceptIntent = new Intent(context, PanelMapActivity.class);
                acceptIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                acceptIntent.setAction(ACTION_FRIEND_REQUEST_ACCEPTED);
                acceptIntent.putExtra("friend_invite", invite);
                acceptIntent.putExtra("notification_id", nid);
                PendingIntent pendingAcceptIntent = PendingIntent.getActivity(context, 1, acceptIntent, 0);
                NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(
                        R.drawable.ic_contacts_black_18dp,
                        "Accept", pendingAcceptIntent).build();

                Intent declineIntent = new Intent(context, OnBootReceiver.class);
                declineIntent.setAction(ACTION_FRIEND_REQUEST_DECLINED);
                declineIntent.putExtra("friend_invite", invite);
                declineIntent.putExtra("notification_id", nid);
                PendingIntent pendingDeclineIntent = PendingIntent.getBroadcast(context, 1, declineIntent, 0);
                NotificationCompat.Action declineAction = new NotificationCompat.Action.Builder(
                        R.drawable.ic_contacts_black_18dp,
                        "Decline", pendingDeclineIntent).build();

                String content = "You received a friend request from " + inviteUser.getDisplayName() + ".";

                final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_person_pin_circle_white_48dp)
                        .setContentTitle(inviteUser.getDisplayName())
                        .setContentText(content)
                        .setAutoCancel(true)
                        .addAction(acceptAction)
                        .addAction(declineAction);

                // seems not working, use notificationmanager's cancel method
                Notification notification = mBuilder.build();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;

                Picasso.with(MainApplication.getContext()).load(inviteUser.getPicture()).transform(new CircleTransform()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        mBuilder.setLargeIcon(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });

                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

                mNotificationManager.notify(nid, notification);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
