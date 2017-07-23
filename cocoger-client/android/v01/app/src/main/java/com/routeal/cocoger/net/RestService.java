package com.routeal.cocoger.net;

import com.routeal.cocoger.model.Device;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by nabe on 6/11/17.
 */

public interface RestService {

    @POST("/auth/login")
    Call<User> login(@Header("Authorization") String token, @Body User user);

    @POST("/auth/logout")
    Call<Void> logout(@Header("Authorization") String token, @Body Device device);

    @POST("/locations")
    Call<Void> setLocations(@Header("Authorization") String token, @Body List<LocationAddress> locations);

    @GET("/locations")
    Call<List<LocationAddress>> getLocations(@Header("Authorization") String token, @Body Device device);
}
