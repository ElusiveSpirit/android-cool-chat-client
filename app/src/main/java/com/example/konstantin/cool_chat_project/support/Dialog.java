package com.example.konstantin.cool_chat_project.support;

import java.util.ArrayList;

/**
 *
 * Created by konstantin on 26.03.16.
 */
public class Dialog {

    private static final int MAX_PREVIEW_LENGTH = 40;

    private long mID;
    private String mName;
    private String mText;

    private long mMessagesCount;

    private ArrayList<Long> mContactsIDs = new ArrayList<>();
    private ArrayList<TextMessage> mMessages = new ArrayList<>();

    public Dialog() {
        mID = -1;
        mName = "No name";
        mMessagesCount = 0;
        setText();
    }

    public Dialog(long id, String name, ArrayList<Long> contactsIDs) {
        mID = id;
        mName = name;
        mContactsIDs = contactsIDs;
        mMessagesCount = 0;
        setText();
    }

    public Dialog(long id, String name, ArrayList<Long> contactsIDs, ArrayList<TextMessage> messages) {
        mID = id;
        mName = name;
        mContactsIDs = contactsIDs;
        mMessages = messages;
        mMessagesCount = messages.size();
        setText();
    }

    public ArrayList<Long> getContactsIDs() {
        return mContactsIDs;
    }

    public void setContactsIDs(ArrayList<Long> ids) {
        if (ids != null) {
            mContactsIDs.clear();
            mContactsIDs.addAll(ids);
        }
    }

    public void addMsgOrSetSend(TextMessage msg) {
        long id = msg.getID();
        for (Message curMsg : mMessages)
            if (curMsg.getID() == id) {
                curMsg.sent();
                return;
            }
        addMessage(msg);
    }

    public void addMessages(ArrayList<TextMessage> messages) {
        mMessages.addAll(messages);
        setText();
    }

    public void addMessage(TextMessage message) {
        mMessages.add(message);
        setText();
    }

    private void setText() {
        if (mMessages.size() == 0) {
            mText = "No messages...";
        } else {
            String temp = mMessages.get(mMessages.size() - 1).getText();

            if (temp.length() > MAX_PREVIEW_LENGTH)
                mText = temp.substring(0, MAX_PREVIEW_LENGTH);
            else
                mText = temp;
        }
    }


    public ArrayList<TextMessage> getMessages() {
        return mMessages;
    }


    public String getTime() {
        if (mMessages.size() > 0)
            return mMessages.get(mMessages.size() - 1).getStringTime();
        else
            return "";
    }

    public void setMessagesCount(long count) {
        mMessagesCount = count;
    }

    public void setID(long id) {
        mID = id;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public long getCurrentMessagesCount() {
        return mMessages.size();
    }

    public long getMessagesCount() {
        return mMessagesCount;
    }

    public long getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public String getText() {
        return mText;
    }

}
