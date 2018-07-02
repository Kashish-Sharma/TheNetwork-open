package com.thenetwork.app.android.thenetwork.HelperClasses;

/**
 * Created by Kashish on 05-03-2018.
 */

public class BlogPost extends BlogPostId{

    public String image_url;
    public String thumb_url;
    public String title;
    public long timestamp;
    public String user_id;
    public String image_name;



    public BlogPost(){

    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    public String desc;

    public BlogPost(String desc, String image_url,long timestamp, String thumb_url, String title, String user_id, String image_name) {
        this.desc = desc;
        this.image_url = image_url;
        this.thumb_url = thumb_url;
        this.title = title;
        this.timestamp = timestamp;
        this.user_id = user_id;
        this.image_name = image_name;
    }


}
