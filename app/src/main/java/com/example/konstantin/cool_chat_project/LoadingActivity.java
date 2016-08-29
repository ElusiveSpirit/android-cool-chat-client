package com.example.konstantin.cool_chat_project;

import android.content.Intent;
import android.os.*;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.example.konstantin.cool_chat_project.fragments.AuthRegFragment;
import com.example.konstantin.cool_chat_project.fragments.LoadingFragment;
import com.example.konstantin.cool_chat_project.support.ShowSnackSender;

/**
 * An activity for connecting and auth processes of loading app
 *
 * Created by konstantin on 31.03.16.
 */
public class LoadingActivity extends AppCompatActivity implements
        AuthRegFragment.OnAuthAndRegFragmentListener,
        Client.onClientListener,
        LoadingFragment.OnLoadingFragmentListener,
        ShowSnackSender {

    private FragmentManager mFragmentManager;

    private Client mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mClient = new Client(this);
        mClient.startService();

        setContentView(R.layout.activity_loading);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarLoading);
        setSupportActionBar(toolbar);


        mFragmentManager = this.getSupportFragmentManager();

        mFragmentManager.beginTransaction()
                .add(R.id.fragment_container_loading, new AuthRegFragment())
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        //mClient.doBindService();
    }


    @Override
    protected void onStop() {
        super.onStop();
        //mClient.doUnbindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mClient.doUnbindService();
    }

    @Override
    public boolean auth(String login, String password) {
        mClient.setPersonalData(login, password);
        mClient.requestAuth();

        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_loading, new LoadingFragment(), "LOADING")
                .commit();

        return false;
    }

    @Override
    public void register(String login, String password) {
        mClient.setPersonalData(login, password);
        mClient.requestRegister();

        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_loading, new LoadingFragment(), "LOADING")
                .commit();
    }


    @Override
    public boolean openMainActivity() {
        startMainActivity();
        return false;
    }

    @Override
    public void updateUIForDialogsAndMessages() {

    }

    @Override
    public void printLogs(String text, int progress) {
        LoadingFragment fragment = (LoadingFragment) mFragmentManager.findFragmentByTag("LOADING");
        if (fragment != null)
            fragment.setLoadingText(text, progress);
    }

    @Override
    public void showSnack(String text, int duration) {

    }

    @Override
    public void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        mClient.doUnbindService();
        startActivity(intent);
        finish();
    }

}
