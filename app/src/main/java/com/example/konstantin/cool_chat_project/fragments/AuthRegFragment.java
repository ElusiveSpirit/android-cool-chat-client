package com.example.konstantin.cool_chat_project.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.konstantin.cool_chat_project.R;

/**
 *
 * Created by konstantin on 01.04.16.
 */
public class AuthRegFragment extends Fragment {

    private OnAuthAndRegFragmentListener mFragmentListener;

    private boolean isHidden;

    private EditText mLogin;
    private EditText mPassword;

    private LinearLayout mRegContainer;
    private TextView mTextView;
    private EditText mEditText;
    private Button mButton;

    private Button mRegButton;

    private String  AUTH_STRING;
    private String REG_STRING;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAuthAndRegFragmentListener) {
            mFragmentListener = (OnAuthAndRegFragmentListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implements " +
                    "AuthRegFragment.OnAuthAndRegFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reg_auth, container, false);

        AUTH_STRING = inflater.getContext().getString(R.string.enter);
        REG_STRING = inflater.getContext().getString(R.string.register);
        mTextView = (TextView) view.findViewById(R.id.reg_text_view);
        mEditText = (EditText) view.findViewById(R.id.reg_password);
        mButton = (Button) view.findViewById(R.id.auth_button);
        mRegButton = (Button) view.findViewById(R.id.reg_button);

        mRegContainer = (LinearLayout) view.findViewById(R.id.reg_container);

        mLogin = (EditText) view.findViewById(R.id.auth_login);
        mPassword = (EditText) view.findViewById(R.id.auth_password);

        hide();

        mButton.setOnClickListener(new AuthOnClickListener());
        mRegButton.setOnClickListener(new SwitchButton());


        return view;
    }

    private void show() {
        if (mTextView != null && mEditText != null && mButton != null) {
            mRegContainer.setVisibility(View.VISIBLE);
            mRegButton.setText(AUTH_STRING);
            mButton.setText(REG_STRING);
            isHidden = false;
        }
    }

    private void hide() {
        if (mTextView != null && mEditText != null && mButton != null) {
            mRegContainer.setVisibility(View.GONE);
            mRegButton.setText(REG_STRING);
            mButton.setText(AUTH_STRING);
            isHidden = true;
        }
    }

    public interface OnAuthAndRegFragmentListener {

        boolean auth(String login, String password);
        void register(String login, String password);

    }

    class SwitchButton implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mRegContainer.getVisibility() == View.GONE) {
                show();
                mButton.setOnClickListener(new RegOnClickListener());
            } else {
                hide();
                mButton.setOnClickListener(new AuthOnClickListener());
            }
        }
    }

    class RegOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (!mPassword.getText().toString().equals("") &&
                    !mLogin.getText().toString().equals("")) {
                if (mPassword.getText().toString().equals(mEditText.getText().toString())) {
                    mFragmentListener.register(mLogin.getText().toString(), mPassword.getText().toString());
                }
            }
        }
    }

    class AuthOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            if (!mPassword.getText().toString().equals("") &&
                    !mLogin.getText().toString().equals("")) {
                mFragmentListener.auth(mLogin.getText().toString(), mPassword.getText().toString());

            }

        }
    }
}
