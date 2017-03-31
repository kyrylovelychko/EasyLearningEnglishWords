package com.velychko.kyrylo.mydictionaries.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.velychko.kyrylo.mydictionaries.R;
import com.velychko.kyrylo.mydictionaries.adapters.DictionariesNamesListAdapter;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseMaster;
import com.velychko.kyrylo.mydictionaries.ui.activities.TranslateWordTrainingActivity;
import com.velychko.kyrylo.mydictionaries.utils.Constants;

import java.util.ArrayList;

public class StartTrainingFragment extends Fragment {

    //region ===== Поля класса =====
    // Поля для UI
    TextView tvDictionary;
    TextView tvTrainingMode;
    TextView tvTranslationDirection;
    RecyclerView rvDictionaries;
    Button btnChooseDictionaries;
    Button btnStartTraining;
    Spinner spnChooseCountOfWords;
    RadioGroup rgTrainingMode;
    RadioGroup rgTranslationDirection;
    RadioButton rbTranslateWord;
    RadioButton rbChooseTranslate;
    RadioButton rbRandom;
    RadioButton rbLikeInADictionary;
    RadioButton rbEnToRu;
    RadioButton rbRuToEn;
    Switch switchAutoContinue;

    String[] allDictionariesArray;
    boolean[] checkedDictionariesArray;
    ArrayList<String> namesOfCheckedDictionariesArray = new ArrayList<>();
    DictionariesNamesListAdapter adapterChosenDictionaries;
    Cursor cursor;
    //endregion

