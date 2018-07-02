package com.thenetwork.app.android.thenetwork.HelperClasses;

public class Conv {

    public boolean seen;
    public long timestamp;
    public String userId;



    public String name;

    public Conv(boolean seen, long timestamp, String userId, String name) {
        this.seen = seen;
        this.timestamp = timestamp;
        this.name = name;
    }

    public Conv(){

    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
