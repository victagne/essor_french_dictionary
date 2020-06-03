package com.helena.essorfrench;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class HistoryActivity extends AppCompatActivity {

    private static String TAG = "HistoryActivity";
    private List<String> myList;
    private ListView historyList;
    private TextView emptyView;
    private View divider;
    private HistoryListAdapter historyListAdapter;
    private DictDataHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle(R.string.history_words);
        }
        setContentView(R.layout.activity_history);

        historyList = findViewById(R.id.historyList);
        emptyView = findViewById(R.id.emptyViewHistory);
        divider = findViewById(R.id.divider);
        mDbHelper = new DictDataHelper(this);
        mDbHelper.createDatabase();
        mDbHelper.open();

        myList = new ArrayList<>();
        historyListAdapter = new HistoryListAdapter(this, R.layout.history_list_item, myList);
        historyList.setAdapter(historyListAdapter);
        historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView v = view.findViewById(R.id.historyWord);
                String word = v.getText().toString();
                String meaning = "";
                boolean star = false;
                Cursor cursor = mDbHelper.searchSingleWord(word);
                if (cursor != null) {
                    meaning = cursor.getString(0);
                    star = cursor.getInt(1) > 0;
                    cursor.close();
                }

                Intent i = new Intent(HistoryActivity.this, MeaningActivity.class);
                i.putExtra("word", word);
                i.putExtra("meaning", meaning);
                i.putExtra("star", star);
                startActivity(i);
            }
        });
        historyList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        historyList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO  Auto-generated method stub
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO  Auto-generated method stub
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // TODO  Auto-generated method stub
                mode.getMenuInflater().inflate(R.menu.multiple_delete, menu);
                return true;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                // TODO  Auto-generated method stub
                switch (item.getItemId()) {
                    case R.id.selectAll:
                        final int checkedCount = myList.size();
                        historyListAdapter.removeSelection();
                        for (int i = 0; i < checkedCount; i++) {
                            historyList.setItemChecked(i, true);
                        }
                        mode.setTitle(checkedCount+"");
                        return true;
                    case R.id.delete:
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                HistoryActivity.this);
                        builder.setMessage(R.string.dialog_delete);
                        builder.setNegativeButton(R.string.dialog_option_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO  Auto-generated method stub
                            }
                        });
                        builder.setPositiveButton(R.string.dialog_option_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO  Auto-generated method stub
                                SparseBooleanArray selected = historyListAdapter.getSelectedIds();
                                for (int i = (selected.size() - 1); i >= 0; i--) {
                                    if (selected.valueAt(i)) {
                                        String selectedItem = historyListAdapter
                                                .getItem(selected.keyAt(i));
                                        historyListAdapter.remove(selectedItem);
                                        PrefUtils prefUtils = new PrefUtils(HistoryActivity.this);
                                        prefUtils.removeWordFromHistory(selectedItem);
                                    }
                                }

                                if(myList.size() == 0){
                                    emptyView.setVisibility(View.VISIBLE);
                                    divider.setVisibility(View.GONE);
                                }else{
                                    emptyView.setVisibility(View.GONE);
                                    divider.setVisibility(View.VISIBLE);
                                }
                                // Close CAB
                                mode.finish();
                                selected.clear();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.setTitle(R.string.dialog_title_confirmation);
                        alert.show();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position, long id, boolean checked) {
                // TODO  Auto-generated method stub
                final int checkedCount = historyList.getCheckedItemCount();
                mode.setTitle(checkedCount+"");
                historyListAdapter.toggleSelection(position);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        myList.clear();
        new HistoryQueryTask().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDbHelper != null)
            mDbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_clear_history_icon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_clear) {
            clearHistory();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearHistory(){
        if(myList.isEmpty()){
            Toast.makeText(HistoryActivity.this, R.string.no_history_to_clear, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(
                HistoryActivity.this);
        builder.setMessage(R.string.clear_all_history_confirmation);
        builder.setNegativeButton(R.string.dialog_option_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton(R.string.dialog_option_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new PrefUtils(HistoryActivity.this).clearHistory();
                myList.clear();
                emptyView.setVisibility(View.VISIBLE);
                divider.setVisibility(View.GONE);
                historyListAdapter.notifyDataSetChanged();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    //history query task
    private class HistoryQueryTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            PrefUtils prefUtils = new PrefUtils(HistoryActivity.this);
            return prefUtils.getHistory();
        }

        @Override
        protected void onPostExecute(String history) {
            if(history.isEmpty()){
                emptyView.setVisibility(View.VISIBLE);
                divider.setVisibility(View.GONE);
                historyListAdapter.notifyDataSetChanged();
                return;
            }else{
                emptyView.setVisibility(View.GONE);
                divider.setVisibility(View.VISIBLE);
            }

            String[] historyArray = history.split(";");
            for(int i=0; i<historyArray.length; i++){
                myList.add(i, historyArray[i]);
            }
            historyListAdapter.notifyDataSetChanged();
        }
    }
}
