package com.thenetwork.app.android.thenetwork.HelperClasses;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

/**
 * Created by Kashish on 19-06-2018.
 */

public class MessagesId {

    @Exclude
    public String MessageId;

    public  <T extends  MessagesId> T withId(@NonNull final String id){
        this.MessageId = id;
        return (T) this;
    }

}
