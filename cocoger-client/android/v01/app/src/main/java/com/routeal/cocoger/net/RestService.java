package com.routeal.cocoger.net;

import com.routeal.cocoger.model.Device;
import com.routeal.cocoger.model.Test;
import com.routeal.cocoger.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Created by nabe on 6/11/17.
 */

public interface RestService {

    @GET("/test")
    Call<Test> test();

    @GET("/users/me")
    Call<User> me(@Header("Authorization") String token);

    @GET("/auth/logout")
    Call<User> logout(@Header("Authorization") String token);

    @POST("/auth/login")
    Call<User> login(@Body User user);

    @POST("/auth/signup")
    Call<User> signup(@Body User user);

    @POST("/auth/facebook/login")
    Call<User> login(@Header("Authorization") String token, @Body Device device);
}
