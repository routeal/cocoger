package com.routeal.cocoger.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.net.RestClient;
import com.routeal.cocoger.provider.DBUtil;
import com.routeal.cocoger.ui.main.SlidingUpPanelMapActivity;
import com.routeal.cocoger.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by nabe on 7/4/17.
 */

public class FacebookLoginActivity extends AppCompatActivity {
    private final static String TAG = "FacebookLoginActivity";

    private CallbackManager callbackManager;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_facebook_login);

        // initialize the Facebook SDK
        FacebookSdk.sdkInitialize(MainApplication.getContext());

        callbackManager = CallbackManager.Factory.create();

        // setting the HTML text can be done only in programatically
        TextView signupNote = (TextView) findViewById(R.id.signup_textview);
        signupNote.setText(android.text.Html.fromHtml(getString(R.string.signup_note),
                Html.FROM_HTML_MODE_LEGACY));
        signupNote.setMovementMethod(LinkMovementMethod.getInstance());

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);

        loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_friends"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                dialog = Utils.spinBusyCursor(FacebookLoginActivity.this);
                getUserInfo();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // retrieve the Facebook user info
    void getUserInfo() {
        GraphRequest request;
        GraphRequestBatch batch = new GraphRequestBatch();

        request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                if (object != null) {
                    try {
                        User user = DBUtil.getUser();
                        if (!object.isNull("name")) {
                            user.setName(object.getString("name"));
                        }
                        if (!object.isNull("first_name")) {
                            user.setFirstName(object.getString("first_name"));
                        }
                        if (!object.isNull("last_name")) {
                            user.setLastName(object.getString("last_name"));
                        }
                        if (!object.isNull("locale")) {
                            user.setLocale(object.getString("locale"));
                        }
                        if (!object.isNull("picture")) {
                            JSONObject picture = object.getJSONObject("picture");
                            if (picture != null) {
                                JSONObject data = picture.getJSONObject("data");
                                if (!data.isNull("url")) {
                                    user.setPicture(data.getString("url"));
                                }
                            }
                        }
                        if (!object.isNull("timezone")) {
                            user.setTimezone(object.getString("timezone"));
                        }
                        if (!object.isNull("updated_time")) {
                            user.setUpdated(object.getString("updated_time"));
                        }
                        if (!object.isNull("email")) {
                            user.setEmail(object.getString("email"));
                        }
                    } catch (JSONException e) {
                    }
                }
            }
        });

        Bundle parameters = new Bundle();
        String permissions = "id,name,first_name,last_name,gender,locale,picture,timezone,updated_time,email";
        parameters.putString("fields", permissions);
        request.setParameters(parameters);
        batch.add(request);

        request = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONArrayCallback() {
            @Override
            public void onCompleted(JSONArray jsonArray, GraphResponse response) {
                try {
                    if (jsonArray != null && jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Friend friend = new Friend();
                            JSONObject object = jsonArray.getJSONObject(i);
                            if (!object.isNull("id")) {
                                friend.setProviderId(object.getString("id"));
                            }
                            if (!object.isNull("name")) {
                                friend.setName(object.getString("name"));
                            }
                            if (!object.isNull("first_name")) {
                                friend.setFirstName(object.getString("first_name"));
                            }
                            if (!object.isNull("locale")) {
                                friend.setLocale(object.getString("locale"));
                            }
                            if (!object.isNull("last_name")) {
                                friend.setLastName(object.getString("last_name"));
                            }
                            if (!object.isNull("updated_time")) {
                                friend.setUpdated(object.getString("updated_time"));
                            }
                            if (!object.isNull("picture")) {
                                JSONObject picture = object.getJSONObject("picture");
                                if (picture != null) {
                                    JSONObject data = picture.getJSONObject("data");
                                    if (!data.isNull("url")) {
                                        friend.setPicture(data.getString("url"));
                                    }
                                }
                            }
                            DBUtil.saveFriend(friend);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        parameters = new Bundle();
        permissions = "id,name,first_name,last_name,gender,locale,picture,timezone,updated_time,email";
        parameters.putString("fields", permissions);
        request.setParameters(parameters);
        batch.add(request);

        batch.addCallback(new GraphRequestBatch.Callback() {
            @Override
            public void onBatchCompleted(GraphRequestBatch graphRequests) {
                if (DBUtil.getUser().getEmail().isEmpty()) {
                    dialog.dismiss();
                    // FIXME: if the email is empty, it means that the
                    // fb login failed. don't know what to do
                } else {
                    loginToServer();
                }
            }
        });

        batch.executeAsync();
    }

    // login the user to the server
    private void loginToServer() {
        final User user = DBUtil.getUser();

        // set the device info for login
        user.setDevice(Utils.getDevice());

        Log.d(TAG, "user: " + user.toString());

        Call<User> login = RestClient.service().login(RestClient.token(), user);

        login.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                dialog.dismiss();
                if (response.isSuccessful()) {
                    Log.d(TAG, "Login succeeded: " + response.body().toString());

                    // save the user into the database
                    DBUtil.saveUser(user);

                    Intent intent = new Intent(getApplicationContext(),
                            SlidingUpPanelMapActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.d(TAG, "Login failed: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                dialog.dismiss();
                Log.d(TAG, "Login failure:" + t.getLocalizedMessage());
            }
        });
    }

}
