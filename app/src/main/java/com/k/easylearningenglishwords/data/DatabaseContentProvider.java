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
import com.k.easylearningenglishwords.data.DatabaseDescription.*;

public class DatabaseContentProvider extends ContentProvider {

    // Используется для обращения к базе данных
    private DatabaseHelper dbHelper;

    // UriMatcher помогает ContentProvider определить выполняемую операцию
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Константы, используемые для определения выполняемой операции
    private static final int ONE_WORD = 1; // Одно слово
    private static final int WORDS = 2; // Таблица слов

    static {
        // Uri для контакта с заданным идентификатором
        uriMatcher.addURI(DatabaseDescription.AUTHORITY, Words.TABLE_NAME + "/#", ONE_WORD);

        // Uri для таблицы
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
        queryBuilder.setTables(Words.TABLE_NAME);

        switch (uriMatcher.match(uri)){
            case ONE_WORD:
                queryBuilder.appendWhere(Words._ID + "=" + uri.getLastPathSegment());
                break;
            case WORDS:
                break;
            default:
                throw new UnsupportedOperationException(getContext().getString(R.string.invalid_query_uri) + uri);
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
        Uri newContactUri = null;

        switch (uriMatcher.match(uri)){
            case WORDS:
                long rowId = dbHelper.getWritableDatabase().insert(Words.TABLE_NAME, null, values);
                if (rowId > 0) {
                    newContactUri = Words.buildContactUri(rowId);

                    getContext().getContentResolver().notifyChange(uri, null);
                } else {
                    throw new SQLException(getContext().getString(R.string.insert_failed) + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException(getContext().getString(R.string.invalid_insert_uri) + uri);
        }

        return newContactUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
