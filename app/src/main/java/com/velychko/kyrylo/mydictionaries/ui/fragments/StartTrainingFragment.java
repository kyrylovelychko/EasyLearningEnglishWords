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
import android.text.TextUtils;
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
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseHelper;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseMaster;
import com.velychko.kyrylo.mydictionaries.ui.activities.TranslateWordTrainingActivity;
import com.velychko.kyrylo.mydictionaries.utils.Constants;

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
    Switch switchAutoContinue;
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

//        namesOfCheckedDictionariesArray.add("Привет");
//        adapterChosenDictionaries = new DictionariesNamesListAdapter(namesOfCheckedDictionariesArray);
//        rvDictionaries.setAdapter(adapterChosenDictionaries);
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

    private void initSpinner() {
        int currentPosition = 0;

        ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.count_of_words, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnChooseCountOfWords.setAdapter(adapter);

        SharedPreferences sPref = getActivity().getSharedPreferences(
                Constants.SPREF_SETTINGS_FILE_NAME, Context.MODE_PRIVATE);

        currentPosition = sPref.getInt(Constants.SPREF_SPN_POSITION_COUNT_OF_WORDS, 1);

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
        cursor = DatabaseMaster.getInstance(getContext()).getAllDictionaries();

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

    private void setPositionsForRadioGroupsFromPreferences() {
        int position;
        SharedPreferences sPref = getActivity().getSharedPreferences(
                Constants.SPREF_SETTINGS_FILE_NAME, Context.MODE_PRIVATE);

        position = sPref.getInt(Constants.SPREF_RG_TRAINING_MODE_POSITION, 0);
        rgTrainingMode.check(rgTrainingMode.getChildAt(position).getId());

        position = sPref.getInt(Constants.SPREF_RG_TRANSLATION_POSITION, 0);
        rgTranslationDirection.check(rgTranslationDirection.getChildAt(position).getId());
    }

    private void setSwitchFromPreferences() {
        SharedPreferences sPref = getActivity().getSharedPreferences(
                Constants.SPREF_SETTINGS_FILE_NAME, Context.MODE_PRIVATE);

        switchAutoContinue.setChecked(sPref.getBoolean(Constants.SPREF_SWITCH_AUTO_CONTINUE, false));
    }

    private View.OnClickListener onClickBtnStartTraining() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (rgTrainingMode.getCheckedRadioButtonId()){
                    case R.id.rbTranslateWord:
                        startTranslateWordTraining();
                        break;
                    case R.id.rbChooseTranslate:

                        break;
                }

            }
        };
    }

    private void startTranslateWordTraining(){
        if (namesOfCheckedDictionariesArray.size() == 0) {
            showDialogForChoosingDictionaries();
        } else {
            if (isChosenDictionariesEmpty(new ArrayList<>(namesOfCheckedDictionariesArray))) {
                return;
            } else {
                Intent intent = new Intent(getContext(), TranslateWordTrainingActivity.class);
                intent.putExtra(Constants.EXTRA_KEY_CHECKED_DICTIONARIES_LIST,
                        namesOfCheckedDictionariesArray);
                intent.putExtra(Constants.EXTRA_KEY_TRANSLATION_DIRECTION_ID,
                        rgTranslationDirection.getCheckedRadioButtonId());
                intent.putExtra(Constants.EXTRA_KEY_POSITION_COUNT_OF_WORDS,
                        spnChooseCountOfWords.getSelectedItemPosition());
                intent.putExtra(Constants.EXTRA_KEY_AUTO_CONTINUE,
                        switchAutoContinue.isChecked());
                startActivity(intent);
            }
        }
    }

    private boolean isChosenDictionariesEmpty(ArrayList<String> namesForQuery){
        Cursor cursor;
        for (int i = 0; i < namesForQuery.size(); i++) {
            namesForQuery.set(i, "\"" + namesForQuery.get(i) + "\"");
        }
        String sqlQuery = "SELECT * FROM " + DatabaseDescription.Words.TABLE_NAME
                + " WHERE " + DatabaseDescription.Words.COLUMN_DICTIONARY
                + " IN (" + TextUtils.join(", ", namesForQuery) + ") ORDER BY "
                + DatabaseDescription.Words.COLUMN_DATE_OF_CHANGE + " COLLATE NOCASE DESC";

        cursor = new DatabaseHelper(getContext()).getReadableDatabase().rawQuery(
                sqlQuery, null);

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
