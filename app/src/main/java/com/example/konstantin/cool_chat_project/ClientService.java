package com.example.konstantin.cool_chat_project;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;
import com.example.konstantin.cool_chat_project.support.*;

import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

public class ClientService extends Service {

    /** Command to the service to display a message */
    public static final int MSG_CONNECT_TO_SERVICE = 1;
    public static final int MSG_DISCONNECT_FROM_SERVICE = 2;
    public static final int MSG_IS_CONNECTED = 5;
    public static final int MSG_DISCONNECT_FROM_SERVER = 6;
    public static final int MSG_REGISTER = 7;
    public static final int MSG_AUTHORIZE = 8;
    public static final int MSG_SEND_TEXT_MESSAGE = 9;
    public static final int MSG_CREATE_DIALOG = 11;

    private String mAddress;
    private short mServerPort;

    private long mID = -1;
    private String mLogin = null;
    private String mPassword = null;

    private SocketAdapter mSocketAdapter;

    private ServerHandling mServerHandling;
    private Thread mReceivingThread;
    private ServerSending mServerSending;
    private Thread mSendingThread;

    /** Messenger for communicating with the service. */
    private Messenger mClientApp = null;

    @Override
    public void onCreate() {
        super.onCreate();
        //mAddress = "192.168.1.45";
        mAddress = "172.16.48.202";
        mServerPort = 8080;
        mSocketAdapter = new SocketAdapter(mAddress, mServerPort);

        mServerSending = new ServerSending();
        mSendingThread = new Thread(mServerSending);

        mServerHandling = new ServerHandling();
        mReceivingThread = new Thread(mServerHandling);
    }

