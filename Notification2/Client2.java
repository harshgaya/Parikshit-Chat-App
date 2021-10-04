package com.parikshit.parikshitchat.Notification2;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Client2 {

    private static Retrofit retrofit=null;

    public static  Retrofit getRetrofit(String url){
        if (retrofit==null){
            retrofit=new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
