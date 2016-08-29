package com.example.konstantin.cool_chat_project.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.konstantin.cool_chat_project.MainActivity;
import com.example.konstantin.cool_chat_project.R;
import com.example.konstantin.cool_chat_project.support.ClientDataListener;
import com.example.konstantin.cool_chat_project.support.Contact;
import com.example.konstantin.cool_chat_project.support.Dialog;
import com.example.konstantin.cool_chat_project.support.ShowSnackSender;

import java.util.ArrayList;

/**
 *
 * Created by konstantin on 14.04.16.
 */
public class DetailDialogFragment extends Fragment {

    public static final String DIALOG_INDEX = "DetailDialogFragment.DIALOG_INDEX";
    private static final long DEFAULT_DIALOG_INDEX = -1;

    private TextView mNameTextView;
    private ImageView mImageView;

    private ShowSnackSender mSnack;
    private DialogContactsListAdapter mAdapter;

    private ClientDataListener mClientListener;
    private OnDetailDialogFragmentListener mFragmentListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDetailDialogFragmentListener) {
            mFragmentListener = (OnDetailDialogFragmentListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement AddNewDialogFragment.onAddNewDialogFragmentListener");
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_dialog, container, false);

        mNameTextView = (TextView) view.findViewById(R.id.detail_dialog_name);
        mImageView = (ImageView) view.findViewById(R.id.detail_dialog_image);

        mAdapter = new DialogContactsListAdapter(inflater);
        ListView listView = (ListView) view.findViewById(R.id.detail_dialog_list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(mAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        long dialogID = args != null ? args.getLong(DIALOG_INDEX, DEFAULT_DIALOG_INDEX) : DEFAULT_DIALOG_INDEX;


        if (dialogID != DEFAULT_DIALOG_INDEX && mAdapter != null) {
            Dialog dialog = mClientListener.getDialogByID(dialogID);
            if (dialog != null) {
                mNameTextView.setText(dialog.getText());
                ArrayList<Contact> contacts = new ArrayList<>();
                for (long contactID : dialog.getContactsIDs()) {
                    contacts.add(mClientListener.getContactByID(contactID));
                }
                mAdapter.init(dialog, contacts);
                getActivity().setTitle(dialog.getName());
            } else {
                getActivity().setTitle("");
            }
        }

    }

    class DialogContactsListAdapter extends BaseAdapter
            implements ListView.OnItemClickListener {

        private LayoutInflater mInflater;

        private Dialog mDialog;
        private ArrayList<Contact> mContacts = new ArrayList<>();

        DialogContactsListAdapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        public void init(Dialog dialog, ArrayList<Contact> contacts) {
            mDialog = dialog;
            mContacts.addAll(contacts);
            mContacts.add(new Contact(-1, ""));
        }

        @Override
        public int getCount() {
            return mContacts.size();
        }

        @Override
        public Object getItem(int position) {
            return mContacts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mContacts.get(position).getID();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_contacts, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.contact_name);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Contact contact = (Contact) getItem(position);
            if (contact.getID() != -1) {
                viewHolder.name.setText(contact.getName());
                viewHolder.name.setCompoundDrawablesWithIntrinsicBounds(contact.getAvatar(), null, null, null);
            } else {
                viewHolder.name.setText(getString(R.string.invite));
                viewHolder.name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

            }


            return convertView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (id == -1) {
                mSnack.showSnack("Invite user", Snackbar.LENGTH_SHORT);
            } else {
                DetailContactFragment fragment = new DetailContactFragment();

                Bundle args = new Bundle();
                args.putSerializable(DetailContactFragment.CONTACT_OBJECT, (Contact) getItem(position));
                fragment.setArguments(args);

                mClientListener.openFragment(fragment, MainActivity.DETAIL_CONTACT_FRAGMENT_TAG, true);
            }
        }

        class ViewHolder {
            TextView name;
        }

    }

    public interface OnDetailDialogFragmentListener {

    }
}
