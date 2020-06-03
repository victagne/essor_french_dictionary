package com.helena.essorfrench;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

public class FavoriteActivity extends AppCompatActivity {

    private static String TAG = "FavoriteActivity";

    private List<String> myList;
    private ListView favoriteList;
    private FavoriteListAdapter favoriteListAdapter;
    private DictDataHelper mDbHelper;
    private ActionBar actionBar;
    private View divider;
    private TextView emptyView;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle(R.string.star_words);
        }
        setContentView(R.layout.activity_favorite);

        favoriteList = (ListView) findViewById(R.id.favoriteList);
        emptyView = (TextView) findViewById(R.id.emptyViewFavorite);
        divider = (View) findViewById(R.id.divider);

        mDbHelper = new DictDataHelper(this);
        mDbHelper.createDatabase();
        mDbHelper.open();

        myList = new ArrayList<>();
        favoriteListAdapter = new FavoriteListAdapter(this, R.layout.favorite_list_item, myList);
        favoriteList.setAdapter(favoriteListAdapter);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                //Log.d(TAG, "handleMessage");
                if (myList.size() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                    divider.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    divider.setVisibility(View.VISIBLE);
                }
                favoriteListAdapter.notifyDataSetChanged();
            }
        };

        favoriteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView v = view.findViewById(R.id.favoriteWord);
                String word = v.getText().toString();
                String meaning = "";
                Boolean star = false;
                Cursor cursor = mDbHelper.searchSingleWord(word);
                if (cursor != null) {
                    meaning = cursor.getString(0);
                    star = cursor.getInt(1) > 0;
                    cursor.close();
                }

                Intent i = new Intent(FavoriteActivity.this, MeaningActivity.class);
                i.putExtra("word", word);
                i.putExtra("meaning", meaning);
                i.putExtra("star", star);
                startActivity(i);
            }
        });

        favoriteList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        favoriteList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
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
            public boolean onActionItemClicked(final ActionMode mode,
                                               MenuItem item) {
                // TODO  Auto-generated method stub
                switch (item.getItemId()) {
                    case R.id.selectAll:
                        final int checkedCount = myList.size();
                        favoriteListAdapter.removeSelection();
                        for (int i = 0; i < checkedCount; i++) {
                            favoriteList.setItemChecked(i, true);
                        }
                        mode.setTitle(checkedCount+"");
                        return true;
                    case R.id.delete:
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                FavoriteActivity.this);
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
                                SparseBooleanArray selected = favoriteListAdapter.getSelectedIds();
                                for (int i = (selected.size() - 1); i >= 0; i--) {
                                    if (selected.valueAt(i)) {
                                        final String selectedItem = favoriteListAdapter
                                                .getItem(selected.keyAt(i));
                                        favoriteListAdapter.remove(selectedItem);

                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mDbHelper.updateStar(selectedItem, false);
                                            }
                                        }).start();
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
                final int checkedCount = favoriteList.getCheckedItemCount();
                mode.setTitle(checkedCount+"");
                favoriteListAdapter.toggleSelection(position);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_clear_favorite_icon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_clear) {
            clearFavorites();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume - 0");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = mDbHelper.searchAllFavorite();
                if(cursor != null){
                    if (myList != null) {
                        myList.clear();
                    }
                    if(cursor.getCount() > 0){
                        cursor.moveToFirst();
                        do {
                            myList.add(cursor.getString(cursor.getColumnIndex("word")));
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }

                //Log.d(TAG, "onResume - sendEmptyMessage");
                handler.sendEmptyMessage(0);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDbHelper != null) {
            mDbHelper.close();
        }
    }

    private void clearFavorites(){
        if(myList.isEmpty()){
            Toast.makeText(FavoriteActivity.this, R.string.no_favorite_to_clear, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(
                FavoriteActivity.this);
        builder.setMessage(R.string.clear_all_favorite_confirmation);
        builder.setNegativeButton(R.string.dialog_option_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton(R.string.dialog_option_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (String favorite : myList) {
                            //Log.d(TAG, "favorite = " + favorite);
                            mDbHelper.updateStar(favorite, false);
                        }
                    }
                }).start();

                myList.clear();
                emptyView.setVisibility(View.VISIBLE);
                divider.setVisibility(View.GONE);
                favoriteListAdapter.notifyDataSetChanged();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
