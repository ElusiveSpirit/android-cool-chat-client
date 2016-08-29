package com.example.konstantin.cool_chat_project.support;

import android.util.Log;
import com.example.konstantin.cool_chat_project.SocketAdapter;
import com.example.konstantin.cool_chat_project.iMessageSender;

import java.io.IOException;

/**
 * Created by konstantin on 22.03.16.
 *
 */
public class TextMessage extends Message implements iMessageSender {

    private String mTheme;
    private String mText;

    public TextMessage() {}

    public TextMessage(long id, long userID, long roomID, String theme, String text, long time) {
        super(id, userID, roomID, time);
        this.mTheme = theme;
        this.mText = text;
    }

    @Override
    public void send(SocketAdapter socket) throws IOException {
        socket.sendFlag(SocketAdapter.TEXT_MESSAGE_SEND);
        socket.sendLong(getID());
        socket.sendLong(getUserID());
        socket.sendLong(getRoomID());
        socket.sendString(getText());
        socket.sendLong(getTime());
        socket.sendString(getTheme());
    }


    public void receive(SocketAdapter socket) throws IOException {

        long id = socket.receiveLong();
        long userID = socket.receiveLong();
        long roomID = socket.receiveLong();
        String text = socket.receiveString();
        long time = socket.receiveLong();
        String theme = socket.receiveString();

        this.setID(id);
        this.setUserID(userID);
        this.setRoomID(roomID);
        this.setTime(time);
        this.setText(text);
        this.setTheme(theme);
    }

    public String getTheme() {
        return mTheme;
    }

    public String getText() {
        return mText;
    }

    @Override
    public String toString() {
        return getUserID() + " -> " + mText + " : " + getTime();
    }

    protected void setTheme(String theme) {
        this.mTheme = theme;
    }

    protected void setText(String text) {
        this.mText = text;
    }

}
