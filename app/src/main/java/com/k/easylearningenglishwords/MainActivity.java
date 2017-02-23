package com.k.easylearningenglishwords;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.k.easylearningenglishwords.data.DatabaseDescription;
import com.k.easylearningenglishwords.data.DatabaseHelper;
import com.k.easylearningenglishwords.fragments.AddEditWordFragment;
import com.k.easylearningenglishwords.fragments.DictionariesListFragment;
import com.k.easylearningenglishwords.fragments.DictionaryFragment;
import com.k.easylearningenglishwords.fragments.WordDetailsFragment;
import com.k.easylearningenglishwords.fragments.dialogs.AddDictionaryDialog;

public class MainActivity
        extends AppCompatActivity
        implements DictionariesListFragment.DictionariesListFragmentListener,
        DictionaryFragment.DictionaryFragmentListener,
        WordDetailsFragment.WordDetailsFragmentListener,
        AddEditWordFragment.AddEditWordFragmentListener {

    // Ключ для сохранения Uri словаря в переданном объекте Bundle
    public static final String DICTIONARY_URI = "dictionary_uri";
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

//        if (savedInstanceState != null) {
            dictionariesListFragment = new DictionariesListFragment();

            // Добавление фрагмента в FrameLayout
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragmentContainer, dictionariesListFragment);
            transaction.commit();
//        }
    }


    @Override
    public void onDictionarySelected(Uri dictionaryUri) {
        DictionaryFragment dictionaryFragment = new DictionaryFragment();

        // Передача URI словаря в аргументе dictionaryFragment
        Bundle arguments = new Bundle();
        arguments.putParcelable(DICTIONARY_URI, dictionaryUri);
        dictionaryFragment.setArguments(arguments);

        // Использование FragmentTransaction для отображения
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, dictionaryFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // Приводит к отображению DictionaryFragment
        setTitle("Словарь");
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
    public void onWordSelected(Uri wordUri) {
        WordDetailsFragment wordDetailsFragment = new WordDetailsFragment();

        // Передача URI словаря в аргументе wordDetailsFragment
        Bundle arguments = new Bundle();
        arguments.putParcelable(WORD_URI, wordUri);
        wordDetailsFragment.setArguments(arguments);

        // Использование FragmentTransaction для отображения
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentDictionary, wordDetailsFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // Приводит к отображению DictionaryFragment
        setTitle("Слово");
    }

    @Override
    public void onAddWord() {
        displayAddEditWordFragment(null);
    }

    @Override
    public void onWordEdited(Uri wordUri) {
        displayAddEditWordFragment(wordUri);
    }

    @Override
    public void onWordDeleted() {

    }

    private void displayAddEditWordFragment(Uri wordUri) {
        AddEditWordFragment addEditWordFragment = new AddEditWordFragment();

        // Передача URI словаря в аргументе addEditWordFragment
        Bundle arguments = new Bundle();
        arguments.putParcelable(WORD_URI, wordUri);
        addEditWordFragment.setArguments(arguments);

        // Использование FragmentTransaction для отображения
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentWordDetails, addEditWordFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // Приводит к отображению DictionaryFragment
    }

    @Override
    public void onAddEditCompleted(Uri wordUri) {

    }

    private void checkDB (){
        final String TAG = "TESTMYBD";

        SQLiteDatabase database = new DatabaseHelper(this).getWritableDatabase();
        Cursor cursor = database.query(DatabaseDescription.Dictionaries.TABLE_NAME, null, null, null, null, null, null);

        if (cursor.moveToFirst()){
            int indexId = cursor.getColumnIndex(DatabaseDescription.Dictionaries._ID);
            int indexName = cursor.getColumnIndex(DatabaseDescription.Dictionaries.COLUMN_NAME);
            int index = cursor.getColumnIndex(DatabaseDescription.Dictionaries.COLUMN_DATE_OF_CHANGE);

            do {
                Log.d(TAG, "ID = " + cursor.getInt(indexId) +
                        ", Name = " + cursor.getString(indexName) +
                        ", Date = " + cursor.getInt(index));
            } while (cursor.moveToNext());
        } else {
            Log.d(TAG, "0 rows");
        }
    }
}
