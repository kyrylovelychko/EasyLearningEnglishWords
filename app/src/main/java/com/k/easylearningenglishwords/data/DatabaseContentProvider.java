package com.k.easylearningenglishwords.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.data.DatabaseDescription.Dictionaries;
import com.k.easylearningenglishwords.data.DatabaseDescription.Words;

public class DatabaseContentProvider extends ContentProvider {

    // Используется для обращения к базе данных
    private DatabaseHelper dbHelper;

    // UriMatcher помогает ContentProvider определить выполняемую операцию
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Константы, используемые для определения выполняемой операции
    private static final int ONE_DICTIONARY = 101; // Одно слово
    private static final int DICTIONARIES = 102; // Таблица слов
    private static final int ONE_WORD = 201; // Одно слово
    private static final int WORDS = 202; // Таблица слов

    static {
        // Uri для словаря с заданным идентификатором
        uriMatcher.addURI(DatabaseDescription.AUTHORITY, Dictionaries.TABLE_NAME + "/#", ONE_DICTIONARY);

        // Uri для всех записей таблицы словарей
        uriMatcher.addURI(DatabaseDescription.AUTHORITY, Dictionaries.TABLE_NAME, DICTIONARIES);

        // Uri для слова с заданным идентификатором
        uriMatcher.addURI(DatabaseDescription.AUTHORITY, Words.TABLE_NAME + "/#", ONE_WORD);

        // Uri для всех записей таблицы слов
        uriMatcher.addURI(DatabaseDescription.AUTHORITY, Words.TABLE_NAME, WORDS);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }


        @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(Dictionaries.TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case ONE_DICTIONARY:
                queryBuilder.appendWhere(Dictionaries._ID + "=" + uri.getLastPathSegment());
                break;
            case DICTIONARIES:
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.invalid_query_uri) + uri);
        }

        Cursor cursor = queryBuilder.query(dbHelper.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri newUri = null;
        long rowId = 0;

        switch (uriMatcher.match(uri)) {
            case ONE_WORD:
                rowId = dbHelper.getWritableDatabase().insert(Words.TABLE_NAME, null, values);
                if (rowId > 0) {
                    newUri = Words.buildWordsUri(rowId);

                    getContext().getContentResolver().notifyChange(uri, null);
                } else {
                    throw new SQLException(getContext().getString(R.string.insert_failed) + uri);
                }
                break;
            case DICTIONARIES:
                rowId = dbHelper.getWritableDatabase().insert(Dictionaries.TABLE_NAME, null, values);
                if (rowId > 0) {
                    newUri = Dictionaries.buildDictionariesUri(rowId);

                    getContext().getContentResolver().notifyChange(uri, null);
                } else {
                    throw new SQLException(getContext().getString(R.string.insert_failed) + uri);
                }
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.invalid_insert_uri) + uri);
        }

        return newUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int numberOfRowsDeleted;

        switch (uriMatcher.match(uri)){
            case ONE_WORD:
                String id = uri.getLastPathSegment();
                numberOfRowsDeleted = dbHelper.getWritableDatabase().delete(Words.TABLE_NAME,
                        Words._ID + "=" + id, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.invalid_delete_uri) + uri);
        }

        if (numberOfRowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numberOfRowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int numberOfRowsUpdated; // 1, если обновление успешно; 0 при неудаче

        switch (uriMatcher.match(uri)) {
            case ONE_WORD:
                String id = uri.getLastPathSegment();
                numberOfRowsUpdated = dbHelper.getWritableDatabase().update(Words.TABLE_NAME,
                        values, Words._ID + "=" + id, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.invalid_update_uri) + uri);
        }

        if (numberOfRowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numberOfRowsUpdated;
    }
}
