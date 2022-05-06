package com.mobile.security;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LoginDatabase extends SQLiteOpenHelper {

    /* private static final String TABLE_LOGIN = "LOGIN";
	 private static final String KEY_ID = "id";
	    private static final String KEY_PASSWORD = "Password";
	    private static final String KEY_REPASSWORD = "Repassword";*/
    // TODO: Create public field for each column in your table.
    // SQL Statement to create a new database.


    // User Table Columns names
    private static final String TAG = "LoginDatabase";
    private static final String PK = "ID";
    private static final String TABLE_NAME = "TB_USERID";
    private static final String USERID = "USERID";
    private static final String LOGIN_STATUS = "LOGIN_STATUS";

    public LoginDatabase(Context context){
        super(context, TABLE_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create table sql query
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + USERID + " TEXT, " + LOGIN_STATUS + " TEXT)";
        db.execSQL(createTable);
    }

    // upgrade table sql query
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //Drop User Table if exist
        db.execSQL(String.format("DROP IF TABLE EXISTS %s", TABLE_NAME));
        // Create tables again
        onCreate(db);
    }

    //This method is to create data record
    public boolean addData(String item1, String item2){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USERID, item1);
        contentValues.put(LOGIN_STATUS, item2);

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }


    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + PK + " DESC LIMIT 1";
        Cursor data = db.rawQuery(query,null);
        return data;
    }

}
