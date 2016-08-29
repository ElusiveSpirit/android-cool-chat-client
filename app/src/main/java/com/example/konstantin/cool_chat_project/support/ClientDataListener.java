package com.example.konstantin.cool_chat_project.support;

import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;

import com.example.konstantin.cool_chat_project.Client;

import java.util.ArrayList;

/**
 *
 * Created by konstantin on 14.04.16.
 */
public interface ClientDataListener {

    Client getClient();
    Dialog getDialogByID(long id);
    Contact getContactByID(long contactID);

    ArrayList<Dialog> getDialogs();
    ArrayList<Contact> getContacts();

    void createContact(String name, Drawable avatar);

    void openFragment(Fragment fragment, String tag, boolean addToBackStack);
}
