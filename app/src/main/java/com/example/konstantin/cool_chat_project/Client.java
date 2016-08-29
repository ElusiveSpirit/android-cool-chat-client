package com.example.konstantin.cool_chat_project;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.util.Log;
import android.widget.Toast;
import com.example.konstantin.cool_chat_project.support.Contact;
import com.example.konstantin.cool_chat_project.support.Dialog;
import com.example.konstantin.cool_chat_project.support.TextMessage;

import java.util.ArrayList;

/**
 * Client class to work with server
 *
 * Created by konstantin on 26.03.16.
 */
public class Client {

    private Activity activity;
    private Intent mIntent;

    private onClientListener mClientListener;

    private long mID;
    private String mName;
    private String mPassword;

    private ArrayList<Contact> mContacts;
    private ArrayList<Dialog> mDialogs;

    Client(Activity context) {
        mID = -1;
        this.activity = context;
        mIntent = new Intent(context, ClientService.class);

        mDialogs = new ArrayList<>();
        mContacts = new ArrayList<>();

        if (context instanceof onClientListener) {
            mClientListener = (onClientListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement Client.onClientListener");
        }

        doBindService();

     /*   showSnackBar("Test", Snackbar.LENGTH_SHORT);
        ArrayList<Long> ids = new ArrayList<>();
        ids.add(0l);
        ids.add(1l);
        //mDialogs.add(new Dialog(0, "Test", 0));
        mDialogs.add(new Dialog(0, "New", ids));
        Dialog dialog = mDialogs.get(0);
        dialog.addMessage(new TextMessage(0, 1, 0, "Theme", "hello World", System.currentTimeMillis()));


        */
        Contact contact = new Contact(0, "Константин Маняхин");
        contact.setAvatar(context.getDrawable(R.drawable.ic_account_circle_black_24dp));
        mContacts.add(contact);
        contact = new Contact(1, "Иван Руднев");
        contact.setAvatar(context.getDrawable(R.drawable.ic_account_circle_black_24dp));
        mContacts.add(contact);
        contact = new Contact(2, "Игорь Яковлев");
        contact.setAvatar(context.getDrawable(R.drawable.ic_account_circle_black_24dp));
        mContacts.add(contact);
        contact = new Contact(3, "Егор Утробин");
        contact.setAvatar(context.getDrawable(R.drawable.ic_account_circle_black_24dp));
        mContacts.add(contact);
        for (int i = 0; i < 10; ++i) {
            contact = new Contact(mContacts.size(), "Левый тип");
            contact.setAvatar(context.getDrawable(R.drawable.ic_account_circle_black_24dp));
            mContacts.add(contact);
        }
    }

    public void setPersonalData(String login, String password) {
        mName = login;
        mPassword = password;
    }

    public void addContact(String name, Drawable avatar) {
        Contact contact = new Contact(mContacts.size(), name);
        contact.setAvatar(avatar);
        mContacts.add(contact);
    }

    public long getID() {
        return mID;
    }

    public ArrayList<Contact> getContacts() {
        return mContacts;
    }

    public ArrayList<Dialog> getDialogs() {
        return mDialogs;
    }

    public Dialog getDialogByID(long id) {
        for (Dialog dialog : mDialogs) {
            if (dialog.getID() == id)
                return dialog;
        }
        return null;
    }

    public Contact getContactByID(long id) {
        for (Contact contact : mContacts) {
            if (contact.getID() == id)
                return contact;
        }
        return null;
    }


    public void addDialog(Dialog dialog) {
        mDialogs.add(dialog);
        sendObjectMessage(ClientService.MSG_CREATE_DIALOG, dialog);
    }

    public void isConnect() {
        mClientListener.printLogs("Connection request", 10);
        sendObjectMessage(ClientService.MSG_IS_CONNECTED, null);
    }

    private void showToast(final String text) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // *********************************************************************************************
    //      Запросы к сервису
    // *********************************************************************************************

    /**
     * Запрос на регистрацию и последующую авторизацию.
     * mName и mPassword должны быть установлены заранее!!!!
     */
    public void requestRegister() {
        mClientListener.printLogs("Registration request", 10);
        sendObjectMessage(ClientService.MSG_REGISTER, new Contact(mName, mPassword));
    }
    /**
     * Запрос на авторизацию.
     * mName и mPassword должны быть установлены заранее!!!!
     */
    public void requestAuth() {
        sendObjectMessage(ClientService.MSG_AUTHORIZE, new Contact(mName, mPassword));
    }

    /**
     * Запрос на создание диалога.
     * Диалог появится после ответа сервера.
     *
     * @param name Название диалога.
     * @param contactsIDs ArrayList<Long> с id контактов.
     */
    public void requestCreateDialog(String name, ArrayList<Long> contactsIDs) {
        for (int i = 0; i < contactsIDs.size(); ++i) {
            if (contactsIDs.get(i) == mID) {
                contactsIDs.remove(i);
                break;
            }
        }
        Dialog dialog = new Dialog(mDialogs.size(), name, contactsIDs);
        sendObjectMessage(ClientService.MSG_CREATE_DIALOG, dialog);
    }

    /**
     * Запрос на отправку сообщения
     *
     * @param dialogID индекс диалога
     * @param text текст сообщения
     */
    public void requestSendTextMessage(long dialogID, String text) {
        TextMessage message = new TextMessage(0, mID, dialogID, "Theme", text, System.currentTimeMillis());
        sendObjectMessage(ClientService.MSG_SEND_TEXT_MESSAGE, message);
    }

    /**
     * Запрос на проверку существования контактов
     * Недостающие ID контактов будут установлены.
     */
    public void requestCheckContacts() {
        for (Contact contact : mContacts)
            sendObjectMessage(SocketAdapter.GET_USER_ID_BY_LOGIN, contact.getName());
    }

    /**
     * Запрос на все диалоги пользователя
     */
    public void requestAllDialogs() {
        sendObjectMessage(SocketAdapter.GET_USER_DIALOGS, null);
    }

    /**
     * Запрос на обновление сообщений.
     * В результате будут получены недостающие сообщения диалога
     *
     * @param dialogID - id диалога
     */
    public void requestMessagesInDialog(long dialogID) {
        sendObjectMessage(SocketAdapter.GET_COUNT_OF_MESSAGES_IN_DIALOG, dialogID);
    }

    // *********************************************************************************************
    //
    //      Методы работы с сервисом ClientService.
    //      Обработка сообщений от сервиса в классе IncomingHandler
    //
    // *********************************************************************************************

    public static final int MSG_CONNECTION_ANSWER = 1;
    public static final int MSG_GET_ID = 2;
    public static final int MSG_AUTH_ANSWER = 3;
    public static final int MSG_RECEIVE_TEXT_MESSAGE = 4;
    public static final int MSG_LOADING_STATUS = 6;

    /**
     * Метод для отправки запросов сервису
     *
     * @param what - Номер запроса
     * @param object - Передаваемый параметр
     */
    private void sendObjectMessage(int what, Object object) {
        if (mIsBound) {
            if (mService == null) {
                Log.w("Debug", "Trying to send message on a null object.");
            }
            Message msg = new Message();
            msg.what = what;
            msg.obj = object;
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /** Messenger for communicating with the service. */
    Messenger mService = null;
    final Messenger mMessenger = new Messenger(new IncomingHandler());


    /** Flag indicating whether we have called bind on the service. */
    boolean mIsBound;


    private volatile boolean serviceAnswer = false;
    private final Object mutexAnswer = new Object();

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONNECTION_ANSWER:
                    // TODO Если уже пользователь уже авторизован, то запускать сразу MainActivity
                    // Пусто. Выросла капуста...
                    break;
                case MSG_GET_ID:
                    mID = (long) msg.obj;
                    if (mID != -1 && !mClientListener.openMainActivity()) {
                        requestAllDialogs();
                    }
                    break;
                case MSG_LOADING_STATUS:
                    mClientListener.printLogs((String) msg.obj, msg.arg1);
                    break;
                case MSG_AUTH_ANSWER:

                    long id = (long) msg.obj;
                    if (id != -1) {
                        mID = msg.arg1;
                        mClientListener.printLogs("Registration or Auth OK", 80);
                        mClientListener.printLogs("mID = " + mID, 100);
                    } else {
                        mClientListener.printLogs("Registration or Auth FAILED", 100);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        doUnbindService();
                                        stopService();
                                        mClientListener.openMainActivity();
                                    }
                                });
                            }
                        });
                    }

                    break;
                case MSG_RECEIVE_TEXT_MESSAGE:

                    TextMessage message = (TextMessage) msg.obj;
                    getDialogByID(message.getRoomID()).addMsgOrSetSend(message);

                    mClientListener.updateUIForDialogsAndMessages();
                    break;
                case 5: // You are added to dialog
                    // Если пользователя пригласили в беседу, то отправляем запрос на получение
                    // названия беседы
                    long dialogID = (long) msg.obj;
                    boolean isExist = false;
                    for (Dialog dialog : mDialogs) {
                        if (dialog.getID() == dialogID) {
                            isExist = true;
                            break;
                        }
                    }
                    if (!isExist) {
                        sendObjectMessage(SocketAdapter.GET_DIALOG_NAME_BY_ID, msg.obj);
                        sendObjectMessage(SocketAdapter.GET_USERS_IN_DIALOG, msg.obj);
                    }
                    break;
                case SocketAdapter.GET_DIALOG_NAME_BY_ID:
                    Dialog dialog = (Dialog) msg.obj;
                    mDialogs.add(dialog);

                    mClientListener.updateUIForDialogsAndMessages();
                    break;
                case SocketAdapter.GET_USER_LOGIN_BY_ID:
                    // Пока не вижу ситуации, когда нужно проверять уже добавленные контакты по id,
                    // чтобы изменить их имя
                    Contact contact = (Contact) msg.obj;
                    mContacts.add(contact);
                    break;
                case SocketAdapter.GET_USER_ID_BY_LOGIN:
                    // Обновление id контакта по логину
                    contact = (Contact) msg.obj;
                    for (Contact cnt : mContacts)
                            if (cnt.getName().equals(contact.getName())) {
                                cnt.setID(contact.getID());
                                break;
                            }
                    break;
                case SocketAdapter.GET_USER_DIALOGS:
                    ArrayList<Long> dialogsIDs = (ArrayList<Long>) msg.obj;
                    for (Long cur_id : dialogsIDs) {
                        isExist = false;
                        for (Dialog dialogCur : mDialogs)
                            if (dialogCur.getID() == cur_id) {
                                isExist = true;
                                break;
                            }
                        if (!isExist) {
                            sendObjectMessage(SocketAdapter.GET_DIALOG_NAME_BY_ID, cur_id);
                            sendObjectMessage(SocketAdapter.GET_USERS_IN_DIALOG, cur_id);
                        }
                    }
                    break;
                case SocketAdapter.GET_USERS_IN_DIALOG:
                    ArrayList<Long> usersIDs = (ArrayList<Long>) msg.obj;
                    dialog = getDialogByID(usersIDs.get(0));
                    usersIDs.remove(0);
                    dialog.setContactsIDs(usersIDs);
                    break;
                case SocketAdapter.GET_COUNT_OF_MESSAGES_IN_DIALOG:
                    dialog = (Dialog) msg.obj;
                    long msgCount = dialog.getMessagesCount();

                    for (Dialog curDialog : mDialogs) {
                        if (curDialog.getID() == dialog.getID()) {
                            for (long i = curDialog.getCurrentMessagesCount(); i < msgCount; ++i) {
                                message = new TextMessage(i, -1, curDialog.getID(), "", "", 0);
                                sendObjectMessage(SocketAdapter.GET_MESSAGE_BY_ID_AND_DIALOG_ID, message);
                            }
                            break;
                        }
                    }

                    break;
                default:
                    Log.e("Debug", "Unregistered message: msg.what = " + msg.what);
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            mService = new Messenger(service);

            try {
                Message msg = Message.obtain(null,
                        ClientService.MSG_CONNECT_TO_SERVICE);
                msg.replyTo = mMessenger;

                mService.send(msg);

                // Give it some value as an example.
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }

        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
        }
    };

    void startService() {
        activity.startService(mIntent);
    }

    public void stopService() {
        doUnbindService();
        activity.stopService(mIntent);
    }

    void disconnectFromServer() {
        Log.e("Debug", "Disconnect");
        sendObjectMessage(ClientService.MSG_DISCONNECT_FROM_SERVER, mID);
    }

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.

        Log.e("Debug", "Binding request");
        if (!mIsBound) {
            activity.bindService(new Intent(activity,
                    ClientService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {

                Log.e("Debug", "doUnbindService");
                try {
                    Message msg = Message.obtain(null,
                            ClientService.MSG_DISCONNECT_FROM_SERVICE);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            activity.unbindService(mConnection);
            mIsBound = false;
        }
    }

    public interface onClientListener {

        boolean openMainActivity();
        void updateUIForDialogsAndMessages();
        void printLogs(String text, int progress);

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        disconnectFromServer();
    }
}
