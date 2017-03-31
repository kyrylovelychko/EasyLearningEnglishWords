package com.velychko.kyrylo.mydictionaries.data.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription.Dictionaries;

public class DatabaseMaster {

    private SQLiteDatabase database;
    private DatabaseHelper helper;

    private static DatabaseMaster instance;

    private DatabaseMaster(Context context) {
        helper = new DatabaseHelper(context);
        if (database == null || !database.isOpen()) {
            database = helper.getWritableDatabase();
        }
    }

    public static DatabaseMaster getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseMaster(context);
        }

        return instance;
    }

    public Cursor getAllDictionaries(String table, String[] columns, String selection,
                                     String[] selectionArgs, String groupBy, String having,
                                     String orderBy) {
        Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        return cursor;
    }

    public Cursor getAllDictionaries() {
        return getAllDictionaries(Dictionaries.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                Dictionaries.COLUMN_DATE_OF_CHANGE + " COLLATE NOCASE DESC");
    }

    public Cursor updateDateOfChangeDictionary(String dictionaryName) {
        return database.query(
                Dictionaries.TABLE_NAME,
                null,
                Dictionaries.COLUMN_NAME + "=?",
                new String[]{dictionaryName},
                null,
                null,
                null);
    }
//
//    public Cursor getCountOfWordsInDictionary() {
//        Cursor cursor = database.query(Words.TABLE_NAME,
//                new String[]{Words.COLUMN_DICTIONARY + ", COUNT{_id} AS counter"},
//                null,
//                null,
//                Dictionaries.COLUMN_NAME,
//                null,
//                null
//                );
//        return cursor;
//    }

}
