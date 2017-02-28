package com.k.easylearningenglishwords.fragments;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.k.easylearningenglishwords.MainActivity;
import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.data.DatabaseDescription.Dictionaries;
import com.k.easylearningenglishwords.data.DatabaseDescription.Words;
import com.k.easylearningenglishwords.fragments.dialogs.AddDictionaryDialog;

import java.util.Date;

public class AddEditWordFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface AddEditWordFragmentListener {

        //Вызывается при завершении редактирования слова
        void onAddEditWordCompleted(Uri wordUri, String dictionaryName);

        void updateDateOfChangeDictionary(String dictionaryName);
    }

    // Константа для идентификации Loader
    private static final int WORD_LOADER = 0;
    private static final int DICTIONARIES_LOADER = 1;

    private AddEditWordFragmentListener listener;// MainActivity
    private Uri wordUri;// Uri выбранного контакта
    private boolean addingNewWord = true;// Добавление (true) или изменение

    private TextInputLayout enTextInputLayout;
    private TextInputLayout ruTextInputLayout;
    private TextView dictionary;
    private String dictionaryName;
    private Button chooseDictionaryButton;
    String[] dictionariesNameArray;

    private int FROM_EN_TO_RU;

    private Cursor cursor;
    private int currentPositionInArray = 0;

    private FloatingActionButton saveWordFAB;
    private CoordinatorLayout coordinatorLayout;// Для SnackBar

    public AddEditWordFragment() {
        // Required empty public constructor
    }

    // Вызывается при создании представлений фрагмента
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);// У фрагмента есть команды меню

        // Заполнение GUI и получение ссылок на компоненты EditText
        View view = inflater.inflate(R.layout.fragment_add_edit_word, container, false);
        enTextInputLayout = (TextInputLayout) view.findViewById(R.id.enTextInputLayout);
        enTextInputLayout.getEditText().addTextChangedListener(enOrRuFilled);
        ruTextInputLayout = (TextInputLayout) view.findViewById(R.id.ruTextInputLayout);
        ruTextInputLayout.getEditText().addTextChangedListener(enOrRuFilled);
        dictionary = (TextView) view.findViewById(R.id.dictionary);
        chooseDictionaryButton = (Button) view.findViewById(R.id.chooseDictionaryButton);
        chooseDictionaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDictionaryDialog();
            }
        });

        // Назначение слушателя событий FloatingActionButton
        saveWordFAB = (FloatingActionButton) view.findViewById(R.id.saveWordFAB);
        // Реагирует на событие, генерируемое при сохранении контакта
        saveWordFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).
                        hideSoftInputFromWindow(getView().getWindowToken(), 0);
                saveWord();// Сохранение контакта в базе данных
                getFragmentManager().popBackStack();
            }
        });
        updateSaveButtonFAB();

        // Используется для отображения SnackBar с короткими сообщениями
        coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinatorLayout);

        Bundle arguments = getArguments();
        if (arguments != null) {
            wordUri = arguments.getParcelable(MainActivity.WORD_URI);
            if (wordUri != null) {
                addingNewWord = false;
            }
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (addingNewWord == false) {
            getLoaderManager().initLoader(WORD_LOADER, null, this);
        } else {
            getLoaderManager().initLoader(DICTIONARIES_LOADER, null, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wordUri == null) {
            getActivity().setTitle("Новое слово");
        } else {
            getActivity().setTitle("Редактирование слова");
        }
    }

    // Назначение AddEditWordFragmentListener при присоединении фрагмента
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (AddEditWordFragmentListener) context;
    }

    // Удаление AddEditWordFragmentListener при отсоединении фрагмента
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    // Обнаруживает изменения в тексте поля EditText, связанного
    // с nameTextInputLayout, для отображения или скрытия saveButtonFAB
    private final TextWatcher enOrRuFilled = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        // Вызывается при изменении текста в nameTextInputLayout
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateSaveButtonFAB();
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!enTextInputLayout.getEditText().getText().toString().equals("") &&
                    ruTextInputLayout.getEditText().getText().toString().equals("")) {
                FROM_EN_TO_RU = Words.FROM_EN_TO_RU_TRUE;
            } else if (enTextInputLayout.getEditText().getText().toString().equals("") &&
                    !ruTextInputLayout.getEditText().getText().toString().equals("")) {
                FROM_EN_TO_RU = Words.FROM_EN_TO_RU_FALSE;
            }
        }
    };

    // Кнопка saveButtonFAB видна, если введено слово на русском или на английском
    private void updateSaveButtonFAB() {
        String en = enTextInputLayout.getEditText().getText().toString();
        String ru = ruTextInputLayout.getEditText().getText().toString();

        if (en.trim().length() != 0 || ru.trim().length() != 0) {
            saveWordFAB.show();
        } else {
            saveWordFAB.hide();
        }
    }

    // Сохранение информации контакта в базе данных
    private void saveWord() {
        // Создание объекта ContentValues с парами "ключ—значение"
        ContentValues contentValues = new ContentValues();
        contentValues.put(Words.COLUMN_EN, enTextInputLayout.getEditText().getText().toString().trim());
        contentValues.put(Words.COLUMN_RU, ruTextInputLayout.getEditText().getText().toString().trim());
        contentValues.put(Words.COLUMN_FROM_EN_TO_RU, FROM_EN_TO_RU);
        contentValues.put(Words.COLUMN_DICTIONARY, dictionaryName);
        contentValues.put(Words.COLUMN_DATE_OF_CHANGE, new Date().getTime() / 1000);

        if (addingNewWord) {
            // Использовать объект ContentResolver активности для вызова
            // insert для объекта DatabaseContentProvider
            Uri newWordUri = getActivity().getContentResolver().insert(Words.CONTENT_URI, contentValues);
            if (newWordUri != null) {
                Snackbar.make(coordinatorLayout, R.string.word_added, Snackbar.LENGTH_LONG).show();
                listener.onAddEditWordCompleted(newWordUri, dictionaryName);
            } else {
                Snackbar.make(coordinatorLayout, R.string.word_not_added, Snackbar.LENGTH_LONG).show();
            }
        } else {
            // Использовать объект ContentResolver активности для вызова
            // update для объекта DatabaseContentProvider
            int updatedRows = getActivity().getContentResolver().update(wordUri, contentValues, null, null);
            if (updatedRows > 0) {
                listener.onAddEditWordCompleted(wordUri, dictionaryName);
                Snackbar.make(coordinatorLayout, R.string.word_updated, Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(coordinatorLayout, R.string.word_not_updated, Snackbar.LENGTH_LONG).show();
            }
        }

        listener.updateDateOfChangeDictionary(dictionaryName);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case WORD_LOADER:
                return new CursorLoader(getActivity(),
                        wordUri,// Uri отображаемого контакта
                        null,// Все столбцы
                        null,// Все записи
                        null,// Без аргументов
                        null);// Порядок сортировки
            case DICTIONARIES_LOADER:
                return new CursorLoader(getActivity(),
                        Dictionaries.CONTENT_URI,// Uri отображаемого контакта
                        null,// Все столбцы
                        null,// Все записи
                        null,// Без аргументов
                        Dictionaries.COLUMN_DATE_OF_CHANGE + " COLLATE NOCASE DESC");// Порядок сортировки
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == WORD_LOADER) {
            // Если слово существует в базе данных, вывести его информацию
            if (data != null && data.moveToFirst()) {
                enTextInputLayout.getEditText().setText(data.getString(data.getColumnIndex(Words.COLUMN_EN)));
                ruTextInputLayout.getEditText().setText(data.getString(data.getColumnIndex(Words.COLUMN_RU)));
                dictionaryName = data.getString(data.getColumnIndex(Words.COLUMN_DICTIONARY));
                dictionary.setText(dictionaryName);

                updateSaveButtonFAB();
            }
            getLoaderManager().initLoader(DICTIONARIES_LOADER, null, this);

        } else if (loader.getId() == DICTIONARIES_LOADER) {
            dictionariesNameArray = new String[data.getCount()];
            int counter;
            if (dictionaryName != null) {
                dictionariesNameArray[0] = dictionaryName;
                counter = 1;
                while (data.moveToNext()) {
                    String dictName = data.getString(data.getColumnIndex(Dictionaries.COLUMN_NAME));
                    if (!dictName.equals(dictionaryName)) {
                        dictionariesNameArray[counter] = dictName;
                        counter++;
                    }
                }
            } else {
                counter = 0;
                while (data.moveToNext()) {
                    dictionariesNameArray[counter] = data.getString(data.getColumnIndex(Dictionaries.COLUMN_NAME));
                    counter++;
                }
            }
            cursor = data;
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void chooseDictionaryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Выберите словарь")
                .setSingleChoiceItems(dictionariesNameArray, currentPositionInArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentPositionInArray = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        dictionaryName = dictionariesNameArray[currentPositionInArray];
                        dictionary.setText(dictionaryName);
                        dialog.cancel();
                    }
                })
                .setNeutralButton("Новый словарь", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AddDictionaryDialog().show(getActivity().getSupportFragmentManager(), "add dictionary");
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.create().show();
    }

//    private void updateForDateOfChangeDictionary(){
//        cursor.moveToFirst();
//        while (cursor.moveToNext()) {
//            String str = cursor.getString(cursor.getColumnIndex(Dictionaries.COLUMN_NAME));
//            if (dictionaryName.equals(str)){
//                int id = cursor.getInt(cursor.getColumnIndex(Dictionaries._ID));
//                Uri dictionaryUri = Dictionaries.buildDictionariesUri(id);
//                ContentValues cv = new ContentValues();
//                cv.put(Dictionaries._ID, id);
//                cv.put(Dictionaries.COLUMN_NAME, dictionaryName);
//                cv.put(Dictionaries.COLUMN_DATE_OF_CHANGE, new Date().getTime() / 1000);
//                int updatedRows = getActivity().getContentResolver().update(dictionaryUri, cv, null, null);
//                if (updatedRows < 0) {
//                    throw new SQLException(getContext().getString(R.string.invalid_update_uri)+ dictionaryUri);
//                }
//                break;
//            }
//        }
//    }
}
