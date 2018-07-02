package com.thenetwork.app.android.thenetwork.HelperClasses;

/**
 * Created by Kashish on 11-03-2018.
 */

public class Users {

    private String about;
    private String image;
    private String name;
    private String phone;
    private String skills;
    private String user_id;
    private String email;

    public Users(){

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public Users(String about, String image, String name, String phone, String skills, String user_id, String email) {
        this.about = about;
        this.image = image;
        this.name = name;
        this.phone = phone;
        this.skills = skills;
        this.user_id = user_id;
        this.email = email;
    }



}
