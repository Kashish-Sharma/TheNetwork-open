package com.thenetwork.app.android.thenetwork.HelperClasses;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

/**
 * Created by Kashish on 19-06-2018.
 */

public class RequestItemUserId {

    @Exclude
    public String requestItemUserId;

    public  <T extends  RequestItemUserId> T withId(@NonNull final String id){
        this.requestItemUserId = id;
        return (T) this;
    }

}
