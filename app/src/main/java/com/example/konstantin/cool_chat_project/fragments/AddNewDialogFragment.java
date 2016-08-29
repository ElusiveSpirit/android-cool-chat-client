package com.example.konstantin.cool_chat_project.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.konstantin.cool_chat_project.R;
import com.example.konstantin.cool_chat_project.support.ClientDataListener;
import com.example.konstantin.cool_chat_project.support.Contact;
import com.example.konstantin.cool_chat_project.support.ShowSnackSender;

import java.util.ArrayList;

/**
 * Fragment Class to create new Dialog
 *
 * Created by konstantin on 27.03.16.
 */
public class AddNewDialogFragment extends Fragment {

    private TextView mNameTextView;

    private ShowSnackSender mSnack;
    private ContactsChooseListAdapter mAdapter;

    private ClientDataListener mClientListener;
    private onAddNewDialogFragmentListener mFragmentListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onAddNewDialogFragmentListener) {
            mFragmentListener = (onAddNewDialogFragmentListener) context;
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
        View view = inflater.inflate(R.layout.fragment_create_new_dialog, container, false);

        mNameTextView = (TextView) view.findViewById(R.id.create_dialog_name);

        mAdapter = new ContactsChooseListAdapter(inflater);
        mAdapter.setContacts(mClientListener.getClient().getContacts());

        ListView mListView = (ListView) view.findViewById(R.id.create_dialog_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mAdapter);

        view.findViewById(R.id.create_dialog_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ok = true;
                ArrayList<Contact> contacts = mAdapter.getSelectedContacts();
                if (contacts.size() == 0) {
                    mSnack.showSnack("Выберите контакт", Snackbar.LENGTH_LONG);
                    ok = false;
                }
                if (mNameTextView.getText().length() == 0) {
                    mNameTextView.setHint("Введите название диалога");
                    ok = false;
                }
                if (ok) {
                    ArrayList<Long> ids = new ArrayList<>();
                    for (Contact contact : contacts)
                        ids.add(contact.getID());
                    mFragmentListener.createDialog(mNameTextView.getText().toString(), ids);
                }
            }
        });

        getActivity().setTitle(R.string.new_dialog);

        return view;
    }

    class ContactsChooseListAdapter extends BaseAdapter
            implements ListView.OnItemClickListener {

        private LayoutInflater mInflater;
        private ArrayList<SelectableContact> mContacts = new ArrayList<>();

        ContactsChooseListAdapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        public void setContacts(ArrayList<Contact> contacts) {
            for (Contact contact : contacts)
                mContacts.add(new SelectableContact(contact));
        }

        public ArrayList<Contact> getSelectedContacts() {
            ArrayList<Contact> selected = new ArrayList<>();
            for (SelectableContact contact : mContacts)
                if (contact.isSelected)
                    selected.add(contact.contact);
            return selected;
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
            return mContacts.get(position).contact.getID();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_checkable_contacts, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.name = (CheckedTextView) convertView.findViewById(R.id.checkedContact);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            SelectableContact selectableContact = (SelectableContact) getItem(position);
            Contact contact = selectableContact.contact;

            viewHolder.name.setText(contact.getName());
            viewHolder.name.setCompoundDrawablesWithIntrinsicBounds(contact.getAvatar(), null, null, null);
            viewHolder.name.setChecked(selectableContact.isSelected);
            /*
            if (selectableContact.isSelected) {
                viewHolder.name.setBackground(mInflater.getContext().getDrawable(R.drawable.contact_choosen_bg));
                viewHolder.name.setCompoundDrawablesWithIntrinsicBounds(contact.getAvatar(),
                        null, mInflater.getContext().getDrawable(R.drawable.item_check), null);
            } else {
                viewHolder.name.setBackground(null);
                viewHolder.name.setCompoundDrawablesWithIntrinsicBounds(contact.getAvatar(),
                        null, mInflater.getContext().getDrawable(R.drawable.not_selected_item_bg), null);
            }
            */
            return convertView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mContacts.get(position).isSelected = !mContacts.get(position).isSelected;
            notifyDataSetChanged();
        }

        class ViewHolder {
            CheckedTextView name;
        }

        class SelectableContact {
            Contact contact;
            boolean isSelected = false;

            SelectableContact(Contact contact) {
                this.contact = contact;
            }
        }
    }

    public interface onAddNewDialogFragmentListener {
        void createDialog(String name, ArrayList<Long> IDs);
    }
}
