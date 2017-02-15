package com.k.easylearningenglishwords.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.k.easylearningenglishwords.data.DatabaseDescription.Words;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Dictionaries.db";
    public static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Создание таблицы Words при создании базы данных
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_WORDS_TABLE =
                "CREATE TABLE " + Words.TABLE_NAME + "(" +
                        Words._ID + " INTEGER PRIMARY KEY, " +
                        Words.COLUMN_EN + " TEXT, " +
                        Words.COLUMN_RU + " TEXT, " +
                        Words.COLUMN_DICTIONARY + " TEXT);";
        db.execSQL(CREATE_WORDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
