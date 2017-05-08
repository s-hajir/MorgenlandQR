package com.example.hajir.morgenlandqr;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hajir on 08.05.2017.
 */

public class DbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME ="morgenland.db";
    public static final int DB_VERSION =1;
    public static final String TABLE_QRText ="QRText";
    public static final String TABLE_Datum ="Datum";
    public static final String COLUMN_ID ="id" ;
    public static final String COLUMN_QRText ="qrText" ;
    public static final String COLUMN_TIMESTAMP ="timestamp" ;
    public static final String COLUMN_DATUM ="datum" ;

    public static final String SQL_CREATE = "CREATE TABLE "+TABLE_QRText+"("+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +COLUMN_QRText+" TEXT NOT NULL, "+COLUMN_TIMESTAMP+" TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "+COLUMN_DATUM+" TEXT NOT NULL); ";

    public static final String SQL_CREATE2 = "CREATE TABLE "+TABLE_Datum+"("
            +COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +COLUMN_DATUM+" TEXT NOT NULL); ";


    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
        db.execSQL(SQL_CREATE2);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
