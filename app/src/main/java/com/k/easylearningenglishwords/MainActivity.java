package com.k.easylearningenglishwords;

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

import com.k.easylearningenglishwords.data.DatabaseDescription;
import com.k.easylearningenglishwords.data.DatabaseDescription.Dictionaries;
import com.k.easylearningenglishwords.data.DatabaseHelper;
import com.k.easylearningenglishwords.fragments.AddEditWordFragment;
import com.k.easylearningenglishwords.fragments.DictionariesListFragment;
import com.k.easylearningenglishwords.fragments.DictionaryFragment;
import com.k.easylearningenglishwords.fragments.WordDetailsFragment;
import com.k.easylearningenglishwords.fragments.dialogs.AddDictionaryDialog;
import com.k.easylearningenglishwords.fragments.dialogs.DeleteDictionaryDialog;
import com.k.easylearningenglishwords.fragments.dialogs.DeleteWordDialog;
import com.k.easylearningenglishwords.fragments.dialogs.RenameDictionaryDialog;

import java.util.Date;

public class MainActivity
        extends AppCompatActivity
        implements DictionariesListFragment.DictionariesListFragmentListener,
        DictionaryFragment.DictionaryFragmentListener,
        WordDetailsFragment.WordDetailsFragmentListener,
        AddEditWordFragment.AddEditWordFragmentListener,
        DeleteWordDialog.DeleteWordDialogListener,
        RenameDictionaryDialog.RenameDictionaryDialoglistener
{

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
        setTitle("Мои словари");

        checkDB();

        if (savedInstanceState == null) {
            dictionariesListFragment = new DictionariesListFragment();

            // Добавление фрагмента в FrameLayout
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragmentContainer, dictionariesListFragment);
            transaction.commit();
        }
    }

    @Override
    public void onAddDictionary() {
        new AddDictionaryDialog().show(getSupportFragmentManager(), "add dictionary");
//        DialogFragment dialog = new DialogFragment(){
//            EditText newDictionaryName;
//            private CoordinatorLayout coordinatorLayout;
//
//            @NonNull
//            @Override
//            public Dialog onCreateDialog(Bundle savedInstanceState) {
//                coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinatorLayout);
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_dictionary, null);
//                builder.setTitle(R.string.add_dictionary_request)
//                        .setView(view)
//                        .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//
//                            }
//                        })
//                        .setPositiveButton(R.string.button_add, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                newDictionaryName = (EditText) getView().findViewById(R.id.dictionaryNameEditText);
//
//                                ContentValues cv = new ContentValues();
//                                cv.put(DatabaseDescription.Dictionaries.COLUMN_NAME, newDictionaryName.getText().toString());
//                                cv.put(DatabaseDescription.Dictionaries.COLUMN_EDIT_TIME, new Date().getTime());
//                                Uri newDictionaryUri = getContentResolver().insert(DatabaseDescription.Dictionaries.CONTENT_URI, cv);
//                                if (newDictionaryUri != null){
//                                    Snackbar.make(coordinatorLayout, R.string.dictionary_added, Snackbar.LENGTH_LONG).show();
//                                } else {
//                                    Snackbar.make(coordinatorLayout, R.string.dictionary_not_added, Snackbar.LENGTH_LONG).show();
//                                }
//                            }
//                        })
//                        .setCancelable(false);
//                return builder.create();
//            }
//
//            @Override
//            public void onActivityCreated(Bundle savedInstanceState) {
//                super.onActivityCreated(savedInstanceState);
//                getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//            }
//        };
//        dialog.show(getSupportFragmentManager(), "add dictionary");
//        dictionariesListFragment.updateDictionariesList();
    }

    @Override
    public void onSelectDictionary(Uri dictionaryUri) {
        DictionaryFragment dictionaryFragment = new DictionaryFragment();

        // Передача URI словаря в аргументе dictionaryFragment
        Bundle arguments = new Bundle();
        arguments.putParcelable(DICTIONARY_URI, dictionaryUri);
        dictionaryFragment.setArguments(arguments);

        // Использование FragmentTransaction для отображения
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, dictionaryFragment);
        transaction.addToBackStack("MyStack");
        transaction.commit(); // Приводит к отображению DictionaryFragment
    }

    @Override
    public void onRenameDictionary(String dictionaryName) {
        RenameDictionaryDialog dialog = new RenameDictionaryDialog();
        Bundle arguments = new Bundle();
        arguments.putString(DICTIONARY_NAME, dictionaryName);
        dialog.setArguments(arguments);
        dialog.show(getSupportFragmentManager(), "rename dictionary");
    }

    @Override
    public void onDeleteDictionary(String dictionaryName) {
        DeleteDictionaryDialog dialog = new DeleteDictionaryDialog();
        Bundle arguments = new Bundle();
        arguments.putString(DICTIONARY_NAME, dictionaryName);
        dialog.setArguments(arguments);
        dialog.show(getSupportFragmentManager(), "delete dictionary");
    }

    @Override
    public void onAddWord(int rIdFragmentFrom) {
        displayAddEditWordFragment(null, rIdFragmentFrom);
    }

    @Override
    public void onSelectWord(Uri wordUri) {
        WordDetailsFragment wordDetailsFragment = new WordDetailsFragment();

        // Передача URI словаря в аргументе wordDetailsFragment
        Bundle arguments = new Bundle();
        arguments.putParcelable(WORD_URI, wordUri);
        wordDetailsFragment.setArguments(arguments);

        // Использование FragmentTransaction для отображения
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, wordDetailsFragment);
        transaction.addToBackStack("MyStack");
        transaction.commit(); // Приводит к отображению DictionaryFragment
    }

    @Override
    public void onEditWord(Uri wordUri, int rIdFragmentFrom) {
        displayAddEditWordFragment(wordUri, rIdFragmentFrom);
    }

    @Override
    public void onDeleteWord(Uri wordUri) {
        DeleteWordDialog dialog = new DeleteWordDialog();
        Bundle arguments = new Bundle();
        arguments.putParcelable(WORD_URI, wordUri);
        dialog.setArguments(arguments);
        dialog.show(getSupportFragmentManager(), "delete word");
    }

    private void displayAddEditWordFragment(Uri wordUri, int rIdFragmentFrom) {
        AddEditWordFragment addEditWordFragment = new AddEditWordFragment();

        // Передача URI словаря в аргументе addEditWordFragment
        Bundle arguments = new Bundle();
        arguments.putParcelable(WORD_URI, wordUri);
        addEditWordFragment.setArguments(arguments);

        // Использование FragmentTransaction для отображения
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(rIdFragmentFrom, addEditWordFragment);
        transaction.addToBackStack("MyStack");
        transaction.commit();
    }

    @Override
    public void onAddEditWordCompleted(Uri wordUri, String dictionaryName) {
//        if (addingNewWord){
//            updateDateOfChangeDictionary(dictionaryName);
//        } else {
//            updateDateOfChangeWord(wordUri);
        updateDateOfChangeDictionary(dictionaryName);
//        }
        getContentResolver().notifyChange(Dictionaries.CONTENT_URI, null);
    }

