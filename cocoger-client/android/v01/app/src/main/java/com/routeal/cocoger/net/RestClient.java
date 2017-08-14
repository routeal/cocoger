package com.routeal.cocoger.net;

import com.routeal.cocoger.MainApplication;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by nabe on 6/12/17.
 */

public class RestClient {
    private RestService service;

    public RestClient(String serverUrl, boolean enable_server_debug) {
        if (enable_server_debug) {
            // For logging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(serverUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
            service = retrofit.create(RestService.class);
        } else {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(serverUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            service = retrofit.create(RestService.class);
        }
    }

    public static RestService service() {
        return MainApplication.getRestClient().service;
    }
}