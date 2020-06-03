package com.helena.essorfrench;

import java.io.IOException;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DictDataHelper {
    private static final String TAG = "DictDataHelper";

    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseHelper mDbHelper;

    private static String SELECTION_ALL_WORD = "SELECT word FROM main WHERE id > 1 LIMIT %d OFFSET %d";
    private static String SELECTION_ALL_FAVORITE = "SELECT word FROM main WHERE star = 1";
    private static String SELECTION_SINGLE = "SELECT meaning, star FROM main WHERE word LIKE '%s%%' LIMIT 1";
    private static String SELECTION_HIGHLIGHT_SINGLE = "SELECT word, meaning, star FROM main " +
                                                        "WHERE word LIKE '%s%%' LIMIT 1";
    private static String SELECTION_WORD_LIST = "SELECT word FROM main WHERE (word LIKE '%s%%') " +
                                                "OR (word LIKE '-%s%%') " +
                                                "LIMIT %d OFFSET %d";

//    private static String SELECTION_ALL_OMIS = "SELECT meaning FROM main WHERE (meaning like '%～%')";

    public DictDataHelper(Context context) {
        this.mContext = context;
        mDbHelper = new DataBaseHelper(mContext);
    }

    public DictDataHelper createDatabase() throws SQLException {
        try {
            mDbHelper.createDataBase();
        } catch (IOException mIOException) {
            Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public boolean dictDataExists(){
        return mDbHelper.checkDataBase();
    }

    public DictDataHelper open() throws SQLException {
        try {
            mDbHelper.openDataBase();
            mDbHelper.close();
            mDb = mDbHelper.getWritableDatabase();
        } catch (SQLException mSQLException) {
            Log.e(TAG, "open >>" + mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }

    public void close() {
        if(mDbHelper != null) {
            mDbHelper.close();
        }
        if(mDb != null) {
            mDb.close();
        }
    }

    public Cursor searchAllWords(int queryOffset, int queryNumber){
        String sql = String.format(Locale.US, SELECTION_ALL_WORD,
                queryNumber, queryOffset);
        return rawDbQuery(sql);
    }

    public Cursor searchSingleWord(String query){
        if(query.isEmpty())
            return null;

        String qStr = handleSpecialChars(query);
        String sql = String.format(Locale.US, SELECTION_SINGLE, qStr);
        return rawDbQuery(sql);
    }

    public Cursor searchWordsList(String query, int queryOffset, int queryNumber) {
        if(query.isEmpty())
            return null;

        String qStr = handleSpecialChars(query);
        String sql = String.format(Locale.US, SELECTION_WORD_LIST,
                                        qStr, qStr, queryNumber, queryOffset);

        return rawDbQuery(sql);
    }

    public Cursor searchAllFavorite(){
        String sql = SELECTION_ALL_FAVORITE;
        return rawDbQuery(sql);
    }

    public void updateStar(String word, boolean bStar){
        ContentValues newValues = new ContentValues();
        newValues.put("star", bStar);
        String[] args = { word };
        mDb.update("main", newValues, "word=?", args);
    }

    private String handleSpecialChars(String query){
        String s;
        if(query.contains("'")){
            s = query.replaceAll("'", "''");
        }else{
            s = query;
        }

        return s;
    }

    private Cursor rawDbQuery(String sql){
        try {
            Cursor c = mDb.rawQuery(sql, null);
            if (c != null) {
                c.moveToFirst();
            }
            return c;
        } catch (SQLException mSQLException) {
            Log.e(TAG, "searchByInputText:" + mSQLException.toString());
            throw mSQLException;
        }
    }
}
