package com.helena.essorfrench;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static String TAG = "DataBaseHelper";

    private static final String DB_NAME = "fccf.db";
    private static final int DB_VERSION = 1;
    private static String DB_ABSOLUTE_PATH = "";
    private SQLiteDatabase myDataBase;
    private Context mContext;

    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.mContext = context;

        //Log.d(TAG, "DataBaseHelper");

        DB_ABSOLUTE_PATH = mContext.getApplicationInfo().dataDir + "/databases/" + DB_NAME;
    }

    public void openDataBase() throws SQLException {
        //Log.d(TAG, "openDataBase");
        myDataBase = SQLiteDatabase.openDatabase(DB_ABSOLUTE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public void createDataBase() throws IOException {
        //Log.d(TAG, "createDataBase");
        if (!checkDataBase()) {
            getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    public boolean checkDataBase() {
        File dbFile = mContext.getDatabasePath(DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() throws IOException {
        //Log.d(TAG, "copyDataBase");
        InputStream is = mContext.getAssets().open(DB_NAME);
        OutputStream os = new FileOutputStream(DB_ABSOLUTE_PATH);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
        os.flush();
        is.close();
        os.close();
    }

    @Override
    public synchronized void close() {
        super.close();
        if (myDataBase != null)
            myDataBase.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Log.d(TAG, "onCreate");
        //db.execSQL(DB_ABSOLUTE_PATH);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.disableWriteAheadLogging();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO for upgrading DB
        if(oldVersion != newVersion ){
            mContext.deleteDatabase(DB_NAME);
            try {
                copyDataBase();
            } catch (IOException ie) {
                throw new Error("Error copying database");
            }
        }
    }
}