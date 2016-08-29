package com.example.konstantin.cool_chat_project.support;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 *
 * Created by konstantin on 28.03.16.
 */
public class Contact implements Serializable {

    private long mID;
    private String mName;
    private Drawable mAvatar;
    private String mPassword;

    private boolean mIsExist = true;

    public Contact(long id, String name) {
        mID = id;
        mName = name;
    }

    public Contact(String name, String password) {
        mName = name;
        mPassword = password;
    }

    public void setExist() {
        this.mIsExist = true;
    }

    public void setAvatar(Drawable image) {
        mAvatar = image;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public void setID(long mID) {
        this.mID = mID;
    }

    public Drawable getAvatar() {
        return mAvatar;
    }

    public String getName() {
        return mName;
    }

    public String getPassword() {
        return mPassword;
    }

    public long getID() {
        return mID;
    }

    public boolean isExist() {
        return mIsExist;
    }

    private void resizeAvatar() {
        // TODO Изенять размер аватарки
    }
}
