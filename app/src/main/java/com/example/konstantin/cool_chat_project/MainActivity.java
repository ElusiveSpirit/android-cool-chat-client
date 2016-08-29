package com.example.konstantin.cool_chat_project;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.example.konstantin.cool_chat_project.fragments.*;
import com.example.konstantin.cool_chat_project.support.ClientDataListener;
import com.example.konstantin.cool_chat_project.support.Contact;
import com.example.konstantin.cool_chat_project.support.Dialog;
import com.example.konstantin.cool_chat_project.support.ShowSnackSender;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements  Client.onClientListener,
                    MainDialogsFragment.onMainDialogsFragmentListener,
                    DialogFragment.onDialogFragmentListener,
                    AddNewDialogFragment.onAddNewDialogFragmentListener,
                    FragmentManager.OnBackStackChangedListener,
                    ContactsFragment.onContactFragmentListener,
                    NavigationView.OnNavigationItemSelectedListener,
                    DetailContactFragment.OnAddNewContactFragmentListener,
                    DetailDialogFragment.OnDetailDialogFragmentListener,
                    ShowSnackSender,
                    ClientDataListener {

    public static final String MAIN_DIALOGS_FRAGMENT_TAG = "MAIN_DIALOGS_FRAGMENT_TAG";
    public static final String DIALOG_FRAGMENT_TAG = "DIALOG_FRAGMENT_TAG";
    public static final String ADD_NEW_DIALOG_FRAGMENT_TAG = "ADD_NEW_DIALOG_FRAGMENT_TAG";
    public static final String CONTACTS_FRAGMENT_TAG = "CONTACTS_FRAGMENT_TAG";
    public static final String DETAIL_CONTACT_FRAGMENT_TAG = "DETAIL_CONTACT_FRAGMENT_TAG";
    public static final String DETAIL_DIALOG_FRAGMENT_TAG = "DETAIL_DIALOG_FRAGMENT_TAG";

    private MainDialogsFragment mMainDialogsFragment;
    private DialogFragment mDialogFragment;

    private FragmentManager mFragmentManager;

    private Client mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if (findViewById(R.id.fragment_container) != null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new MainDialogsFragment(), MAIN_DIALOGS_FRAGMENT_TAG)
                    .commit();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatingButtonClicked();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(this);

        mClient = new Client(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        Log.e("Debug", "onStart");
        mClient.doBindService();
    }


    @Override
    protected void onStop() {
        super.onStop();
        mClient.doUnbindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mClient.doUnbindService();
        //mClient.stopService();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        FragmentManager fragmentManager = getSupportFragmentManager();

        mMainDialogsFragment = (MainDialogsFragment)fragmentManager.findFragmentByTag(MAIN_DIALOGS_FRAGMENT_TAG);
        mDialogFragment = (DialogFragment) fragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG);
    }

    @Override
    public void openDialog(long dialogID) {
        DialogFragment fragment = new DialogFragment();

        Bundle args = new Bundle();
        args.putLong(DialogFragment.DIALOG_INDEX, dialogID);
        args.putLong(DialogFragment.USER_INDEX, mClient.getID());
        fragment.setArguments(args);

        openFragment(fragment, DIALOG_FRAGMENT_TAG);

        mClient.requestMessagesInDialog(dialogID);
    }

    @Override
    public Contact getContactByID(long contactID) {
        return mClient.getContactByID(contactID);
    }

    private void openFragment(Fragment fragment, String tag) {
        if (findViewById(R.id.fragment_container) != null) {
            // Если фрагмент уже отрыт, то новая копия не будет создана
            if (mFragmentManager.findFragmentByTag(tag) != null &&
                    mFragmentManager.findFragmentByTag(tag).isVisible())
                return;

            // Замена фрагмента
            mFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment, tag)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();

        }
    }

    @Override
    public void openFragment(Fragment fragment, String tag, boolean addToBackStack) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.fragment_container, fragment, tag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if (addToBackStack)
            ft.addToBackStack(null);

        ft.commit();
    }

    private void floatingButtonClicked() {
        if (mFragmentManager.findFragmentByTag(MAIN_DIALOGS_FRAGMENT_TAG).isVisible()) {
            openFragment(new AddNewDialogFragment(), ADD_NEW_DIALOG_FRAGMENT_TAG);
        }
        else if (mFragmentManager.findFragmentByTag(CONTACTS_FRAGMENT_TAG).isVisible()) {
            openFragment(new DetailContactFragment(), DETAIL_CONTACT_FRAGMENT_TAG);
        }
    }

    @Override
    public void openDetailContact(Contact contact) {
        DetailContactFragment fragment = new DetailContactFragment();

        Bundle args = new Bundle();
        args.putSerializable(DetailContactFragment.CONTACT_OBJECT, contact);
        fragment.setArguments(args);

        openFragment(fragment, DETAIL_CONTACT_FRAGMENT_TAG);
    }

    @Override
    public void openDetailDialogFragment(long id) {
        DetailDialogFragment fragment = new DetailDialogFragment();

        Bundle args = new Bundle();
        args.putLong(DetailDialogFragment.DIALOG_INDEX, id);
        fragment.setArguments(args);

        openFragment(fragment, DETAIL_DIALOG_FRAGMENT_TAG);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackStackChanged() {
        if (mFragmentManager.findFragmentByTag(MAIN_DIALOGS_FRAGMENT_TAG).isVisible() ||
                ( mFragmentManager.findFragmentByTag(CONTACTS_FRAGMENT_TAG) != null &&
                        mFragmentManager.findFragmentByTag(CONTACTS_FRAGMENT_TAG).isVisible()))
            findViewById(R.id.fab).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.fab).setVisibility(View.INVISIBLE);
    }

    @Override
    public ArrayList<Dialog> getDialogs() {
        if (mClient != null)
            return mClient.getDialogs();
        return null;
    }

    @Override
    public boolean openMainActivity() {
        return false;
    }

    @Override
    public void updateUIForDialogsAndMessages() {
        if (mDialogFragment == null || mMainDialogsFragment == null) {
            mMainDialogsFragment = (MainDialogsFragment)getSupportFragmentManager().findFragmentByTag(MAIN_DIALOGS_FRAGMENT_TAG);
            mDialogFragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG);
        }
        if (mDialogFragment != null) {
            Log.e("Debug", "NOTIFY");
            mDialogFragment.updateAdapterData();
        }
        if (mMainDialogsFragment != null)
            mMainDialogsFragment.updateAdapterData();
    }

    @Override
    public void printLogs(String text, int progress) {

    }


    @Override
    public void showSnack(String text, int duration) {
        Snackbar.make(findViewById(R.id.fragment_container), text, duration).show();
    }

    @Override
    public void sendMessage(long dialogID, String text) {
        if (mClient != null)
            mClient.requestSendTextMessage(dialogID, text);
    }

    @Override
    public Dialog getDialogByID(long id) {
        if (mClient != null)
            return mClient.getDialogByID(id);
        return null;
    }

    @Override
    public void createDialog(String name, ArrayList<Long> IDs) {
        if (mClient != null) {
            mClient.requestCreateDialog(name, IDs);
            Log.e("Debug", "Request to create dialog");
        }
        mFragmentManager.popBackStack();
    }

    @Override
    public Client getClient() {
        return mClient;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_contacts:
                this.openFragment(new ContactsFragment(), CONTACTS_FRAGMENT_TAG);
                break;
            case R.id.nav_dialogs:
                mFragmentManager.popBackStack();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_stop_service:
                mClient.stopService();
                return true;
            case R.id.action_exit:
                mClient.disconnectFromServer();
                mClient.doUnbindService();

                startActivity(new Intent(this, LoadingActivity.class));
                finish();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public ArrayList<Contact> getContacts() {
        if (mClient != null)
            return mClient.getContacts();
        return null;
    }

    @Override
    public void openDialogsWithCurrentUser(String login) {
        MainDialogsFragment fragment = new MainDialogsFragment();

        Bundle args = new Bundle();
        args.putString(MainDialogsFragment.FILTER, login);
        fragment.setArguments(args);

        mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, MAIN_DIALOGS_FRAGMENT_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public void createContact(String name, Drawable avatar) {
        if (mClient != null)
            mClient.addContact(name, avatar);
        mFragmentManager.popBackStack();
    }

}
