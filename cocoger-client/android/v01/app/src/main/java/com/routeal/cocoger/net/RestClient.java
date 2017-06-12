package com.routeal.cocoger.net;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by nabe on 6/12/17.
 */

public class RestClient {
    private RestService service;

    public RestClient(String serverUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(RestService.class);
    }

    public RestService getService() {
        return service;
    }
}
