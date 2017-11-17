package com.dcalabrese22.dan.chatter.Objects;

/**
 * Created by dcalabrese on 11/3/2017.
 */

public class User {

    private String userName;
    private String name;
    private String email;
    private String age;
    private String gender;
    private Boolean hasUserImage;
    private String imageUrl;

    public User() {}

    public User(String userName, String email, String age, String gender, Boolean hasUserImage) {
        this.userName = userName;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.hasUserImage = hasUserImage;
    }

    public User(String userName, String name, String email, String age, String gender, Boolean hasUserImage, String imageUrl) {
        this.userName = userName;
        this.name = name;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.hasUserImage = hasUserImage;
        this.imageUrl = imageUrl;
    }

    public Boolean getHasUserImage() {
        return hasUserImage;
    }

    public void setHasUserImage(Boolean hasUserImage) {
        this.hasUserImage = hasUserImage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
