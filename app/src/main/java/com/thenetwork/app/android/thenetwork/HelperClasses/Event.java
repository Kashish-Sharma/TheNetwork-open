package com.thenetwork.app.android.thenetwork.HelperClasses;

import java.util.List;
import java.util.Map;

/**
 * Created by Kashish on 14-06-2018.
 */

public class Event extends EventId{

    private String image_url;
    private String thumb_url;
    private String title;
    private long timestamp;
    private String user_id;
    private String image_name;
    private String desc;

    private Map<String, String> subEvents;

    private String eventPlaceId;
    private String eventDateTo;
    private String eventDateFrom;
    private String eventContact;
    private Boolean isSingle;

    public Event(){

    }


    public Event(String image_url, String thumb_url, String title,
                 long timestamp, String user_id, String image_name,
                 String desc, String eventDateTo,
                 String eventDateFrom, String eventContact, Boolean isSingle,
                 String eventPlaceId, Map<String, String> subEvents) {
        this.image_url = image_url;
        this.thumb_url = thumb_url;
        this.title = title;
        this.timestamp = timestamp;
        this.user_id = user_id;
        this.image_name = image_name;
        this.desc = desc;
        this.eventDateTo = eventDateTo;
        this.eventDateFrom = eventDateFrom;
        this.eventContact = eventContact;
        this.isSingle = isSingle;
        this.eventPlaceId = eventPlaceId;
        this.subEvents = subEvents;
    }

    public Map<String, String> getSubEvents() {
        return subEvents;
    }

    public void setSubEvents(Map<String, String> subEvents) {
        this.subEvents = subEvents;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_name() {
        return image_name;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getEventDateTo() {
        return eventDateTo;
    }

    public void setEventDateTo(String eventDateTo) {
        this.eventDateTo = eventDateTo;
    }

    public String getEventDateFrom() {
        return eventDateFrom;
    }

    public void setEventDateFrom(String eventDateFrom) {
        this.eventDateFrom = eventDateFrom;
    }

    public String getEventContact() {
        return eventContact;
    }

    public void setEventContact(String eventContact) {
        this.eventContact = eventContact;
    }

    public Boolean getIsSingle() {
        return isSingle;
    }

    public void setIsSingle(Boolean single) {
        isSingle = single;
    }

    public String getEventPlaceId() {
        return eventPlaceId;
    }

    public void setEventPlaceId(String eventPlaceId) {
        this.eventPlaceId = eventPlaceId;
    }

}
