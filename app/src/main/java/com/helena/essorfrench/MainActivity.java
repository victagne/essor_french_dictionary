package com.helena.essorfrench;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        SearchView.OnCloseListener, AbsListView.OnScrollListener {
    private static String TAG = "MainActivity";

    private DictDataHelper mDbHelper;
    private ListView mList;
    private SearchView searchView;
    private CustomArrayAdapter dataAdapter;
    private ArrayList<String> dataArray;
    //indicate new query
    private boolean bNeedClear = false;
    //previous string
    private String oldQueryStr = "";
    private final int QUERY_NUMBER = 20;
    private int queryOffset = 0;
    private ProgressDialog pd;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.mipmap.ic_launcher);
            actionBar.setTitle("   " + getResources().getString(R.string.app_name));
        }

        mList = findViewById(R.id.wordList);
        searchView = findViewById(R.id.search);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

        //adapter
        dataArray = new ArrayList<>();
        dataAdapter = new CustomArrayAdapter(this, dataArray);
        mList.setAdapter(dataAdapter);
        mList.setOnScrollListener(this);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView v = view.findViewById(R.id.word);
                String word = v.getText().toString();
                String meaning = "";
                Boolean star = false;
                Cursor cursor = mDbHelper.searchSingleWord(word);
                if (cursor != null) {
                    meaning = cursor.getString(0);
                    star = cursor.getInt(1) > 0;
                    cursor.close();
                }

                Intent i = new Intent(MainActivity.this, MeaningActivity.class);
                i.putExtra("word", word);
                i.putExtra("meaning", meaning);
                i.putExtra("star", star);
                startActivity(i);
            }
        });

        mDbHelper = new DictDataHelper(this);
        if (!mDbHelper.dictDataExists()) {
            pd = new ProgressDialog(this);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setMessage(getResources().getString(R.string.copy_dict_data));
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.show();

            handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    pd.dismiss();
                    mDbHelper.open();
                    loadAllWords();
                }
            };

            new Thread(new Runnable() {
                @Override
                public void run() {
                    mDbHelper.createDatabase();
                    handler.sendEmptyMessage(0);
                }
            }).start();

        }else{
            mDbHelper.open();
            loadAllWords();
        }
    }

    @Override
    public void onBackPressed() {

        try{
            super.onBackPressed();
        }catch (IllegalStateException e){
            // can output some information here
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchView != null) {
            searchView.clearFocus();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onClose() {
        dataAdapter.clear();
        dataAdapter.notifyDataSetChanged();
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        setNeedClear(true);
        loadWords(s.trim());
        return false;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //Log.d(TAG, "onScrollStateChanged scrollState = " + scrollState);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //if view's count doesn't equal to n*QUERY_NUMBER, no need to query more data
        if ((view.getCount() > 0) && (view.getCount() % QUERY_NUMBER == 0)
                && (totalItemCount == firstVisibleItem + visibleItemCount)) {
            setNeedClear(false);
            loadWords(searchView.getQuery().toString().trim());
        }
    }

    private void loadWords(String s) {
        if (s.equals(oldQueryStr) && !s.isEmpty() && isNeedClear()) {
            return;
        }
        oldQueryStr = s;
        new DBQueryTask().execute(s);
    }

    private void loadAllWords() {
        setNeedClear(true);
        new DBQueryTask().execute("");
    }

    public boolean isNeedClear() {
        return bNeedClear;
    }

    public void setNeedClear(boolean bNeedClear) {
        this.bNeedClear = bNeedClear;
    }

    //db query task
    private class DBQueryTask extends AsyncTask<String, Void, Cursor> {

        @Override
        protected Cursor doInBackground(String... params) {

            if (isNeedClear()) {
                queryOffset = 0;
            } else {
                queryOffset += QUERY_NUMBER;
            }

            Cursor cursor;
            if (params[0].isEmpty()) { //get all words
                cursor = mDbHelper.searchAllWords(queryOffset, QUERY_NUMBER);
            } else {
                cursor = mDbHelper.searchWordsList(params[0], queryOffset, QUERY_NUMBER);
            }

            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            //not clear list when scrolling
            if (isNeedClear()) {
                dataArray.clear();
            }

            if (cursor != null && cursor.getCount() > 0
                    && cursor.moveToFirst()) {
                do {
                    dataArray.add(cursor.getString(cursor.getColumnIndex("word")));
                } while (cursor.moveToNext());
            }

            if (cursor != null) {
                cursor.close();
            }

            //when inputting characters quickly, function onQueryTextChange may be called quickly
            //but previous DBQueryTask is not finished, then data array is not cleared
            //for this kind of case, use linked hash set to delete duplicated words (workaround)
            LinkedHashSet<String> lhs = new LinkedHashSet<>(dataArray);
            dataArray.clear();
            dataArray.addAll(lhs);

            //this is for locating first one item, because returned cursor from db without good order
            String query = searchView.getQuery().toString().trim();
            int index;
            if(!query.isEmpty()) {
                index = dataArray.indexOf(query);
                if(index != -1){
                    dataArray.subList(0, index).clear();
                }
            }

            dataAdapter.notifyDataSetChanged();
            if (isNeedClear()) {
                mList.setSelection(0);
            }
            setNeedClear(false);
        }
    }
}
