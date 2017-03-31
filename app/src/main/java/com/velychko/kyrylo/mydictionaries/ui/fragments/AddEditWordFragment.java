package com.velychko.kyrylo.mydictionaries.ui.fragments;

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

import com.velychko.kyrylo.mydictionaries.R;
import com.velychko.kyrylo.mydictionaries.data.network.yandexdictionary.YandexDictionaryResponse;
import com.velychko.kyrylo.mydictionaries.data.network.yandexdictionary.YandexDictionaryRetrofit;
import com.velychko.kyrylo.mydictionaries.data.network.yandextranslate.YandexTranslateResponse;
import com.velychko.kyrylo.mydictionaries.data.network.yandextranslate.YandexTranslateRetrofit;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription.Dictionaries;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription.Words;
import com.velychko.kyrylo.mydictionaries.ui.fragments.dialogs.AddDictionaryDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.velychko.kyrylo.mydictionaries.utils.Constants.*;

public class AddEditWordFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface AddEditWordFragmentListener {
        // Вызывается при завершении редактирования слова
        void onAddEditWordCompleted(Uri wordUri, String dictionaryName);

        // Вызвается при обновлении даты последнего изменения конкретного словаря
        void changeDateOfChangeDictionary(String dictionaryName);

        // Вызывается при необходимости отобразить SnackBar
        void showSnackBar(int snackTextRId);
    }

    //region ===== Константы =====
    // Константа для идентификации Loader
    private static final int WORD_LOADER = 0;
    private static final int DICTIONARIES_LOADER = 1;
    //endregion

    //region ===== Поля класса =====
    // Сообщает MainActivity о действии во фрагменте
    private AddEditWordFragmentListener listener;

    // Uri текущего контакта
    private Uri wordUri;
    // Добавление нового слова (true) или редактирование (false)
    private boolean addingNewWord = true;

    // Поля для UI
    private TextInputLayout enTextInputLayout;
    private TextInputLayout ruTextInputLayout;
    private TextView dictionary;
    private String dictionaryName;
    private Button chooseDictionaryButton;
    private Button translateToEn;
    private Button translateToRu;
    private FloatingActionButton saveWordFAB;

    // Список словарей, в которые пользователь может сохранить свое слово
    String[] dictionariesNameArray;
    private int currentPositionInArray = 0;

    // Если 1 - пользователь сначала ввел английское слово, а потом русское. 0 - наоборот.
    private int FROM_EN_TO_RU;
    //endregion

    // Конструктор
    public AddEditWordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_add_edit_word, container, false);

        initViewComponents(view);

        getArgumentsFromBundle();

        if (wordUri == null) {
            getActivity().setTitle(R.string.title_new_word);
        } else {
            getActivity().setTitle(R.string.title_editing_word);
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (addingNewWord == false) {
            // Если редактирование существующего слова, тогда загружаем сначала это слово, а
            // потом все словари
            getLoaderManager().initLoader(WORD_LOADER, null, this);
        } else {
            // Если добавление нового слова, то инициализируем Loader, который загрузит список всех
            // словарей - этот список будем предлагать на выбор пользователю
            getLoaderManager().initLoader(DICTIONARIES_LOADER, null, this);
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

    // Инициализация компонентов экрана
    private void initViewComponents(View view) {
        enTextInputLayout = (TextInputLayout) view.findViewById(R.id.enTextInputLayout);
        enTextInputLayout.getEditText().addTextChangedListener(enOrRuFilled);
        ruTextInputLayout = (TextInputLayout) view.findViewById(R.id.ruTextInputLayout);
        ruTextInputLayout.getEditText().addTextChangedListener(enOrRuFilled);
        dictionary = (TextView) view.findViewById(R.id.dictionary);
        chooseDictionaryButton = (Button) view.findViewById(R.id.chooseDictionaryButton);
        chooseDictionaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDictionaryDialog(false);
            }
        });
        translateToEn = (Button) view.findViewById(R.id.translateToEnBtn);
        translateToRu = (Button) view.findViewById(R.id.translateToRuBtn);
        translateToEn.setVisibility(View.GONE);
        translateToEn.setOnClickListener(getOnClickTranslate());
        translateToRu.setVisibility(View.GONE);
        translateToRu.setOnClickListener(getOnClickTranslate());

        saveWordFAB = (FloatingActionButton) getActivity().findViewById(R.id.FAB);
        saveWordFAB.setImageResource(R.drawable.ic_save_black_24dp);
        saveWordFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).
                        hideSoftInputFromWindow(getView().getWindowToken(), 0);
                if (dictionary.getText().equals("")) {
                    chooseDictionaryDialog(true);
                } else {
                    saveWord();// Сохранение слова в базе данных
                    getFragmentManager().popBackStack();
                }
            }
        });
        updateSaveButtonFAB();
    }

    // Получение данных из аргументов Bundle
    private void getArgumentsFromBundle() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            wordUri = arguments.getParcelable(ARGS_WORD_URI);
            if (wordUri != null) {
                addingNewWord = false;
            }
            dictionaryName = arguments.getString(ARGS_DICTIONARY_NAME);
            dictionary.setText(dictionaryName);
        }
    }

    // Обнаруживает изменения в тексте полей EditText, связанных
    // с TextInputLayout, для отображения или скрытия saveButtonFAB
    private final TextWatcher enOrRuFilled = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        // Вызывается при изменении текста в TextInputLayout.
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Кнопка saveButtonFAB видна, если введено слово на русском или/и на английском
            updateSaveButtonFAB();
        }

        // Если введено слово на одном языке - вывести кнопку для возможности перевода напротив
        // другого языка. Если заполнены оба поля - переводчики не предлагаются
        @Override
        public void afterTextChanged(Editable s) {
            String enText = enTextInputLayout.getEditText().getText().toString();
            String ruText = ruTextInputLayout.getEditText().getText().toString();
            if (!enText.equals("") && ruText.equals("")) {
                FROM_EN_TO_RU = Words.FROM_EN_TO_RU_TRUE;
                translateToRu.setVisibility(View.VISIBLE);
            } else if (enText.equals("") && !ruText.equals("")) {
                FROM_EN_TO_RU = Words.FROM_EN_TO_RU_FALSE;
                translateToEn.setVisibility(View.VISIBLE);
            } else {
                translateToEn.setVisibility(View.GONE);
                translateToRu.setVisibility(View.GONE);
            }

        }
    };

    // Кнопка saveButtonFAB видна, если введено слово на русском или/и на английском
    private void updateSaveButtonFAB() {
        String en = enTextInputLayout.getEditText().getText().toString();
        String ru = ruTextInputLayout.getEditText().getText().toString();

        if (en.trim().length() != 0 || ru.trim().length() != 0) {
            saveWordFAB.show();
        } else {
            saveWordFAB.hide();
        }
    }

    // Сохранение слова в базе данных
    private void saveWord() {
        // Собираем объект ContentValues
        ContentValues contentValues = new ContentValues();
        contentValues.put(Words.COLUMN_EN,
                enTextInputLayout.getEditText().getText().toString().trim());
        contentValues.put(Words.COLUMN_RU,
                ruTextInputLayout.getEditText().getText().toString().trim());
        contentValues.put(Words.COLUMN_FROM_EN_TO_RU, FROM_EN_TO_RU);
        contentValues.put(Words.COLUMN_DICTIONARY, dictionaryName);
        // Берем текущую дату и время
        contentValues.put(Words.COLUMN_DATE_OF_CHANGE, new Date().getTime() / 1000);

        if (addingNewWord) {
            // Если добавление нового слова, вставляем новую запись в таблицу слов
            Uri newWordUri =
                    getActivity().getContentResolver().insert(Words.CONTENT_URI, contentValues);
            if (newWordUri != null) {
                // Если слово успешно добавлено, уведомляем пользователя.
                listener.showSnackBar(R.string.snack_word_added);
                // Вызываем метод MainActivity для оповещения окончания вставки нового слова
                listener.onAddEditWordCompleted(newWordUri, dictionaryName);
                // В таблице словарей, для записи текущего словаря обновляем дату последнего
                // изменения. Используем метод MainActivity
                listener.changeDateOfChangeDictionary(dictionaryName);
            } else {
                // Ошибка вставки нового слова
                listener.showSnackBar(R.string.snack_word_not_added);
            }
        } else {
            // Если обновление существующего слова, делаем обновление записи таблицы слов
            int updatedRows =
                    getActivity().getContentResolver().update(wordUri, contentValues, null, null);
            if (updatedRows > 0) {
                // Если слово успешно добавлено, уведомляем пользователя.
                listener.showSnackBar(R.string.snack_word_updated);
                // Вызываем метод MainActivity для оповещения окончания обновления слова
                listener.onAddEditWordCompleted(wordUri, dictionaryName);
                // В таблице словарей, для записи текущего словаря обновляем дату последнего
                // изменения. Используем метод MainActivity
                listener.changeDateOfChangeDictionary(dictionaryName);
            } else {
                // Ошибка вставки нового слова
                listener.showSnackBar(R.string.snack_word_not_updated);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case WORD_LOADER:
                return new CursorLoader(getActivity(),
                        wordUri,
                        null,
                        null,
                        null,
                        null);
            case DICTIONARIES_LOADER:
                return new CursorLoader(getActivity(),
                        Dictionaries.CONTENT_URI,
                        null,
                        null,
                        null,
                        Dictionaries.COLUMN_DATE_OF_CHANGE + " COLLATE NOCASE DESC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == WORD_LOADER) {
            // Если слово существует в базе данных, вывести его информацию
            if (data != null && data.moveToFirst()) {
                enTextInputLayout.getEditText().setText(
                        data.getString(data.getColumnIndex(Words.COLUMN_EN)));
                ruTextInputLayout.getEditText().setText(
                        data.getString(data.getColumnIndex(Words.COLUMN_RU)));
                dictionaryName = data.getString(
                        data.getColumnIndex(Words.COLUMN_DICTIONARY));
                dictionary.setText(dictionaryName);

                updateSaveButtonFAB();
            }

            // Инит лоадера для получения списка всех словарей, чтобы предложить пользователю
            // изменить словарь
            getLoaderManager().initLoader(DICTIONARIES_LOADER, null, this);

        } else if (loader.getId() == DICTIONARIES_LOADER) {
            // Формируем список словарей, в которые пользователь может сохранить свое слово
            dictionariesNameArray = new String[data.getCount()];
            int counter;
            if (dictionaryName != null) {
                // После получения и обработки слова, стал известен словарь этого слова.
                // Текущий словарь этого слова будет первым в списке предлоагаемых при выборе словаря.
                // Остальные словари сортируются по убыванию даты
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
                // При добавлении нового слова, словари сортируются просто по дате - сверху последние
                counter = 0;
                while (data.moveToNext()) {
                    dictionariesNameArray[counter] = data.getString(data.getColumnIndex(
                            Dictionaries.COLUMN_NAME));
                    counter++;
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    // Диалог выбора словаря
    private void chooseDictionaryDialog(final boolean saveWordAfterChoosing) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_choose_dictionary)
                .setSingleChoiceItems(dictionariesNameArray, currentPositionInArray, null)
                .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Сохранение позиции, выбранной в списке
                        currentPositionInArray =
                                ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        // Получение имени словаря по позиции в списке
                        dictionaryName = dictionariesNameArray[currentPositionInArray];
                        dictionary.setText(dictionaryName);
                        dialog.cancel();
                        if (saveWordAfterChoosing == true) {
                            saveWord();// Сохранение слова в базе данных
                            getFragmentManager().popBackStack();
                        }
                    }
                })
                .setNeutralButton(R.string.dialog_new_dictionary, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AddDictionaryDialog()
                                .show(getActivity().getSupportFragmentManager(), "add dictionary");
                    }
                })
                .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.create().show();
    }

    // Вызов переводчика по нажатию на кнопку
    private View.OnClickListener getOnClickTranslate() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = null;
                String lang = null;
                // Получаем направление перевода и текст для перевода
                switch (FROM_EN_TO_RU) {
                    case Words.FROM_EN_TO_RU_TRUE:
                        text = enTextInputLayout.getEditText().getText().toString().trim();
                        lang = "en-ru";
                        break;
                    case Words.FROM_EN_TO_RU_FALSE:
                        text = ruTextInputLayout.getEditText().getText().toString().trim();
                        lang = "ru-en";
                        break;
                }

                // Если в тексте есть пробелы - это текст. Переводим переводчиком.
                // Если пробелов нет - это слово. Пробуем перевести словарем.
                if (text.contains(" ")) {
                    tryToTranslateText(text, lang);
                } else {
                    tryToTranslateWord(text, lang);
                }
            }
        };
    }

    //region ===== Перевод с помощью Яндекс Слоаря или Переводчика =====

    // Перевод текста с помощью Переводчика
    private void tryToTranslateText(String text, String lang) {
        YandexTranslateRetrofit.translateText(text, lang)
                .enqueue(new Callback<YandexTranslateResponse>() {
                    @Override
                    public void onResponse(Call<YandexTranslateResponse> call,
                                           final Response<YandexTranslateResponse> response) {
                        if (response.code() == 200) {
                            switch (FROM_EN_TO_RU) {
                                case Words.FROM_EN_TO_RU_TRUE:
                                    ruTextInputLayout.getEditText()
                                            .setText(response.body().text.get(0).trim());
                                    ruTextInputLayout.getEditText().setSelection(
                                            ruTextInputLayout.getEditText().getText().length());
                                    break;
                                case Words.FROM_EN_TO_RU_FALSE:
                                    enTextInputLayout.getEditText()
                                            .setText(response.body().text.get(0).trim());
                                    enTextInputLayout.getEditText().setSelection(
                                            enTextInputLayout.getEditText().getText().length());
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<YandexTranslateResponse> call, Throwable t) {
                        CoordinatorLayout coordinatorLayout = (CoordinatorLayout)
                                getActivity().findViewById(R.id.coordinatorLayout);
                        Snackbar.make(coordinatorLayout, R.string.snack_translate_problem,
                                Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    // Перевод слова с помощью Словаря
    private void tryToTranslateWord(final String text, final String lang) {
        YandexDictionaryRetrofit.translateWord(text, lang)
                .enqueue(new Callback<YandexDictionaryResponse>() {
                    @Override
                    public void onResponse(Call<YandexDictionaryResponse> call,
                                           final Response<YandexDictionaryResponse> response) {
                        if (response.code() == 200) {
                            final ArrayList<String> arrayList =
                                    getArrayListWithResultOfTranslate(response);

                            if (arrayList.size() > 0) {
                                createDialogForChoosingTranslate(arrayList);
                            } else {
                                tryToTranslateText(text, lang);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<YandexDictionaryResponse> call, Throwable t) {
                        CoordinatorLayout coordinatorLayout = (CoordinatorLayout)
                                getActivity().findViewById(R.id.coordinatorLayout);
                        Snackbar.make(coordinatorLayout, R.string.snack_translate_problem,
                                Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    // Получаем все доступные в словаре переводы текущего слова. Количество переводов не больше 8.
    private ArrayList<String> getArrayListWithResultOfTranslate(
            Response<YandexDictionaryResponse> response) {
        ArrayList<String> arrayList = new ArrayList<>();
        List<YandexDictionaryResponse.Def> responseDef = response.body().def;
        for (YandexDictionaryResponse.Def def : responseDef) {
            for (YandexDictionaryResponse.Tr tr : def.tr) {
                if (arrayList.size() < 8) {
                    arrayList.add(tr.text.toString());
                } else {
                    break;
                }
            }
        }
        return arrayList;
    }

    // Вызов диалога для выбора перевода среди предложенных вариантов
    private void createDialogForChoosingTranslate(final ArrayList<String> arrayList) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_choose_translate)
                .setSingleChoiceItems(arrayList.toArray(new String[arrayList.size()]), 0, null)
                .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        int checkedPosition =
                                ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        switch (FROM_EN_TO_RU) {
                            case Words.FROM_EN_TO_RU_TRUE:
                                ruTextInputLayout.getEditText()
                                        .setText(arrayList.get(checkedPosition).trim());
                                ruTextInputLayout.getEditText().setSelection(
                                        ruTextInputLayout.getEditText().getText().length());
                                break;
                            case Words.FROM_EN_TO_RU_FALSE:
                                enTextInputLayout.getEditText()
                                        .setText(arrayList.get(checkedPosition).trim());
                                enTextInputLayout.getEditText().setSelection(
                                        enTextInputLayout.getEditText().getText().length());
                                break;
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    //endregion

}