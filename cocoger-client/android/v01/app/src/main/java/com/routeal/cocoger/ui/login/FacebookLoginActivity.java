package com.routeal.cocoger.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.Device;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.net.RestClient;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_facebook_login);

        callbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);

        loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_friends"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                /*

                Device device = new Device();
                device.setId("1234");
                device.setPlatform("windows");
                device.setCountry("france");

                Call<User> login = RestClient.service().login(RestClient.token(), device);

                login.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Login succeeded: " + response.body().getAuthToken());
                        } else {
                            Log.d(TAG, "Login failed: " + response.errorBody().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.d(TAG, "retrofit2 fb login error:" + t.getLocalizedMessage());
                    }
                });

                */

                User user = null;

                GraphRequestBatch batch = new GraphRequestBatch();

                // Facebook Email address
                GraphRequest request = GraphRequest.newMeRequest(
                        AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                //07-07 22:28:56.811 8869-8869/com.routeal.cocoger V/FacebookLoginActivity: {Response:  responseCode: 200, graphObject:
                                // {"id":"10153852460631264","first_name":"Hiroshi","last_name":"Watanabe","locale":"en_US",
                                // "picture":{"data":{"is_silhouette":true,"url":"https:\/\/scontent.xx.fbcdn.net\/v\/t1.0-1\/c15.0.50.50\/p50x50\/10645251_10150004552801937_4553731092814901385_n.jpg?oh=e4e7a4c15fc8173d149c3b00f3619db9&oe=59D08C10"}},
                                // "timezone":-7,"updated_time":"2014-08-12T05:40:38+0000","email":"nabe@live.com"}, error: null}

                                    /*
                                if (object != null) {
                                try {
                                        String value;
                                        user = new User();
                                        if (!object.isNull("first_name")) {
                                            value = object.getString("first_name");
                                        }
                                        if (!object.isNull("last_name")) {
                                            value = object.getString("last_name");
                                        }
                                        if (!object.isNull("locale")) {
                                            value = object.getString("local");
                                        }
                                        if (!object.isNull("picture")) {
                                            JSONObject picture = jsonObject.getJSONObject("picture");
                                            if (picture != null) {
                                                JSONObject data = jsonObject.getJSONObject("data");
                                                if (!data.isNull("url")) {
                                                    value = data.getString("url");
                                                }
                                            }
                                        }
                                        if (!object.isNull("timezone")) {
                                            value = object.getString("timezone");
                                        }
                                        if (!object.isNull("updated_time")) {
                                            value = object.getString("updated_time");
                                        }
                                        if (!object.isNull("email")) {
                                            value = object.getString("email");
                                        }
                                } catch (JSONException e) {}
                                }
                                   */

                                // {Response:  responseCode: 400, graphObject: null, error: {HttpStatus: 400, errorCode: 100, errorType: OAuthException, errorMessage: (#100) Tried accessing nonexisting field (user_friends) on node type (User)}}

                                //if (response.getError())

                                Log.v(TAG, response.toString());
                                /*
                                try {
                                    String name = null;
                                    String link = null;
                                    String email = null;
                                    String birthday = null;
                                    String gender = null;
                                    if (!object.isNull("name")) {
                                        name = object.getString("name");
                                        Log.d(TAG, name);
                                    }
                                    if (!object.isNull("link")) {
                                        link = object.getString("link");
                                        Log.d(TAG, link);
                                    }
                                    if (!object.isNull("email")) {
                                        email = object.getString("email");
                                        Log.d(TAG, email);
                                    }
                                    if (!object.isNull("birthday")) {
                                        birthday = object.getString("birthday");
                                        Log.d(TAG, birthday);
                                    }
                                    if (!object.isNull("gender")) {
                                        gender = object.getString("gender");
                                        Log.d(TAG, gender);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
*/
                            }
                        });


                Bundle parameters = new Bundle();
                String permissions = "id,first_name,last_name,gender,locale,picture,timezone,updated_time,email";
                parameters.putString("fields", permissions);
                //parameters.putString("fields", "friendlist, members");
                request.setParameters(parameters);
                //request.executeAsync();

                batch.add(request);

                request = GraphRequest.newMyFriendsRequest(
                        AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONArrayCallback() {
                            @Override
                            public void onCompleted(JSONArray jsonArray, GraphResponse response) {
                                // Application code for users friends
                                System.out.println("getFriendsData onCompleted : jsonArray " + jsonArray);
                                System.out.println("getFriendsData onCompleted : response " + response);
                                try {
                                    JSONObject jsonObject = response.getJSONObject();
                                    System.out.println("getFriendsData onCompleted : jsonObject " + jsonObject);
                                    JSONObject summary = jsonObject.getJSONObject("summary");
                                    System.out.println("getFriendsData onCompleted : summary total_count - " + summary.getString("total_count"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                batch.add(request);

                batch.addCallback(new GraphRequestBatch.Callback() {
                    @Override
                    public void onBatchCompleted(GraphRequestBatch graphRequests) {
                        // Application code for when the batch finishes
                    }
                });

                batch.executeAsync();

                //Bundle parameters = new Bundle();
                //parameters.putString("fields", "id,name,link,picture");




                //07-07 22:28:56.705 8869-8869/com.routeal.cocoger V/FacebookLoginActivity: {Response:  responseCode: 200, graphObject: {"data":[],"summary":{"total_count":71}}, error: null}
                //07-07 22:28:56.811 8869-8869/com.routeal.cocoger V/FacebookLoginActivity: {Response:  responseCode: 200, graphObject: {"id":"10153852460631264","first_name":"Hiroshi","last_name":"Watanabe","locale":"en_US","picture":{"data":{"is_silhouette":true,"url":"https:\/\/scontent.xx.fbcdn.net\/v\/t1.0-1\/c15.0.50.50\/p50x50\/10645251_10150004552801937_4553731092814901385_n.jpg?oh=e4e7a4c15fc8173d149c3b00f3619db9&oe=59D08C10"}},"timezone":-7,"updated_time":"2014-08-12T05:40:38+0000","email":"nabe@live.com"}, error: null}


                /* make the API call */
                /*
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me/friends",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                Log.v(TAG, response.toString());
                            }
                        }
                ).executeAsync();
                */
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
}
