package com.velychko.kyrylo.mydictionaries.data.sqlite;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class DatabaseDescription {

    // Имя ContentProvider: обычно совпадает с именем пакета
    public static final String AUTHORITY = "com.velychko.kyrylo.mydictionaries.data";

    // Базовый URI для взаимодействия с ContentProvider
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);


    //Таблица со словарями
    public static final class Dictionaries implements BaseColumns {
        public static final String TABLE_NAME = "dictionaries";

        // Объект Uri для таблицы dictionaries
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        // Имена столбцов таблицы
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DATE_OF_CHANGE = "date_of_change";


        // Создание Uri для конкретного слова
        public static Uri buildDictionariesUri (long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


    //Таблица со словами
    public static final class Words implements BaseColumns {
        public static final int FROM_EN_TO_RU_TRUE = 1;
        public static final int FROM_EN_TO_RU_FALSE = 0;

        public static final String TABLE_NAME = "words";

        // Объект Uri для таблицы words
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        // Имена столбцов таблицы
        public static final String COLUMN_EN = "en";
        public static final String COLUMN_RU = "ru";
        public static final String COLUMN_FROM_EN_TO_RU = "from_en_to_ru";
        public static final String COLUMN_DICTIONARY = "dictionary";
        public static final String COLUMN_DATE_OF_CHANGE = "date_of_change";


        // Создание Uri для конкретного слова
        public static Uri buildWordsUri (long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
