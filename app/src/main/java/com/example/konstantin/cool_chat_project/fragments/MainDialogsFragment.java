package com.example.konstantin.cool_chat_project.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.konstantin.cool_chat_project.R;
import com.example.konstantin.cool_chat_project.support.ClientDataListener;
import com.example.konstantin.cool_chat_project.support.Dialog;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by konstantin on 26.03.16.
 *
 */
public class MainDialogsFragment extends Fragment {

    public static final String FILTER = "MainDialogsFragment.FILTER";

    private onMainDialogsFragmentListener mFragmentListener;
    private ClientDataListener mClientListener;

    private ListView mListView;
    private AllDialogsListViewAdapter mListViewAdapter;

    private AutoCompleteTextView mFilterView;
    private TextView mErrorView;
    private ImageView mImageClear;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof onMainDialogsFragmentListener) {
            mFragmentListener = (onMainDialogsFragmentListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement MyListFragment.OnItemSelectedListener");
        }
        if (context instanceof ClientDataListener) {
            mClientListener = (ClientDataListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement ClientDataListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main_dialogs, container, false);

        mFilterView = (AutoCompleteTextView) view.findViewById(R.id.main_dialogs_search);
        mFilterView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //if (s.charAt(count) != KeyEvent.KEYCODE_ENTER) {
                //}
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mListViewAdapter.setFilter(mFilterView.getText().toString());
                if (mFilterView.getText().toString().equals(""))
                    mImageClear.setVisibility(View.GONE);
            }
        });

        mErrorView = (TextView) view.findViewById(R.id.main_dialogs_not_found);
        mImageClear = (ImageView) view.findViewById(R.id.main_dialogs_clear_search);
        mImageClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListViewAdapter.setFilter("");
            }
        });

        mListView = (ListView) view.findViewById(R.id.all_dialogs_listView);
        mListViewAdapter = new AllDialogsListViewAdapter(inflater);

        mListView.setAdapter(mListViewAdapter);
        mListView.setOnItemClickListener(mListViewAdapter);

        getActivity().setTitle(R.string.dialogs);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            String filter = args.getString(FILTER);
            if (filter != null) {
                mFilterView.setText(filter);
            }
            mListViewAdapter.setFilter(filter);
        }

        mListViewAdapter.setDialogs(mClientListener.getDialogs());
    }

    public void updateAdapterData() {
        mListViewAdapter.filter();
    }

    class AllDialogsListViewAdapter extends BaseAdapter
            implements AdapterView.OnItemClickListener {
            // TODO implements Filterable http://stackoverflow.com/questions/14663725/list-view-filter-android

        private ArrayList<Dialog> mDialogs = new ArrayList<>();
        private ArrayList<Dialog> mFilteredDialogs = new ArrayList<>();
        private LayoutInflater mInflater;

        private String mFilter;
        private DialogFilter mDialogFilter;

        AllDialogsListViewAdapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        public void setDialogs(ArrayList<Dialog> dialogs) {
            if (dialogs != null) {
                mDialogs = dialogs;
            }
            filter();
        }

        private void filter() {
            if (mFilter == null || mFilter.equals("")) {
                mFilteredDialogs.clear();
                mFilteredDialogs.addAll(mDialogs);
                notifyDataSetChanged();
                mListView.setVisibility(View.VISIBLE);
                mErrorView.setVisibility(View.GONE);
            } else {
                if (mDialogFilter != null && mDialogFilter.getStatus() == AsyncTask.Status.RUNNING) {
                    mDialogFilter.cancel(false);
                }
                mDialogFilter = new DialogFilter();
                mDialogFilter.execute();
            }
        }

        public void setFilter(String filter) {
            if (filter != null) {
                mFilter = filter;
            } else {
                mFilter = "";
            }
            filter();
        }

        @Override
        public int getCount() {
            return mFilteredDialogs.size();
        }

        @Override
        public Object getItem(int position) {
            return mFilteredDialogs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mFilteredDialogs.get(position).getID();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_main_dialogs, parent, false);

                holder = new ViewHolder();

                holder.dialogName = (TextView) convertView.findViewById(R.id.main_dialogs_name);
                holder.dialogText = (TextView) convertView.findViewById(R.id.main_dialogs_text);
                holder.dialogTime = (TextView) convertView.findViewById(R.id.main_dialogs_time);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Dialog dialog = (Dialog) getItem(position);

            holder.dialogName.setText(dialog.getName());
            holder.dialogText.setText(dialog.getText());
            holder.dialogTime.setText(dialog.getTime());

            return convertView;
        }

        class ViewHolder {
            TextView dialogName;
            TextView dialogText;
            TextView dialogTime;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mFragmentListener.openDialog(id);
        }

        class DialogFilter extends AsyncTask<Void, Dialog, Void> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mFilteredDialogs.clear();
                mListView.setVisibility(View.VISIBLE);
                mErrorView.setVisibility(View.GONE);
                notifyDataSetChanged();
            }

            @Override
            protected Void doInBackground(Void... params) {
                for (Dialog dialog : mDialogs) {
                    if (isCancelled()) {
                        return null;
                    }
                    if (dialog.getName().contains(mFilter)) {
                        publishProgress(dialog);
                        continue;
                    }
                    for (long contactID : dialog.getContactsIDs()) {
                        if (isCancelled()) {
                            return null;
                        }
                        if (mClientListener.getContactByID(contactID).getName().contains(mFilter)) {
                            publishProgress(dialog);
                            break;
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Dialog... values) {
                super.onProgressUpdate(values);

                Collections.addAll(mFilteredDialogs, values);

                notifyDataSetChanged();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (mFilteredDialogs.size() != 0) {
                    mListView.setVisibility(View.VISIBLE);
                    mErrorView.setVisibility(View.GONE);
                } else {
                    mListView.setVisibility(View.GONE);
                    mErrorView.setVisibility(View.VISIBLE);
                }
            }
        }
    }



    public interface onMainDialogsFragmentListener {

        void openDialog(long dialogID);
    }
}
