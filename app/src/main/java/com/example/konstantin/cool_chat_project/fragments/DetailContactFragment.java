package com.example.konstantin.cool_chat_project.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.konstantin.cool_chat_project.R;
import com.example.konstantin.cool_chat_project.support.ClientDataListener;
import com.example.konstantin.cool_chat_project.support.Contact;
import com.example.konstantin.cool_chat_project.support.ShowSnackSender;

import java.io.IOException;

/**
 *
 * Created by konstantin on 28.03.16.
 */
public class DetailContactFragment extends Fragment {

    public static final String CONTACT_OBJECT = "ContactsFragment.CONTACT_OBJECT";

    private ShowSnackSender mSnack;
    private ClientDataListener mClientListener;
    private OnAddNewContactFragmentListener mFragmentListener;
    private Context mContext;

    private int PICK_IMAGE_REQUEST = 1;
    private boolean isNewImage = false;

    private android.support.design.widget.FloatingActionButton mFab;
    private android.support.design.widget.AppBarLayout mImageView;
    private EditText mEditText;
    private TextView mTextView;

    private Contact mContact;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof OnAddNewContactFragmentListener) {
            mFragmentListener = (OnAddNewContactFragmentListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement DetailContactFragment.OnAddNewContactFragmentListener");
        }
        if (context instanceof ShowSnackSender) {
            mSnack = (ShowSnackSender) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must ShowSnackSender");
        }
        if (context instanceof ClientDataListener) {
            mClientListener = (ClientDataListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement ClientDataListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if (args != null)
            mContact = (Contact) args.getSerializable(CONTACT_OBJECT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_contact, container, false);

        mImageView = (android.support.design.widget.AppBarLayout) view.findViewById(R.id.app_bar);
        mEditText = (EditText) view.findViewById(R.id.detail_contact_name);
        mFab = (android.support.design.widget.FloatingActionButton) view.findViewById(R.id.detail_contact_fab);
        mTextView = (TextView) view.findViewById(R.id.detail_contact_error);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContact != null) {
                    mFragmentListener.openDialogsWithCurrentUser(mContact.getName());
                }
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        if (mContact != null) {
            mEditText.setText(mContact.getName());
            if (mContact.isExist())
                mTextView.setVisibility(View.GONE);
        } else {
            mFab.setVisibility(View.INVISIBLE);
        }

        getActivity().setTitle("");

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        if (mContact == null) {
            // Если нужно сохранить новый контакт
            inflater.inflate(R.menu.menu_detail_contact, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_save:
                if (mEditText.getText().length() != 0) {
                    if (isNewImage) {
                        mClientListener.createContact(mEditText.getText().toString(), mImageView.getBackground());
                    } else {
                        mClientListener.createContact(mEditText.getText().toString(), mContext.getDrawable(R.drawable.ic_account_circle_black_24dp));
                    }
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST /*&& resultCode == RESULT_OK*/ && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                // TODO Настроить ресайз битмапа
                isNewImage = true;
                mImageView.setBackground(Drawable.createFromPath(uri.getPath()));
            } catch (Exception e) {
                e.printStackTrace();
                mSnack.showSnack("Ошибка при загрузке изображения", Snackbar.LENGTH_SHORT);
            }

        }
    }

    public interface OnAddNewContactFragmentListener {
        void openDialogsWithCurrentUser(String login);
    }
}
