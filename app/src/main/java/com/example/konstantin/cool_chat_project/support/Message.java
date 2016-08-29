package com.example.konstantin.cool_chat_project.support;

import android.text.format.DateFormat;
import com.example.konstantin.cool_chat_project.SocketAdapter;
import com.example.konstantin.cool_chat_project.iMessageSender;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by konstantin on 22.03.16.
 */
public class Message implements iMessageSender {

    private long mID;
    private long mUserID;
    private long mRoomID;

    private long mTime;

    private boolean mIsSent = false;

    public Message() {
        mID = -1;
        mUserID = -1;
        mRoomID = -1;
        mTime = -1;
    }

    public Message(long id, long userID, long roomID, long time) {
        this.mID = id;
        this.mUserID = userID;
        this.mRoomID = roomID;
        this.mTime = time;
    }

    public void send(SocketAdapter socket) throws IOException {
        socket.sendLong(mUserID);
        socket.sendLong(mRoomID);
        socket.sendLong(mTime);
    }

    public void receive(SocketAdapter socket) throws IOException {
        long userID = socket.receiveLong();
        long roomID = socket.receiveLong();
        long time = socket.receiveLong();

        this.mUserID = userID;
        this.mRoomID = roomID;
        this.mTime = time;
    }

    public String getStringTime() {
        Calendar cal = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            cal = Calendar.getInstance(Locale.forLanguageTag("RUS"));
            cal.setTimeInMillis(mTime);
        }
        return DateFormat.format("HH:mm", cal).toString();
    }

    public long getID() {
        return mID;
    }

    public long getRoomID() {
        return mRoomID;
    }

    public long getUserID() {
        return mUserID;
    }

    public long getTime() {
        return mTime;
    }

    public boolean getIsSent() {
        return mIsSent;
    }

    public void sent() {
        mIsSent = true;
    }

    @Override
    public String toString() {
        return mUserID + " : " + mTime;
    }

    protected void setID(long id) {
        this.mID = id;
    }

    protected void setUserID(long userID) {
        this.mUserID = userID;
    }

    protected void setRoomID(long roomID) {
        this.mRoomID = roomID;
    }

    protected void setTime(long time) {
        this.mTime = time;
    }
}
