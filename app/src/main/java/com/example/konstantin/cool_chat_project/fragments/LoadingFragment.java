package com.example.konstantin.cool_chat_project.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.konstantin.cool_chat_project.R;


/**
 *
 * Created by konstantin on 31.03.16.
 */
public class LoadingFragment extends Fragment {

    private OnLoadingFragmentListener mFragmentListener;

    private ContentLoadingProgressBar mProgressBar;
    private TextView mTextView;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoadingFragmentListener) {
            mFragmentListener = (OnLoadingFragmentListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implements " +
                    "LoadingFragment.OnLoadingFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        View view = inflater.inflate(R.layout.fragment_loading, container, false);

        mProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.loading_app_progress_bar);
        mTextView = (TextView) view.findViewById(R.id.loading_app_text);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setLoadingText(String line, int progress) {

        if (line.contains("mID = ")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mFragmentListener.startMainActivity();
                }
            }).start();
            line = line.substring(6);
            // TODO Принимаемый id не всегда совпадает с реальным
            mTextView.setText("Ваш ID = " + line);
            mProgressBar.setProgress(100);
        } else {
            mTextView.setText(line);
            mProgressBar.setProgress(progress);
        }
    }

    public interface OnLoadingFragmentListener {
        void startMainActivity();
    }
}
