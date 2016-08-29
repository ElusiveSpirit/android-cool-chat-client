package com.example.konstantin.cool_chat_project.fragments;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.konstantin.cool_chat_project.R;
import com.example.konstantin.cool_chat_project.support.ClientDataListener;
import com.example.konstantin.cool_chat_project.support.Dialog;
import com.example.konstantin.cool_chat_project.support.TextMessage;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class DialogFragment extends Fragment {

    public static final String USER_INDEX = "DialogFragment.USER_INDEX";
    public static final String DIALOG_INDEX = "DialogFragment.DIALOG_INDEX";
    private static final long DEFAULT_USER_INDEX = -1;
    private static final long DEFAULT_DIALOG_INDEX = -1;

    private onDialogFragmentListener mFragmentListener;
    private ClientDataListener mClientListener;

    public long mID;

    private DialogArrayAdapter dialogArrayAdapter;

    private long mDialogID;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onDialogFragmentListener) {
            mFragmentListener = (onDialogFragmentListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement DialogFragment.onDialogFragmentListener");
        }
        if (context instanceof ClientDataListener) {
            mClientListener = (ClientDataListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement ClientDataListener");
        }
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        final View view = inflater.inflate(R.layout.fragment_dialog, container, false);

        dialogArrayAdapter = new DialogArrayAdapter(inflater);

        ListView listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(dialogArrayAdapter);

        (view.findViewById(R.id.buttonSend)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Отправка сообщения
                mFragmentListener.sendMessage(mDialogID, ((EditText)view.findViewById(R.id.editText)).getText().toString());
                ((EditText)view.findViewById(R.id.editText)).setText("");

            }
        });
        ((EditText)view.findViewById(R.id.editText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                ((ListView)view.findViewById(R.id.listView)).smoothScrollByOffset(View.SCROLLBAR_POSITION_RIGHT);

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        mDialogID = args != null ? args.getLong(DIALOG_INDEX, DEFAULT_DIALOG_INDEX) : DEFAULT_DIALOG_INDEX;
        mID = args != null ? args.getLong(USER_INDEX, DEFAULT_USER_INDEX) : DEFAULT_USER_INDEX;


        if (mDialogID != DEFAULT_DIALOG_INDEX && dialogArrayAdapter != null) {
            Dialog dialog = mClientListener.getDialogByID(mDialogID);
            if (dialog != null) {
                dialogArrayAdapter.addItems(dialog.getMessages());
                getActivity().setTitle(dialog.getName());
            } else {
                getActivity().setTitle("");
            }
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_dialog, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_dialog_detail:
                mFragmentListener.openDetailDialogFragment(mDialogID);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateAdapterData() {
        dialogArrayAdapter.notifyDataSetChanged();
    }

    class DialogArrayAdapter extends BaseAdapter {

        private ArrayList<TextMessage> messages = new ArrayList<>();

        private LayoutInflater mInflater;

        public DialogArrayAdapter(LayoutInflater inflater) {
            super();
            mInflater = inflater;
        }

        public void addItems(ArrayList<TextMessage> messages) {
            this.messages = messages;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return messages.size();
        }

        @Override
        public Object getItem(int position) {
            return messages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextMessage message = (TextMessage)this.getItem(position);


            if (message.getUserID() == mID) {
                ViewHolderIn holder;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.item_dialog_message_out_layout, parent, false);

                    holder = new ViewHolderIn();

                    holder.messageText = (TextView)convertView.findViewById(R.id.message_text);
                    holder.timeText = (TextView)convertView.findViewById(R.id.message_time);

                    convertView.setTag(holder);
                } else {
                    try {
                        holder = (ViewHolderIn) convertView.getTag();
                    } catch (Exception e) {

                        convertView = mInflater.inflate(R.layout.item_dialog_message_out_layout, parent, false);

                        holder = new ViewHolderIn();

                        holder.messageText = (TextView)convertView.findViewById(R.id.message_text);
                        holder.timeText = (TextView)convertView.findViewById(R.id.message_time);

                        convertView.setTag(holder);
                    }
                }

                holder.messageText.setText(message.getText());
                holder.timeText.setText(message.getStringTime());
            } else {
                ViewHolderOut holder;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.item_dialog_message_in_layout, parent, false);

                    holder = new ViewHolderOut();

                    holder.messageText = (TextView)convertView.findViewById(R.id.message_text);
                    holder.timeText = (TextView)convertView.findViewById(R.id.message_time);

                    convertView.setTag(holder);
                } else {
                    try {
                        holder = (ViewHolderOut) convertView.getTag();
                    } catch (Exception e) {

                        convertView = mInflater.inflate(R.layout.item_dialog_message_in_layout, parent, false);

                        holder = new ViewHolderOut();

                        holder.messageText = (TextView)convertView.findViewById(R.id.message_text);
                        holder.timeText = (TextView)convertView.findViewById(R.id.message_time);

                        convertView.setTag(holder);
                    }
                }

                holder.messageText.setText(message.getText());
                holder.timeText.setText(message.getStringTime());
            }


            return convertView;
        }

        class ViewHolderIn {
            TextView messageText;
            TextView timeText;
        }
        class ViewHolderOut {
            TextView messageText;
            TextView timeText;
        }
    }

    public interface onDialogFragmentListener {

        void openDetailDialogFragment(long id);
        void sendMessage(long dialogID, String text);

    }

}
