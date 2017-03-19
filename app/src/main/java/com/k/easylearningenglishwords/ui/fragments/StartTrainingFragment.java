package com.k.easylearningenglishwords.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.TextView;

import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.Utils.Constants;
import com.k.easylearningenglishwords.adapters.DictionariesNamesListAdapter;
import com.k.easylearningenglishwords.data.SQLite.DatabaseDescription;

import java.util.ArrayList;

public class StartTrainingFragment extends Fragment {

    // region ===== Поля для UI =====
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
    // endregion

    Cursor cursor;
    String[] allDictionariesArray;
    boolean[] checkedDictionariesArray;
    ArrayList<String> namesOfCheckedDictionariesArray = new ArrayList<>();
    DictionariesNamesListAdapter adapterChosenDictionaries;

    public StartTrainingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_training, container, false);

        initViewComponents(view);

        initSpinner();

        getActivity().findViewById(R.id.FAB).setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_start_training);
    }

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
        spnChooseCountOfWords = (Spinner) view.findViewById(R.id.spnChooseCountOfWords);
        rgTrainingMode = (RadioGroup) view.findViewById(R.id.rgTrainingMode);
        rgTranslationDirection = (RadioGroup) view.findViewById(R.id.rgTranslationDirection);
        rbTranslateWord = (RadioButton) view.findViewById(R.id.rbTranslateWord);
        rbChooseTranslate = (RadioButton) view.findViewById(R.id.rbChooseTranslate);
        rbRandom = (RadioButton) view.findViewById(R.id.rbRandom);
        rbLikeInADictionary = (RadioButton) view.findViewById(R.id.rbLikeInADictionary);
        rbEnToRu = (RadioButton) view.findViewById(R.id.rbEnToRu);
        rbRuToEn = (RadioButton) view.findViewById(R.id.rbRuToEn);
    }

    private void initSpinner() {
        int currentPosition = 0;

        ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.count_of_words, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnChooseCountOfWords.setAdapter(adapter);

        SharedPreferences sPref = getActivity().getSharedPreferences(
                Constants.SPREF_SETTINGS_FILE_NAME, Context.MODE_PRIVATE);
        if (sPref.contains(Constants.SPREF_SPINNER_POSITION_COUNT_OF_WORDS)){
            currentPosition = sPref.getInt(Constants.SPREF_SPINNER_POSITION_COUNT_OF_WORDS, 1);
        } else {
            SharedPreferences.Editor editor = sPref.edit();
            editor.putInt(Constants.SPREF_SPINNER_POSITION_COUNT_OF_WORDS, 1);
            editor.apply();
            currentPosition = 1;
        }

        spnChooseCountOfWords.setSelection(currentPosition);

    }

    private void showDialogForChoosingDictionaries() {
        if (cursor == null) {
            initDictionariesArray();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_choose_dictionaries)
                .setMultiChoiceItems(allDictionariesArray, checkedDictionariesArray, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedDictionariesArray[which] = isChecked;
                    }
                })
                .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        namesOfCheckedDictionariesArray.clear();

                        for (int i = 0; i < checkedDictionariesArray.length; i++) {
                            if (checkedDictionariesArray[i] == true) {
                                namesOfCheckedDictionariesArray.add(allDictionariesArray[i]);
                            }
                        }

                        adapterChosenDictionaries = new DictionariesNamesListAdapter(namesOfCheckedDictionariesArray);
                        rvDictionaries.setAdapter(adapterChosenDictionaries);
                    }
                })

                .setNegativeButton(R.string.dialog_button_cancel, null)
                .setCancelable(false)
                .show();
    }

    private void initDictionariesArray() {
        cursor = getActivity().getContentResolver().query(
                DatabaseDescription.Dictionaries.CONTENT_URI,
                null,
                null,
                null,
                DatabaseDescription.Dictionaries.COLUMN_DATE_OF_CHANGE + " COLLATE NOCASE DESC"
        );

        allDictionariesArray = new String[cursor.getCount()];
        checkedDictionariesArray = new boolean[cursor.getCount()];

        if (cursor.moveToFirst()) {
            int i = 0;
            do {
                allDictionariesArray[i] = cursor.getString(cursor.getColumnIndex(DatabaseDescription.Dictionaries.COLUMN_NAME));
                checkedDictionariesArray[i] = false;
                i++;
            } while (cursor.moveToNext());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(
                Constants.SPREF_SETTINGS_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(Constants.SPREF_SPINNER_POSITION_COUNT_OF_WORDS, spnChooseCountOfWords.getSelectedItemPosition());
        editor.apply();
    }
}