    // Конструктор
    public StartTrainingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_training, container, false);

        initViewComponents(view);

        getActivity().setTitle(R.string.title_start_training);

        return view;
    }

    // Инициализация компонентов экрана
    private void initViewComponents(View view) {
        tvDictionary = (TextView) view.findViewById(R.id.tvDictionary);
        tvTrainingMode = (TextView) view.findViewById(R.id.tvTrainingMode);
        tvTranslationDirection = (TextView) view.findViewById(R.id.tvTranslationDirection);

        rvDictionaries = (RecyclerView) view.findViewById(R.id.rvDictionaries);
        rvDictionaries.setLayoutManager(new LinearLayoutManager(getActivity().getBaseContext(),
                LinearLayoutManager.HORIZONTAL, false));

        btnChooseDictionaries = (Button) view.findViewById(R.id.btnChooseDictionaries);
        btnChooseDictionaries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogForChoosingDictionaries();
            }
        });
        btnStartTraining = (Button) view.findViewById(R.id.btnStartTraining);
        btnStartTraining.setOnClickListener(onClickBtnStartTraining());

        spnChooseCountOfWords = (Spinner) view.findViewById(R.id.spnChooseCountOfWords);
        initSpinner();

        rgTrainingMode = (RadioGroup) view.findViewById(R.id.rgTrainingMode);
        rgTranslationDirection = (RadioGroup) view.findViewById(R.id.rgTranslationDirection);
        setPositionsForRadioGroupsFromPreferences();

        rbTranslateWord = (RadioButton) view.findViewById(R.id.rbTranslateWord);
        rbChooseTranslate = (RadioButton) view.findViewById(R.id.rbChooseTranslate);
        rbRandom = (RadioButton) view.findViewById(R.id.rbRandom);
        rbLikeInADictionary = (RadioButton) view.findViewById(R.id.rbLikeInADictionary);
        rbEnToRu = (RadioButton) view.findViewById(R.id.rbEnToRu);
        rbRuToEn = (RadioButton) view.findViewById(R.id.rbRuToEn);

        switchAutoContinue = (Switch) view.findViewById(R.id.switchAutoContinue);
        setSwitchFromPreferences();

        getActivity().findViewById(R.id.FAB).setVisibility(View.GONE);
    }

    // Инициализация спиннера
    private void initSpinner() {
        int currentPosition = 0;

        ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.count_of_words, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnChooseCountOfWords.setAdapter(adapter);

        // Получаем последний выбранный пользователем вариант. Его и поставим по умолчанию
        SharedPreferences sPref = getActivity().getSharedPreferences(
                Constants.SPREF_SETTINGS_FILE_NAME, Context.MODE_PRIVATE);

        currentPosition = sPref.getInt(Constants.SPREF_SPN_POSITION_COUNT_OF_WORDS, 1);

        spnChooseCountOfWords.setSelection(currentPosition);

    }

    // Отображение диалога для выбора словаря
    private void showDialogForChoosingDictionaries() {
        // Если курсор пустой, значит это в первый раз. При последующих открытиях диалога выбора
        // словаря, список словарей заново не загружается
        if (cursor == null) {
            initDictionariesArray();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_choose_dictionaries)
                .setMultiChoiceItems(allDictionariesArray, checkedDictionariesArray,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                checkedDictionariesArray[which] = isChecked;
                            }
                        })
                .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Сохраняем имена выбранных словарей
                        namesOfCheckedDictionariesArray.clear();

                        for (int i = 0; i < checkedDictionariesArray.length; i++) {
                            if (checkedDictionariesArray[i] == true) {
                                namesOfCheckedDictionariesArray.add(allDictionariesArray[i]);
                            }
                        }

                        // Устанавливаем адаптер
                        adapterChosenDictionaries =
                                new DictionariesNamesListAdapter(namesOfCheckedDictionariesArray);
                        rvDictionaries.setAdapter(adapterChosenDictionaries);
                    }
                })

                .setNegativeButton(R.string.dialog_button_cancel, null)
                .setCancelable(false)
                .show();
    }

    // Получение списка слвоарей для тренировки (всех)
    private void initDictionariesArray() {
        cursor = DatabaseMaster.getInstance(getContext()).getAllDictionaries();

        allDictionariesArray = new String[cursor.getCount()];
        checkedDictionariesArray = new boolean[cursor.getCount()];

        if (cursor.moveToFirst()) {
            int i = 0;
            do {
                allDictionariesArray[i] = cursor.getString(
                        cursor.getColumnIndex(DatabaseDescription.Dictionaries.COLUMN_NAME));
                checkedDictionariesArray[i] = false;
                i++;
            } while (cursor.moveToNext());
        }
    }

    // Установка значения по умолчанию для радиогрупп. Основывается на данных
    // последнего использования (через SharedPreferences)
    private void setPositionsForRadioGroupsFromPreferences() {
        int position;
        SharedPreferences sPref = getActivity().getSharedPreferences(
                Constants.SPREF_SETTINGS_FILE_NAME, Context.MODE_PRIVATE);

        position = sPref.getInt(Constants.SPREF_RG_TRAINING_MODE_POSITION, 0);
        rgTrainingMode.check(rgTrainingMode.getChildAt(position).getId());

        position = sPref.getInt(Constants.SPREF_RG_TRANSLATION_POSITION, 0);
        rgTranslationDirection.check(rgTranslationDirection.getChildAt(position).getId());
    }

    // Установка значения по умолчанию для свитча. Основывается на данных
    // последнего использования (через SharedPreferences)
    private void setSwitchFromPreferences() {
        SharedPreferences sPref = getActivity().getSharedPreferences(
                Constants.SPREF_SETTINGS_FILE_NAME, Context.MODE_PRIVATE);

        switchAutoContinue.setChecked(sPref.getBoolean(Constants.SPREF_SWITCH_AUTO_CONTINUE, true));
    }

    // По нажатию на кнопку начала тренировки запускается активити с тренировкой определенного типа
    private View.OnClickListener onClickBtnStartTraining() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (rgTrainingMode.getCheckedRadioButtonId()) {
                    case R.id.rbTranslateWord:
                        startTranslateWordTraining();
                        break;
                    case R.id.rbChooseTranslate:

                        break;
                }

            }
        };
    }

    // Начать тренировку с переводом слов
    private void startTranslateWordTraining() {
        if (namesOfCheckedDictionariesArray.size() == 0) {
            showDialogForChoosingDictionaries();
        } else {
            // Проверяем, есть ли слова в выбранных словарях
            if (isChosenDictionariesEmpty(new ArrayList<>(namesOfCheckedDictionariesArray))) {
                return;
            } else {
                Intent intent = new Intent(getContext(), TranslateWordTrainingActivity.class);
                // Список выбранных словарей
                intent.putExtra(Constants.EXTRA_KEY_CHECKED_DICTIONARIES_LIST,
                        namesOfCheckedDictionariesArray);
                // Направление перевода
                intent.putExtra(Constants.EXTRA_KEY_TRANSLATION_DIRECTION_ID,
                        rgTranslationDirection.getCheckedRadioButtonId());
                // Количество слов
                intent.putExtra(Constants.EXTRA_KEY_POSITION_COUNT_OF_WORDS,
                        spnChooseCountOfWords.getSelectedItemPosition());
                // Автоматический переход к новому слову
                intent.putExtra(Constants.EXTRA_KEY_AUTO_CONTINUE,
                        switchAutoContinue.isChecked());
                startActivity(intent);
            }
        }
    }

    // Проверяем, есть ли слова в выбранных словарях
    // Отбрасываем все слова, которые не имеют оба перевода
    private boolean isChosenDictionariesEmpty(ArrayList<String> namesForQuery) {
        // Получение всех слов из словарей, выбранных пользователем
        Cursor cursor = DatabaseMaster.getInstance(getContext())
                .getOnlyFullWordsFromSelectedDictionaries(namesForQuery);

        if (cursor.getCount() == 0) {
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout) getActivity()
                    .findViewById(R.id.coordinatorLayout);
            Snackbar.make(coordinatorLayout, R.string.snack_empty_dictionaries,
                    Snackbar.LENGTH_LONG).show();
            return true;
        } else {
            return false;
        }
    }

    @Override
    // При закрытии фрагмента, сохраняем все в SharedPreferences
    public void onStop() {
        super.onStop();
        RadioButton checkedRadioButtonTrainingMode =
                (RadioButton) getActivity().findViewById(rgTrainingMode.getCheckedRadioButtonId());
        int checkedIndexTrainingMode = rgTrainingMode.indexOfChild(checkedRadioButtonTrainingMode);
        RadioButton checkedRadioButtonTranslation = (RadioButton)
                getActivity().findViewById(rgTranslationDirection.getCheckedRadioButtonId());
        int checkedIndexTranslation =
                rgTranslationDirection.indexOfChild(checkedRadioButtonTranslation);

        SharedPreferences.Editor editor = getActivity().getSharedPreferences(
                Constants.SPREF_SETTINGS_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(Constants.SPREF_RG_TRAINING_MODE_POSITION, checkedIndexTrainingMode);
        editor.putInt(Constants.SPREF_RG_TRANSLATION_POSITION, checkedIndexTranslation);
        editor.putInt(Constants.SPREF_SPN_POSITION_COUNT_OF_WORDS,
                spnChooseCountOfWords.getSelectedItemPosition());
        editor.putBoolean(Constants.SPREF_SWITCH_AUTO_CONTINUE, switchAutoContinue.isChecked());
        editor.apply();
    }

}
