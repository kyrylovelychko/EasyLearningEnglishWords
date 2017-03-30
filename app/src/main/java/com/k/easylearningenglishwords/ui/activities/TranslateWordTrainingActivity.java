package com.k.easylearningenglishwords.ui.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.data.sqlite.DatabaseDescription.Words;
import com.k.easylearningenglishwords.data.sqlite.DatabaseHelper;
import com.k.easylearningenglishwords.ui.fragments.dialogs.ExitTrainingDialog;
import com.k.easylearningenglishwords.utils.Constants;

import java.util.ArrayList;
import java.util.Random;

public class TranslateWordTrainingActivity extends AppCompatActivity {

    //region ===== Поля для UI =====
    TextView tvTextToTranslate;
    TextView tvTrainingProgress;
    EditText etTranslatedText;
    Button btnExitTraining;
    Button btnHelpTraining;
    Button btnNextWord;
    Button btnHidedNext;
    ProgressBar prbTrainingProgress;
    //endregion


    private ArrayList<String[]> trainingWords = new ArrayList<>();
    private int indexOfCurrentWord = 0;
    private boolean doExit = false;
    private int maxCountOfWords = 0;
    private String answer;
    private boolean autoContinue = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_word_training);

        initViewComponents();

        Intent intent = getIntent();
        ArrayList namesOfDictionaries = intent.getParcelableArrayListExtra(
                Constants.EXTRA_KEY_CHECKED_DICTIONARIES_LIST);
        int rIdTranslationDirection = intent.getIntExtra(
                Constants.EXTRA_KEY_TRANSLATION_DIRECTION_ID, 0);
        int positionCountOfWords = intent.getIntExtra(
                Constants.SPREF_SPN_POSITION_COUNT_OF_WORDS, 1);
        autoContinue = intent.getBooleanExtra(
                Constants.EXTRA_KEY_AUTO_CONTINUE, false);
        if (autoContinue) {
            btnNextWord.setVisibility(View.GONE);
            btnHidedNext.setVisibility(View.GONE);
        }

        prepareDataForTraining(namesOfDictionaries, rIdTranslationDirection, positionCountOfWords);

        prbTrainingProgress.setMax(maxCountOfWords);
        tvTrainingProgress.setText(
                "1 " + getResources().getString(R.string.layout_tv_from) + " " + maxCountOfWords);
        tvTextToTranslate.setText(trainingWords.get(0)[0]);
        answer = trainingWords.get(0)[1];
        showBtnNextWord(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void initViewComponents() {
        prbTrainingProgress = (ProgressBar) findViewById(R.id.prbTrainingProgress);
        prbTrainingProgress.incrementProgressBy(1);

        tvTextToTranslate = (TextView) findViewById(R.id.tvTextToTranslate);
        tvTrainingProgress = (TextView) findViewById(R.id.tvTrainingProgress);

        etTranslatedText = (EditText) findViewById(R.id.etTranslatedText);
        etTranslatedText.addTextChangedListener(textWatcher);
        etTranslatedText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        btnExitTraining = (Button) findViewById(R.id.btnExitTraining);
        btnExitTraining.setOnClickListener(onClickBtnExitTraining());
        btnHelpTraining = (Button) findViewById(R.id.btnHelpTraining);
        btnHelpTraining.setOnClickListener(onClickBtnHelpTraining());
        btnNextWord = (Button) findViewById(R.id.btnNextWord);
        btnNextWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etTranslatedText.getText().toString().equalsIgnoreCase(answer)) {
                    goToNextWord();
                }
            }
        });
        btnHidedNext = (Button) findViewById(R.id.btnHidedNext);
    }

    private void prepareDataForTraining(ArrayList<String> namesOfDictionaries,
                                        int rIdTranslationDirection, int positionCountOfWords) {
        Cursor cursorForTraining;
        ArrayList<String[]> allWordsFromSelectedDictionaries = new ArrayList<>();

        for (int i = 0; i < namesOfDictionaries.size(); i++) {
            namesOfDictionaries.set(i, "\"" + namesOfDictionaries.get(i) + "\"");
        }
        String sqlQuery = "SELECT * FROM " + Words.TABLE_NAME + " WHERE " + Words.COLUMN_DICTIONARY
                + " IN (" + TextUtils.join(", ", namesOfDictionaries) + ") ORDER BY "
                + Words.COLUMN_DATE_OF_CHANGE + " COLLATE NOCASE DESC";

        cursorForTraining = new DatabaseHelper(getApplicationContext()).getReadableDatabase().rawQuery(
                sqlQuery, null);

        if (cursorForTraining.moveToFirst()) {
            String[] countOfWordsArray = getResources().getStringArray(R.array.count_of_words);
            if (positionCountOfWords == countOfWordsArray.length - 1) {
                maxCountOfWords = cursorForTraining.getCount();
            } else {
                maxCountOfWords = Integer.valueOf(countOfWordsArray[positionCountOfWords]);
                if (cursorForTraining.getCount() < maxCountOfWords) {
                    maxCountOfWords = cursorForTraining.getCount();
                }
            }
        } else {
        }

        switch (rIdTranslationDirection) {
            case R.id.rbRandom:
                do {
                    allWordsFromSelectedDictionaries.add(
                            getPairOfWordsForTraining(cursorForTraining, 2));
                } while (cursorForTraining.moveToNext());
                break;
            case R.id.rbLikeInADictionary:
                do {
                    int fromEnToRu = cursorForTraining.getInt(
                            cursorForTraining.getColumnIndex(Words.COLUMN_FROM_EN_TO_RU));
                    allWordsFromSelectedDictionaries.add(
                            getPairOfWordsForTraining(cursorForTraining, fromEnToRu));
                } while (cursorForTraining.moveToNext());
                break;
            case R.id.rbEnToRu:
                do {
                    allWordsFromSelectedDictionaries.add(
                            getPairOfWordsForTraining(cursorForTraining, 1));
                } while (cursorForTraining.moveToNext());
                break;
            case R.id.rbRuToEn:
                do {
                    allWordsFromSelectedDictionaries.add(
                            getPairOfWordsForTraining(cursorForTraining, 0));
                } while (cursorForTraining.moveToNext());
                break;
        }

        int nextPosition = 0;
        while (trainingWords.size() < maxCountOfWords) {
            nextPosition = new Random().nextInt(allWordsFromSelectedDictionaries.size());
            trainingWords.add(allWordsFromSelectedDictionaries.get(nextPosition));
            allWordsFromSelectedDictionaries.remove(nextPosition);
        }
    }

    private String[] getPairOfWordsForTraining(Cursor cursor, int fromEnToRu) {
        String[] pairOfWords = new String[2];
        if (fromEnToRu == 2){
            fromEnToRu =  new Random().nextInt(2);
        }
        switch (fromEnToRu) {
            case 0:
                pairOfWords[0] = cursor.getString(cursor.getColumnIndex(Words.COLUMN_RU));
                pairOfWords[1] = cursor.getString(cursor.getColumnIndex(Words.COLUMN_EN));
                break;
            case 1:
                pairOfWords[0] = cursor.getString(cursor.getColumnIndex(Words.COLUMN_EN));
                pairOfWords[1] = cursor.getString(cursor.getColumnIndex(Words.COLUMN_RU));
                break;
        }
        return pairOfWords;
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() >= answer.length()) {
                if (s.toString().equalsIgnoreCase(answer)) {
                    btnHelpTraining.setClickable(false);
//                    etTranslatedText.setBackgroundColor(getResources().getColor(R.color.correctAnswer));
                    etTranslatedText.setBackgroundResource(R.drawable.shape_corners_15_green);
                    if (autoContinue) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                goToNextWord();
                            }
                        }, 1000);
                    } else {
                        showBtnNextWord(true);
                    }
                } else {
                    btnHelpTraining.setClickable(true);
                    showBtnNextWord(false);
//                    etTranslatedText.setBackgroundColor(getResources().getColor(R.color.wrongAnswer));
                    etTranslatedText.setBackgroundResource(R.drawable.shape_corners_15_red);
                }
            } else {
                btnHelpTraining.setClickable(true);
                showBtnNextWord(false);
//                etTranslatedText.setBackgroundColor(getResources().getColor(white));
                etTranslatedText.setBackgroundResource(R.drawable.shape_corners_15_white);
            }
        }
    };

    private void goToNextWord() {
        indexOfCurrentWord++;
        if (indexOfCurrentWord < trainingWords.size()) {
            tvTextToTranslate.setText(trainingWords.get(indexOfCurrentWord)[0]);
            etTranslatedText.setText("");
        } else {
            doExit = true;
            super.onBackPressed();
            return;
        }
        answer = trainingWords.get(indexOfCurrentWord)[1];
        prbTrainingProgress.setProgress(indexOfCurrentWord + 1);
        tvTrainingProgress.setText("" + (indexOfCurrentWord + 1) + " " +
                getResources().getString(R.string.layout_tv_from) + " " + maxCountOfWords);
        btnHelpTraining.setClickable(true);
        showBtnNextWord(false);
    }

    private View.OnClickListener onClickBtnExitTraining() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogExitTraining();
            }
        };
    }

    private View.OnClickListener onClickBtnHelpTraining() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentText = etTranslatedText.getText().toString();
                String answer = trainingWords.get(indexOfCurrentWord)[1];
                if (currentText.length() >= answer.length()) {
                    etTranslatedText.setText(answer);
                } else {
                    String partOfAnswer = answer.substring(0, currentText.length());
                    if (!currentText.equalsIgnoreCase(partOfAnswer)) {
                        etTranslatedText.setText(partOfAnswer);
                    } else {
                        etTranslatedText.setText(answer.substring(0, currentText.length() + 1));
                    }
                }
                etTranslatedText.setSelection(etTranslatedText.getText().length());
            }
        };
    }

    @Override
    public void onBackPressed() {
        if (doExit) {
            super.onBackPressed();
            doExit = false;
        } else {
            showDialogExitTraining();
        }
    }

    public void setDoExit(boolean doExit) {
        this.doExit = doExit;
    }

    private void showDialogExitTraining() {
        new ExitTrainingDialog().show(getSupportFragmentManager(), "exit training");
    }

    private void showBtnNextWord(boolean show){
        if (show) {
            btnNextWord.setVisibility(View.VISIBLE);
            btnHidedNext.setVisibility(View.VISIBLE);
        } else {
            btnNextWord.setVisibility(View.GONE);
            btnHidedNext.setVisibility(View.GONE);
        }

    }
}