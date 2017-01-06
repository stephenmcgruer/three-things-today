package com.stephenmcgruer.threethingstoday.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.stephenmcgruer.threethingstoday.database.ThreeThingsContract.ThreeThingsEntry;

public class ThreeThingsDbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ThreeThingsEntry.TABLE_NAME + " (" +
                    ThreeThingsEntry.COLUMN_NAME_YEAR + " INTEGER," +
                    ThreeThingsEntry.COLUMN_NAME_MONTH + " INTEGER," +
                    ThreeThingsEntry.COLUMN_NAME_DAY_OF_MONTH + " INTEGER," +
                    ThreeThingsEntry.COLUMN_NAME_FIRST_THING + " TEXT," +
                    ThreeThingsEntry.COLUMN_NAME_SECOND_THING + " TEXT," +
                    ThreeThingsEntry.COLUMN_NAME_THIRD_THING + " TEXT," +
                    "PRIMARY KEY (" +
                        ThreeThingsEntry.COLUMN_NAME_YEAR + ", " +
                        ThreeThingsEntry.COLUMN_NAME_MONTH + ", " +
                        ThreeThingsEntry.COLUMN_NAME_DAY_OF_MONTH +
                    ")" +
            ")";

    public static final String DATABASE_NAME = "ThreeThings.db";
    public static final int DATABASE_VERSION = 1;

    public ThreeThingsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO(smcgruer): Implement.
        throw new UnsupportedOperationException("Database upgrades not supported");
    }
}
