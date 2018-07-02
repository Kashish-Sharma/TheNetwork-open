package com.thenetwork.app.android.thenetwork.HelperClasses;

import com.thenetwork.app.android.thenetwork.NotificationUtils.Message;

public class Messages extends MessagesId {

    private String message;
    private int type;
    private String from;
    private long time;
    private Boolean seen;
    private String image;
    private String thumb;
    private String path;
    private String filename;
    private String imageText;


    public Messages(String message, int type, long time,
                    Boolean seen, String from, String image,
                    String thumb, String path, String filename, String imageText) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.seen = seen;
        this.from = from;
        this.image = image;
        this.thumb = thumb;
        this.path = path;
        this.filename = filename;
        this.imageText = imageText;
    }

    public Messages(){

    }

    public String getImageText() {
        return imageText;
    }

    public void setImageText(String imageText) {
        this.imageText = imageText;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getpath() {
        return path;
    }

    public void setpath(String path) {
        this.path = path;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public long getTime() {
        return time;
    }

    public Boolean getSeen() {
        return seen;
    }
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setSeen(Boolean  seen) {
        this.seen = seen;
    }
}

