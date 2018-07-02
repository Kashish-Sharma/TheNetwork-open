package com.thenetwork.app.android.thenetwork.HelperClasses;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

/**
 * Created by Kashish on 14-06-2018.
 */

public class EventId {

    @Exclude
    public String EventId;

    public  <T extends  EventId> T withId(@NonNull final String id){
        this.EventId = id;
        return (T) this;
    }

}
