package com.example.konstantin.cool_chat_project;

import android.util.Log;
import com.example.konstantin.cool_chat_project.support.Contact;
import com.example.konstantin.cool_chat_project.support.Dialog;
import com.example.konstantin.cool_chat_project.support.Message;
import com.example.konstantin.cool_chat_project.support.TextMessage;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by konstantin on 22.03.16.
 *
 * SocketAdapter class to make isConnect with C++ server
 */
public class SocketAdapter {

    private TextMessage textMessage;

    private boolean mIsAuthorize = false;
    private boolean mIsConnect = false;

    public static final byte TEXT_MESSAGE_SEND = 0;
    public static final byte DISCONNECT = 10;
    public static final byte CREATE_DIALOG = 11;
    public static final byte ADD_USER_TO_DIALOG = 12;
    public static final byte STILL_ALIVE = 19;
    public static final byte STOP_SERVER = 20;
    public static final byte GET_USER_LOGIN_BY_ID = 30;
    public static final byte GET_USER_ID_BY_LOGIN = 31;
    public static final byte GET_DIALOG_NAME_BY_ID = 32;
    public static final byte GET_USER_DIALOGS = 33;
    public static final byte GET_USERS_IN_DIALOG = 34;
    public static final byte GET_COUNT_OF_MESSAGES_IN_DIALOG = 35;
    public static final byte GET_MESSAGE_BY_ID_AND_DIALOG_ID = 36;

    private InetAddress ipAddress;
    private short serverPort;

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public SocketAdapter(String address, short port) {
        this.serverPort = port;
        try {
            this.ipAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            System.out.println("Can't find the host. Error: " + e);
        }
    }

    public boolean isConnect() {
        return mIsConnect;
    }

    public boolean isAuthorize() {
        return mIsAuthorize;
    }

    public void connect() throws IOException {
        socket = new Socket(ipAddress, serverPort);

        mIsConnect = true;

        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public boolean register(String login, String password) {
        if (!isConnect()) return false;
        try {
            this.sendString("register");
            this.sendString(login);
            this.sendString(password);

            mIsAuthorize = true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void setDisconnect() {
        mIsAuthorize = false;
        mIsConnect = false;
    }

    public boolean disconnect() {
        if (!isConnect()) return false;
        try {
            sendFlag(DISCONNECT);
            mIsAuthorize = false;
            mIsConnect = false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean authorizeIfNot(String login, String password) {
        if (!isConnect()) return false;
        if (mIsAuthorize) return true;
        try {
            this.sendString("login");
            this.sendString(login);
            this.sendString(password);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean stillAlive() {
        if (!isConnect()) return false;
        try {
            sendFlag(STILL_ALIVE);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void setAuth() {
        mIsAuthorize = true;
    }

    // Изменить возвращаемое значение на id
    public boolean addDialog(String name, ArrayList<Long> IDs) {
        if (!isConnect()) return false;
        try {
            Log.e("Debug", "Sending dialog info start");
            this.sendFlag(CREATE_DIALOG);
            this.sendString(name);
            this.sendLong((long) IDs.size());
            for (long id : IDs) {
                this.sendLong(id);

            }
            Log.e("Debug", "Sending dialog info end");

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean addUserToDialog(long dialogID, long userID) {
        if (!isConnect()) return false;
        try {
            this.sendFlag(ADD_USER_TO_DIALOG);
            this.sendLong(dialogID);
            this.sendLong(userID);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean askForUserLoginByID(long id) {
        if (!isConnect()) return false;
        try {
            this.sendFlag(GET_USER_LOGIN_BY_ID);
            this.sendLong(id);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Contact receiveUserLogin() {
        Contact contact = new Contact(-1, "");
        try {
            contact.setID(this.receiveLong());
            contact.setName(this.receiveString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contact;
    }

    public boolean askForUserID(String login) {
        try {
            this.sendFlag(GET_USER_ID_BY_LOGIN);
            this.sendString(login);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public Contact receiveUserID() {
        Contact contact = new Contact(-1, "");
        try {
            contact.setName(this.receiveString());
            contact.setID(this.receiveLong());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contact;
    }

    public boolean askForDialogByID(long id) {
        try {
            this.sendFlag(GET_DIALOG_NAME_BY_ID);
            this.sendLong(id);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Dialog receiveDialogByID() {
        Dialog dialog = new Dialog();

        try {
            dialog.setID(this.receiveLong());
            dialog.setName(this.receiveString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dialog;
    }

    public boolean askForAllDialogs() {
        try {
            this.sendFlag(GET_USER_DIALOGS);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public ArrayList<Long> receiveDialogsIDs() {
        ArrayList<Long> dialogs = new ArrayList<>();

        try {
            this.receiveLong();
            long size = receiveLong();

            for (long i = 0; i < size; ++i)
                dialogs.add(this.receiveLong());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return dialogs;
    }

    public boolean askForUsersInDialog(long id) {
        try {
            sendFlag(GET_USERS_IN_DIALOG);
            sendLong(id);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // First element in ArrayList = dialogID
    public ArrayList<Long> receiveUsersIDsInDialog() {
        ArrayList<Long> users = new ArrayList<>();

        try {
            users.add(receiveLong());
            long size = receiveLong();
            for (long i = 0; i < size; ++i)
                users.add(receiveLong());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return users;
    }

    public boolean askForMessagesCountInDialog(long id) {
        try {
            sendFlag(GET_COUNT_OF_MESSAGES_IN_DIALOG);
            sendLong(id);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Dialog receiveMessagesCountInDialog() {
        Dialog dialog = new Dialog();

        try {
            dialog.setID(receiveLong());
            dialog.setMessagesCount(receiveLong());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dialog;
    }

    public boolean askForMessageByID(long dialogID, long messageID) {
        try {
            sendFlag(GET_MESSAGE_BY_ID_AND_DIALOG_ID);
            sendLong(dialogID);
            sendLong(messageID);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    /*
    public TextMessage receiveMessage() {
        TextMessage message = new TextMessage();

        try {
            this.receiveMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return message;
    }
    */

    public void sendMessage(iMessageSender message) throws IOException {
        if (message == null) return;
        message.send(this);
    }


    public void receiveMessage(iMessageSender message) throws IOException{

        message.receive(this);
    }

    public void sendInt(int value) throws IOException {
        outputStream.writeInt(value);
    }

    public void sendLong(long value) throws IOException {
        sendInt((int) value);
        sendInt((int) (value >> 32));
        //outputStream.writeLong(value);
    }

    public void sendFlag(byte flag) throws IOException {
        outputStream.writeByte(flag);
    }

    public void sendStringMessage(String string) throws IOException {
        outputStream.writeByte(0);
        sendString(string);
    }

    public void sendString(String string) throws IOException {
        if (string == null) {
            Log.e("Debug", "Null string");
            return;
        }

        this.sendInt(string.length());
        outputStream.writeBytes(string);
    }

    public int receiveFlag() throws IOException {
        return inputStream.readByte();
    }

    public String receiveString() throws IOException {
        int length = inputStream.readInt();
        byte input[] = new byte[length];
        inputStream.readFully(input);

        return new String(input);
    }

    public long receiveLong() throws IOException  {
        return inputStream.readInt() + ((long) inputStream.readInt() << 32);
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
