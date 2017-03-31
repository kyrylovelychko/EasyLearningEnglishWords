package com.velychko.kyrylo.mydictionaries.data.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Dictionaries.db";
    public static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы Words при создании базы данных
        final String CREATE_DICTIONARIES_TABLE =
                "CREATE TABLE " + DatabaseDescription.Dictionaries.TABLE_NAME + "(" +
                        DatabaseDescription.Dictionaries._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DatabaseDescription.Dictionaries.COLUMN_NAME + " TEXT, " +
                        DatabaseDescription.Dictionaries.COLUMN_DATE_OF_CHANGE + " INTEGER);";
        db.execSQL(CREATE_DICTIONARIES_TABLE);

        // Создание таблицы Dictionaries при создании базы данных
        final String CREATE_WORDS_TABLE =
                "CREATE TABLE " + DatabaseDescription.Words.TABLE_NAME + "(" +
                        DatabaseDescription.Words._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DatabaseDescription.Words.COLUMN_EN + " TEXT, " +
                        DatabaseDescription.Words.COLUMN_RU + " TEXT, " +
                        DatabaseDescription.Words.COLUMN_FROM_EN_TO_RU + " INTEGER, " +
                        DatabaseDescription.Words.COLUMN_DICTIONARY + " TEXT, " +
                        DatabaseDescription.Words.COLUMN_DATE_OF_CHANGE + " INTEGER);";
        db.execSQL(CREATE_WORDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
