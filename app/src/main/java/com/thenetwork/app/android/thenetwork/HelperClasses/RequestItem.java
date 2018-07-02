package com.thenetwork.app.android.thenetwork.HelperClasses;

/**
 * Created by Kashish on 19-06-2018.
 */

public class RequestItem extends RequestItemUserId{
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public RequestItem(long timestamp, int type, String name) {
        this.timestamp = timestamp;
        this.type = type;
        this.name = name;
    }

    public RequestItem() {

    }

    public long timestamp;
    public int type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String name;

}
