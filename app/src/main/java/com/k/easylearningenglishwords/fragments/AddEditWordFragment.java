package com.k.easylearningenglishwords.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

import com.k.easylearningenglishwords.MainActivity;
import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.data.DatabaseDescription.Words;

public class AddEditWordFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface AddEditWordFragmentListener {

        //Вызывается при завершении редактирования слова
        void onAddEditCompleted(Uri wordUri);
    }

    // Константа для идентификации Loader
    private static final int WORD_LOADER = 0;

    private AddEditWordFragmentListener listener;// MainActivity
    private Uri wordUri;// Uri выбранного контакта
    private boolean addingNewWord = true;// Добавление (true) или изменение

    private TextInputLayout enTextInputLayout;
    private TextInputLayout ruTextInputLayout;
    private TextInputLayout dictTextInputLayout;

    private FloatingActionButton saveWordFAB;
    private CoordinatorLayout coordinatorLayout;// Для SnackBar

    public AddEditWordFragment() {
        // Required empty public constructor
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
        dictTextInputLayout = (TextInputLayout) view.findViewById(R.id.dictTextInputLayout);

        // Назначение слушателя событий FloatingActionButton
        saveWordFAB = (FloatingActionButton) view.findViewById(R.id.saveWordFAB);
        // Реагирует на событие, генерируемое при сохранении контакта
        saveWordFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).
                        hideSoftInputFromWindow(getView().getWindowToken(), 0);
                saveWord();// Сохранение контакта в базе данных
            }
        });
        updateSaveButtonFAB();

        // Используется для отображения SnackBar с короткими сообщениями
        coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinatorLayout);

        Bundle arguments = getArguments(); // null при создании слова
        if (arguments != null){
            addingNewWord = false;
            wordUri = arguments.getParcelable(MainActivity.WORD_URI);
        }
        if (wordUri != null){
            getLoaderManager().initLoader(WORD_LOADER, null, this);
        }

        return view;
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

        }
    };

    // Кнопка saveButtonFAB видна, если введено слово на русском или на английском
    private void updateSaveButtonFAB() {
        String en = enTextInputLayout.getEditText().getText().toString();
        String ru = ruTextInputLayout.getEditText().getText().toString();

        if (en.trim().length() != 0 || ru.trim().length() != 0){
            saveWordFAB.show();
        } else {
            saveWordFAB.hide();
        }
    }

    // Сохранение информации контакта в базе данных
    private void saveWord() {
        // Создание объекта ContentValues с парами "ключ—значение"
        ContentValues contentValues = new ContentValues();
        contentValues.put(Words.COLUMN_EN, enTextInputLayout.getEditText().getText().toString());
        contentValues.put(Words.COLUMN_RU, ruTextInputLayout.getEditText().getText().toString());
        contentValues.put(Words.COLUMN_DICTIONARY_ID, dictTextInputLayout.getEditText().getText().toString());

        if(addingNewWord){
            // Использовать объект ContentResolver активности для вызова
            // insert для объекта DatabaseContentProvider
            Uri newWordUri = getActivity().getContentResolver().insert(Words.CONTENT_URI, contentValues);
            if (newWordUri != null){
                Snackbar.make(coordinatorLayout, R.string.word_added, Snackbar.LENGTH_LONG).show();
                listener.onAddEditCompleted(newWordUri);
            } else {
                Snackbar.make(coordinatorLayout, R.string.word_not_added, Snackbar.LENGTH_LONG).show();
            }
        } else {
            // Использовать объект ContentResolver активности для вызова
            // update для объекта DatabaseContentProvider
            int updatedRows = getActivity().getContentResolver().update(wordUri, contentValues, null, null);
            if (updatedRows > 0){
                listener.onAddEditCompleted(wordUri);
                Snackbar.make(coordinatorLayout, R.string.word_updated, Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(coordinatorLayout, R.string.word_not_updated, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            case WORD_LOADER:
                return new CursorLoader(getActivity(),
                        wordUri,// Uri отображаемого контакта
                        null,// Все столбцы
                        null,// Все записи
                        null,// Без аргументов
                        null);// Порядок сортировки
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Если слово существует в базе данных, вывести его информацию
        if (data != null && data.moveToFirst()){
            int enIndex = data.getColumnIndex(Words.COLUMN_EN);
            int ruIndex = data.getColumnIndex(Words.COLUMN_RU);
            int dictIndex = data.getColumnIndex(Words.COLUMN_DICTIONARY_ID);

            enTextInputLayout.getEditText().setText(data.getString(enIndex));
            ruTextInputLayout.getEditText().setText(data.getString(ruIndex));
            dictTextInputLayout.getEditText().setText(data.getString(dictIndex));

            updateSaveButtonFAB();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


}
