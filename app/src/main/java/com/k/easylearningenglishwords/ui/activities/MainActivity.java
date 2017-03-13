package com.k.easylearningenglishwords.ui.activities;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.data.SQLite.DatabaseDescription;
import com.k.easylearningenglishwords.data.SQLite.DatabaseDescription.Dictionaries;
import com.k.easylearningenglishwords.data.SQLite.DatabaseHelper;
import com.k.easylearningenglishwords.ui.fragments.AddEditWordFragment;
import com.k.easylearningenglishwords.ui.fragments.DictionariesListFragment;
import com.k.easylearningenglishwords.ui.fragments.DictionaryFragment;
import com.k.easylearningenglishwords.ui.fragments.WordDetailsFragment;
import com.k.easylearningenglishwords.ui.fragments.dialogs.AddDictionaryDialog;
import com.k.easylearningenglishwords.ui.fragments.dialogs.DeleteDictionaryDialog;
import com.k.easylearningenglishwords.ui.fragments.dialogs.DeleteWordDialog;
import com.k.easylearningenglishwords.ui.fragments.dialogs.RenameDictionaryDialog;

import java.util.Date;

public class MainActivity
        extends AppCompatActivity
        implements DictionariesListFragment.DictionariesListFragmentListener,
        DictionaryFragment.DictionaryFragmentListener,
        WordDetailsFragment.WordDetailsFragmentListener,
        AddEditWordFragment.AddEditWordFragmentListener,
        DeleteWordDialog.DeleteWordDialogListener,
        RenameDictionaryDialog.RenameDictionaryDialogListener {

    // Ключ для сохранения Uri словаря в переданном объекте Bundle
    public static final String DICTIONARY_URI = "dictionary_uri";
    public static final String DICTIONARY_NAME = "dictionary_name";
    // Ключ для сохранения Uri слова в переданном объекте Bundle
    public static final String WORD_URI = "word_uri";

    // Вывод списка контактов
    private DictionariesListFragment dictionariesListFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_my_dictionaries);

        checkDB();

        //  Вывод фрагмента со списком словарей
        if (savedInstanceState == null) {
            dictionariesListFragment = new DictionariesListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragmentContainer, dictionariesListFragment);
            transaction.commit();
        }
    }

    @Override
    public void onAddDictionary() {
        // Вызов диалога добавления нового словаря
        new AddDictionaryDialog().show(getSupportFragmentManager(), "add dictionary");
    }


    @Override
    public void onSelectDictionary(Uri dictionaryUri) {
        // Вывод фрагмента со словами конкретного словаря
        // Uri словаря передаем параметром
        DictionaryFragment dictionaryFragment = new DictionaryFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(DICTIONARY_URI, dictionaryUri);
        dictionaryFragment.setArguments(arguments);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, dictionaryFragment);
        transaction.addToBackStack("MyStack");
        transaction.commit();
    }

    @Override
    public void onRenameDictionary(String dictionaryName) {
        // Вызов диалога переименования словаря
        // Имя словаря передаем параметром
        RenameDictionaryDialog dialog = new RenameDictionaryDialog();
        Bundle arguments = new Bundle();
        arguments.putString(DICTIONARY_NAME, dictionaryName);
        dialog.setArguments(arguments);
        dialog.show(getSupportFragmentManager(), "rename dictionary");
    }

    @Override
    public void onDeleteDictionary(String dictionaryName) {
        // Вызов диалога для подтверждения удаления словаря. Удаление словаря
        // Имя словаря передаем параметром
        DeleteDictionaryDialog dialog = new DeleteDictionaryDialog();
        Bundle arguments = new Bundle();
        arguments.putString(DICTIONARY_NAME, dictionaryName);
        dialog.setArguments(arguments);
        dialog.show(getSupportFragmentManager(), "delete dictionary");
    }

    @Override
    public void onAddWord(String dictionaryName) {
        // Вывод фрагмента добавления/редактирования слова
        displayAddEditWordFragment(null, dictionaryName);
    }

    @Override
    public void onSelectWord(Uri wordUri) {
        // Вывод фрагмента с деталями конкретного слова
        // Uri слова передаем параметром
        WordDetailsFragment wordDetailsFragment = new WordDetailsFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(WORD_URI, wordUri);
        wordDetailsFragment.setArguments(arguments);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, wordDetailsFragment);
        transaction.addToBackStack("MyStack");
        transaction.commit(); // Приводит к отображению DictionaryFragment
    }

    @Override
    public void onEditWord(Uri wordUri) {
        // Вывод фрагмента добавления/редактирования слова
        displayAddEditWordFragment(wordUri, null);
    }

    @Override
    public void onDeleteWord(Uri wordUri) {
        DeleteWordDialog dialog = new DeleteWordDialog();
        Bundle arguments = new Bundle();
        arguments.putParcelable(WORD_URI, wordUri);
        dialog.setArguments(arguments);
        dialog.show(getSupportFragmentManager(), "delete word");
    }

    private void displayAddEditWordFragment(Uri wordUri, String dictionaryName) {
        // Вывод фрагмента добавления/редактирования слова
        // Если редактирование слова - Uri слова передаем параметром
        // Если добавление нового слова - Uri будет содержать null
        AddEditWordFragment addEditWordFragment = new AddEditWordFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(WORD_URI, wordUri);
        arguments.putString(DICTIONARY_NAME, dictionaryName);
        addEditWordFragment.setArguments(arguments);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, addEditWordFragment);
        transaction.addToBackStack("MyStack");
        transaction.commit();
    }

    @Override
    public void onAddEditWordCompleted(Uri wordUri, String dictionaryName) {
        updateDateOfChangeDictionary(dictionaryName);
    }

    // Обновление даты последнего изменения для словаря в таблице словарвей
    @Override
    public void updateDateOfChangeDictionary(String dictionaryName) {
        // Получаем _ID словаря по имени
        Cursor cursor = new DatabaseHelper(this).getReadableDatabase().query(
                Dictionaries.TABLE_NAME,
                null,
                Dictionaries.COLUMN_NAME + "=?",
                new String[]{dictionaryName},
                null,
                null,
                null);
        if (cursor.getCount() > 0) {
            // Обновляем дату последнего изменения словаря по Uri словаря
            cursor.moveToFirst();
            int id = cursor.getInt(cursor.getColumnIndex(Dictionaries._ID));
            Uri dictionaryUri = Dictionaries.buildDictionariesUri(id);
            ContentValues cv = new ContentValues();
//            cv.put(Dictionaries._ID, id);
//            cv.put(Dictionaries.COLUMN_NAME, dictionaryName);
            cv.put(Dictionaries.COLUMN_DATE_OF_CHANGE, new Date().getTime() / 1000);
            int updatedRows = getContentResolver().update(
                    dictionaryUri,
                    cv,
                    null,
                    null);
            if (updatedRows < 0) {
                throw new SQLException("" + R.string.exc_invalid_update_uri + dictionaryUri);
            }
        }
    }

    private void checkDB() {
        final String TAG = "TESTMYBD";

        SQLiteDatabase database = new DatabaseHelper(this).getWritableDatabase();
        Cursor cursor_1 = database.query(DatabaseDescription.Dictionaries.TABLE_NAME, null, null, null, null, null, null);

        if (cursor_1.moveToFirst()) {
            do {
                Log.d(TAG, "ID = " + cursor_1.getInt(cursor_1.getColumnIndex(DatabaseDescription.Dictionaries._ID)) +
                        ", Name = " + cursor_1.getString(cursor_1.getColumnIndex(DatabaseDescription.Dictionaries.COLUMN_NAME)) +
                        ", Date = " + cursor_1.getInt(cursor_1.getColumnIndex(DatabaseDescription.Dictionaries.COLUMN_DATE_OF_CHANGE)));
            } while (cursor_1.moveToNext());
        } else {
            Log.d(TAG, "0 rows");
        }

        Cursor cursor = database.query(DatabaseDescription.Words.TABLE_NAME, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Log.d(TAG, "ID = " + cursor.getInt(cursor.getColumnIndex(DatabaseDescription.Words._ID)) +
                        ", EN = " + cursor.getString(cursor.getColumnIndex(DatabaseDescription.Words.COLUMN_EN)) +
                        ", RU = " + cursor.getString(cursor.getColumnIndex(DatabaseDescription.Words.COLUMN_RU)) +
                        ", FROM_EN_TO_RU = " + cursor.getString(cursor.getColumnIndex(DatabaseDescription.Words.COLUMN_FROM_EN_TO_RU)) +
                        ", Dictionary = " + cursor.getString(cursor.getColumnIndex(DatabaseDescription.Words.COLUMN_DICTIONARY)) +
                        ", Date = " + cursor.getInt(cursor.getColumnIndex(DatabaseDescription.Words.COLUMN_DATE_OF_CHANGE)));
            } while (cursor.moveToNext());
        } else {
            Log.d(TAG, "0 rows");
        }
    }

}
