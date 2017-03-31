package com.velychko.kyrylo.mydictionaries.data.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription.*;

import java.util.ArrayList;

public class DatabaseMaster {

    // Поля класса:
    private SQLiteDatabase database;
    private DatabaseHelper helper;
    private static DatabaseMaster instance;

    // Конструктор
    private DatabaseMaster(Context context) {
        helper = new DatabaseHelper(context);
        if (database == null || !database.isOpen()) {
            database = helper.getWritableDatabase();
        }
    }

    // Получаем экземпляр DatabaseMaster
    public static DatabaseMaster getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseMaster(context);
        }
        return instance;
    }

    // Получение словарей
    public Cursor getAllDictionaries(String table, String[] columns, String selection,
                                     String[] selectionArgs, String groupBy, String having,
                                     String orderBy) {
        Cursor cursor =
                database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        return cursor;
    }

    // Получение всех словарей по убыванию даты обновления
    public Cursor getAllDictionaries() {
        return getAllDictionaries(Dictionaries.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                Dictionaries.COLUMN_DATE_OF_CHANGE + " COLLATE NOCASE DESC");
    }

    // Обновление даты последнего изменения словаря по имени словаря
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

    // Получение всех слов из словарей, выбранных пользователем. Выбираются только те слова,
    // у которых заполнены перевод и на английский и на русский
    public Cursor getOnlyFullWordsFromSelectedDictionaries(ArrayList<String> namesOfDictionaries) {
        // Присоединяем в начале и в конце каждого названия словаря двойные кавычки
        for (int i = 0; i < namesOfDictionaries.size(); i++) {
            namesOfDictionaries.set(i, "\"" + namesOfDictionaries.get(i) + "\"");
        }

        // Формирование SQL-запроса для получения всех слов из словарей, выбранных пользователем
        String sqlQuery = "SELECT * FROM " + Words.TABLE_NAME + " WHERE " + Words.COLUMN_DICTIONARY
                + " IN (" + TextUtils.join(", ", namesOfDictionaries) + ") AND " + Words.COLUMN_EN +
                " <> \"\" AND " + Words.COLUMN_RU + " <> \"\"" + " ORDER BY "
                + Words.COLUMN_DATE_OF_CHANGE + " COLLATE NOCASE DESC";

        Cursor cursor =  database.rawQuery(sqlQuery, null);

        return cursor;
    }
}
