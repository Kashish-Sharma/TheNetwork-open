package com.thenetwork.app.android.thenetwork.NotificationUtils;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Kashish on 04-06-2018.
 */

public interface FirebaseAPI {

    @POST("/fcm/send")
    Call<Message> sendMessage(@Body Message message);

}