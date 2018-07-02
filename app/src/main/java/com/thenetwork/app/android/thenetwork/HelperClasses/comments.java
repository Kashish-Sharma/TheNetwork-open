package com.thenetwork.app.android.thenetwork.HelperClasses;

/**
 * Created by Kashish on 25-05-2018.
 */

public class comments {

    String userId;
    String message;
    long timestamp;

    public comments(String userId, String message, long timestamp){
        this.userId = userId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public comments(){

    }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
