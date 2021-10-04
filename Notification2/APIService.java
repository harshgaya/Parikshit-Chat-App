package com.parikshit.parikshitchat.Notification2;

import com.parikshit.parikshitchat.notifications.Response;
import com.parikshit.parikshitchat.notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAg85zcyg:APA91bE7Oyztl349hi_a3koesSddSjx5ME8ZUGxYVRaUBiQGQXxb81yawDkIfipv2O1FGOK2Xs5l-sDBpZrlgbXV87c7CxToWsT8uD3bH-hVw5hvDxi7NcOtdu-vmiJsJwfzjimBr3n3"

    })
    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);

}
//AAAAg85zcyg:APA91bE7Oyztl349hi_a3koesSddSjx5ME8ZUGxYVRaUBiQGQXxb81yawDkIfipv2O1FGOK2Xs5l-sDBpZrlgbXV87c7CxToWsT8uD3bH-hVw5hvDxi7NcOtdu-vmiJsJwfzjimBr3n3