    /**
     * Handler of incoming messages from clients.
     */
    @SuppressLint("HandlerLeak")
    private class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONNECT_TO_SERVICE:
                    mClientApp = msg.replyTo;
                    try {
                        if (mID != -1)
                            mClientApp.send(Message.obtain(null, Client.MSG_GET_ID, mID));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_DISCONNECT_FROM_SERVICE:
                    mClientApp = null;
                    break;
                default:
                    mServerSending.addToQueue(msg);
                    //super.handleMessage(msg);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();
        Log.e("Service", "onStartCommand");

        // TODO изменить на STICKY
        return START_NOT_STICKY;
    }


    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "onBind", Toast.LENGTH_SHORT).show();
        Log.e("Service", "onBind");

        if (!mSendingThread.isAlive())
            mSendingThread.start();

        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("Service", "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e("Service", "onDestroy");
        Toast.makeText(getApplicationContext(), "onDestroy", Toast.LENGTH_SHORT).show();

        if (mSocketAdapter != null)
            mSocketAdapter.disconnect();
        stopThreads();
    }

    private void sendAnswer(boolean bool) {
        try {
            if (mClientApp != null) {
                mClientApp.send(Message.obtain(null, Client.MSG_CONNECTION_ANSWER, -1, 0, bool));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendObjectMessage(int what, Object object) {
        try {
            if (mClientApp != null) {
                mClientApp.send(Message.obtain(null, what, object));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void stopThreads() {
        isCanceled = true;
        mReceivingThread.interrupt();
        mSendingThread.interrupt();
    }

    private boolean isCanceled = false;
    private final Object connectionMutex = new Object();

    private class ServerSending implements Runnable {

        private final PriorityBlockingQueue<QueueMessage> mQueue = new PriorityBlockingQueue<>();

        void addToQueue(Message msg) {
                Message msgCopy = new Message();
                // Чёрная магия требует копирования объекта
                msgCopy.copyFrom(msg);
                QueueMessage queueMessage = new QueueMessage(msgCopy);

            synchronized (mQueue) {
                if (!mQueue.offer(queueMessage)) {
                    Log.e("SENDING", "Error in adding to queue");
                }
                mQueue.notifyAll();
                Log.i("INFO", "Added msg to queue. msg.what = " + msg.what);
            }
        }

        @Override
        public void run() {
            Message msg;

            while (true) {
                msg = Message.obtain(null, SocketAdapter.STILL_ALIVE);
                synchronized (mQueue) {
                    try {
                        mQueue.wait(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        if (isCanceled) return;
                    }

                    if (!mQueue.isEmpty()) {
                        try {
                            msg = mQueue.poll().getMessage();
                            if (msg == null)
                                msg = Message.obtain(null, SocketAdapter.STILL_ALIVE);
                        } catch (ClassCastException e) {
                            Log.e("Debug", "Exception in polling from mQueue" + e.toString());
                        }
                    }

                }

                while (!mSocketAdapter.isConnect()) {
                    mSocketAdapter.setDisconnect();
                    try {
                        mSocketAdapter.connect();
                    } catch (IOException e) {
                        Log.w("Connection", "Error in connection. Trying to reconnect.");
                        //e.printStackTrace();
                        sendAnswer(false);
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                            if (isCanceled) return;
                        }
                    }
                }
                while (!mSocketAdapter.isAuthorize() &&
                        mLogin != null &&
                        mPassword != null) {
                    Log.i("INFO", "ReAuth");
                    waitForAuth(mSocketAdapter.authorizeIfNot(mLogin, mPassword));
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                        if (isCanceled) return;
                    }
                }
                Log.i("INFO", "msg.what = " + msg.what);

                switch (msg.what) {

                    case MSG_IS_CONNECTED:
                        sendAnswer(mSocketAdapter.isConnect());
                        break;
                    case MSG_DISCONNECT_FROM_SERVER:
                        Log.e("Debug", "Disconnect in service id = " + msg.obj);
                        mSocketAdapter.disconnect();
                        mLogin = null;
                        mPassword = null;
                        mID = -1;
                        // TODO остановка сервиса
                        stopSelf();
                        break;
                    case MSG_AUTHORIZE:
                        Contact contact = (Contact) msg.obj;
                        if (!mSocketAdapter.isAuthorize()) {
                            mLogin = contact.getName();
                            mPassword = contact.getPassword();

                            waitForAuth(mSocketAdapter.authorizeIfNot(contact.getName(), contact.getPassword()));
                        }
                        break;
                    case MSG_REGISTER:
                        // Регистрация
                        if (!mSocketAdapter.isAuthorize()) {
                            Log.i("INFO", "Registration");
                            contact = (Contact) msg.obj;
                            mLogin = contact.getName();
                            mPassword = contact.getPassword();

                            waitForAuth(mSocketAdapter.register(contact.getName(), contact.getPassword()));
                        }
                        break;
                    case SocketAdapter.STILL_ALIVE:
                        if (mSocketAdapter.isAuthorize()) {
                            mSocketAdapter.stillAlive();
                            synchronized (connectionMutex) {
                                connectionMutex.notifyAll();
                            }
                        }
                        break;
                    case MSG_SEND_TEXT_MESSAGE:
                        TextMessage message = (TextMessage) msg.obj;

                        try {
                            mSocketAdapter.sendMessage(message);
                            Log.i("INFO", "Send message: Success");
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i("INFO", "Send message: Fail");
                        }
                        break;
                    case MSG_CREATE_DIALOG:
                        Dialog dialog = (Dialog) msg.obj;

                        if (mSocketAdapter.addDialog(dialog.getName(), dialog.getContactsIDs()))
                            Log.i("INFO", "Create Dialog: Success");
                        else
                            Log.i("INFO", "Create Dialog: Fail");
                        break;
                    case SocketAdapter.GET_USER_ID_BY_LOGIN:
                        mSocketAdapter.askForUserID((String) msg.obj);
                        break;
                    case SocketAdapter.GET_COUNT_OF_MESSAGES_IN_DIALOG:
                        mSocketAdapter.askForMessagesCountInDialog((long) msg.obj);
                        break;
                    case SocketAdapter.GET_USER_DIALOGS:
                        mSocketAdapter.askForAllDialogs();
                        break;
                    case SocketAdapter.GET_DIALOG_NAME_BY_ID:
                        mSocketAdapter.askForDialogByID((long) msg.obj);
                        break;
                    case SocketAdapter.GET_USER_LOGIN_BY_ID:
                        mSocketAdapter.askForUserLoginByID((long) msg.obj);
                        break;
                    case SocketAdapter.GET_MESSAGE_BY_ID_AND_DIALOG_ID:
                        com.example.konstantin.cool_chat_project.support.Message messageTemp =
                                (com.example.konstantin.cool_chat_project.support.Message) msg.obj;

                        mSocketAdapter.askForMessageByID(messageTemp.getRoomID(), messageTemp.getID());
                        break;
                    case SocketAdapter.GET_USERS_IN_DIALOG:
                        mSocketAdapter.askForUsersInDialog((long) msg.obj);
                        break;
                }
            }
        }

        private void waitForAuth(boolean answer) {
            long id = -1;
            sendLoadingStatus("Waiting for answer", 30);
            if (answer) {
                try {
                    String string = mSocketAdapter.receiveString();
                    sendLoadingStatus("Waiting for id", 40);
                    id = mSocketAdapter.receiveLong();
                    sendLoadingStatus("Authorizing complete", 60);
                    if (string.equals("success")) {
                        mSocketAdapter.setAuth();
                        synchronized (connectionMutex) {
                            connectionMutex.notifyAll();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sendLoadingStatus("Starting receiving thread", 70);
                // TODO Запуск приёма сообщений
                if (!mReceivingThread.isAlive()) {
                    mReceivingThread.start();
                }
                mID = id;
                try {
                    if (mClientApp != null) {
                        mClientApp.send(Message.obtain(null, Client.MSG_AUTH_ANSWER, id));
                    } else {
                        Log.e("Debug", "mClientApp is null");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendLoadingStatus(String text, int progress) {
            try {
                if (mClientApp != null) {
                    mClientApp.send(Message.obtain(null, Client.MSG_LOADING_STATUS, progress, 0, text));
                } else {
                    Log.e("Debug", "mClientApp is null");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private class ServerHandling implements Runnable {

        private final int SERVER_FLAG_TEXT_MESSAGE = 0;
        private final int YOU_ARE_ADDED_TO_DIALOG = 5;

        @Override
        public void run() {
            while (true) {
                // Пока нет соединения или авторизации, то будет ожидаться сигнал
                // об успешной авторизации
                if (!mSocketAdapter.isConnect() || !mSocketAdapter.isAuthorize()) {
                    synchronized (connectionMutex) {
                        try {
                            connectionMutex.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            if (isCanceled) return;
                        }
                    }
                }
                Log.i("Info", "Waiting for input flag");
                try {
                    android.os.Message msg = Message.obtain(null, mSocketAdapter.receiveFlag());
                    Log.i("Info", "Flag = " + msg.what);
                    switch (msg.what) {

                        case SERVER_FLAG_TEXT_MESSAGE:
                        case SocketAdapter.GET_MESSAGE_BY_ID_AND_DIALOG_ID:
                            // Приём текствого сообщения
                            com.example.konstantin.cool_chat_project.support.TextMessage message =
                                    new com.example.konstantin.cool_chat_project.support.TextMessage();
                            mSocketAdapter.receiveMessage(message);

                            msg.what = Client.MSG_RECEIVE_TEXT_MESSAGE;
                            msg.obj = message;

                            break;
                        case YOU_ARE_ADDED_TO_DIALOG:
                            msg.obj = mSocketAdapter.receiveLong();
                            break;
                        case SocketAdapter.GET_USER_LOGIN_BY_ID:
                            msg.obj = mSocketAdapter.receiveUserLogin();
                            break;
                        case SocketAdapter.GET_USER_ID_BY_LOGIN:
                            msg.obj = mSocketAdapter.receiveUserID();
                            break;
                        case SocketAdapter.GET_DIALOG_NAME_BY_ID:
                            msg.obj = mSocketAdapter.receiveDialogByID();
                            break;
                        case SocketAdapter.GET_USER_DIALOGS:
                            msg.obj = mSocketAdapter.receiveDialogsIDs();
                            break;
                        case SocketAdapter.GET_USERS_IN_DIALOG:
                            msg.obj = mSocketAdapter.receiveUsersIDsInDialog();
                            break;
                        case SocketAdapter.GET_COUNT_OF_MESSAGES_IN_DIALOG:
                            msg.obj = mSocketAdapter.receiveMessagesCountInDialog();
                            break;
                    }
                    sendMsg(msg);
                } catch (IOException e) {
                    mSocketAdapter.setDisconnect();
                    e.printStackTrace();
                    Log.e("SERVICE_ERROR", "ConnectionError in receiving");
                    if (isCanceled) return;
                }
            }
        }

        private void sendMsg(Message msg) {
            if (mClientApp != null) {
                try {
                    mClientApp.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("Debug", "Cant send message. mClientApp is null");
            }

        }

    }


    private class QueueMessage implements Comparable<QueueMessage> {

        private Message message;

        QueueMessage(Message message) {
            this.message = message;
        }

        Message getMessage() {
            return message;
        }

        @Override
        public int compareTo(@NonNull QueueMessage another) {
            if (another.getMessage().getWhen() > this.message.getWhen())
                return 1;
            else if (another.getMessage().getWhen() < this.message.getWhen())
                return -1;

            return 0;
        }
    }

}
