package com.thenetwork.app.android.thenetwork.NotificationUtils;

/**
 * Created by Kashish on 04-06-2018.
 */

public class Message {
    String to;
    NotifyData notification;

    public Message(String to, NotifyData notification) {
        this.to = to;
        this.notification = notification;
    }

}