//    public void updateDateOfChangeWord(Uri wordUri){
//        String updateSQL = "UPDATE " + Words.TABLE_NAME +
//                " SET " + Words.COLUMN_DATE_OF_CHANGE + "=" + new Date().getTime() / 1000 +
//                " WHERE " + Words._ID + "=" + wordUri.getLastPathSegment();
//        new DatabaseHelper(this).getWritableDatabase().execSQL(updateSQL);
//    }

    @Override
    public void updateDateOfChangeDictionary(String dictionaryName) {
        Cursor cursor = new DatabaseHelper(this).getReadableDatabase().query(
                Dictionaries.TABLE_NAME,
                null,
                Dictionaries.COLUMN_NAME + "=?",
                new String[]{dictionaryName},
                null,
                null,
                null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int id = cursor.getInt(cursor.getColumnIndex(Dictionaries._ID));
            Uri dictionaryUri = Dictionaries.buildDictionariesUri(id);
            ContentValues cv = new ContentValues();
            cv.put(Dictionaries._ID, id);
            cv.put(Dictionaries.COLUMN_NAME, dictionaryName);
            cv.put(Dictionaries.COLUMN_DATE_OF_CHANGE, new Date().getTime() / 1000);
            int updatedRows = getContentResolver().update(dictionaryUri, cv, null, null);
            if (updatedRows < 0) {
                throw new SQLException(""+ R.string.invalid_update_uri + dictionaryUri);
            }
        }


//        String updateSQL = "UPDATE " + Dictionaries.TABLE_NAME +
//                " SET " + Dictionaries.COLUMN_DATE_OF_CHANGE + "=" + new Date().getTime() / 1000 +
//                " WHERE " + Dictionaries.COLUMN_NAME + "='" + dictionaryName + "'";
//        new DatabaseHelper(this).getWritableDatabase().execSQL(updateSQL);
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
