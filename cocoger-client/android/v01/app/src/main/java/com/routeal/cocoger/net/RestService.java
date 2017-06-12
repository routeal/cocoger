package com.routeal.cocoger.net;

import com.routeal.cocoger.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Created by nabe on 6/11/17.
 */

public interface RestService {

    @POST("m/auth/login")
    Call<User> login(@Body User user);
}
