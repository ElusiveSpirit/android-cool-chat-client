package com.example.konstantin.cool_chat_project.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.konstantin.cool_chat_project.R;
import com.example.konstantin.cool_chat_project.support.ClientDataListener;
import com.example.konstantin.cool_chat_project.support.Contact;

import java.util.ArrayList;

/**
 *
 * Created by konstantin on 28.03.16.
 */
public class ContactsFragment extends Fragment {

    private ListView mListView;
    private ContactsListViewAdapter mAdapter;

    private ClientDataListener mClientListener;
    private onContactFragmentListener mFragmentListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onContactFragmentListener) {
            mFragmentListener = (onContactFragmentListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement ContactsFragment.onContactFragmentListener");
        }
        if (context instanceof ClientDataListener) {
            mClientListener = (ClientDataListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement ClientDataListener");
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        mListView = (ListView) view.findViewById(R.id.contacts_list_view);
        mAdapter = new ContactsListViewAdapter(inflater);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mFragmentListener.openDetailContact((Contact) mAdapter.getItem(position));
            }
        });
        mListView.setAdapter(mAdapter);

        getActivity().setTitle(R.string.contacts);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter.setContacts(mClientListener.getContacts());
    }

    class ContactsListViewAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        private ArrayList<Contact> mContacts = new ArrayList<>();

        public ContactsListViewAdapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        public void setContacts(ArrayList<Contact> contacts) {
            if (contacts != null)
                mContacts = contacts;
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

            viewHolder.name.setText(contact.getName());
            viewHolder.name.setCompoundDrawablesWithIntrinsicBounds(contact.getAvatar(), null, null, null);

            return convertView;
        }

        class ViewHolder {
            TextView name;
        }
    }

    public interface onContactFragmentListener {
        void openDetailContact(Contact contact);
    }
}
