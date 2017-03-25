package com.k.easylearningenglishwords.ui.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.utils.Constants;
import com.k.easylearningenglishwords.data.sqlite.DatabaseDescription.Words;
import com.k.easylearningenglishwords.data.sqlite.DatabaseHelper;

import java.util.ArrayList;
import java.util.Random;

public class TranslateWordTrainingActivity extends AppCompatActivity {

    //region ===== Поля для UI =====
    TextView tvTextToTranslate;
    EditText etTranslatedText;
    Button btnStopTraining;
    //endregion


    ArrayList<String[]> trainingWords = new ArrayList<>();

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

        prepareDataForTraining(namesOfDictionaries, rIdTranslationDirection, positionCountOfWords);
    }


    private void initViewComponents() {
        tvTextToTranslate = (TextView) findViewById(R.id.tv_text_to_translate);
        etTranslatedText = (EditText) findViewById(R.id.et_translated_text);
        btnStopTraining = (Button) findViewById(R.id.btn_stop_training);
    }

    private void prepareDataForTraining(ArrayList<String> namesOfDictionaries,
                                        int rIdTranslationDirection, int positionCountOfWords) {
        Cursor cursorForTraining;
        int maxCountOfWords = 0;
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
            if (positionCountOfWords == countOfWordsArray.length - 1){
                maxCountOfWords = cursorForTraining.getCount();
            } else {
                maxCountOfWords = Integer.valueOf(countOfWordsArray[positionCountOfWords]);
                if (cursorForTraining.getCount() < maxCountOfWords) {
                    maxCountOfWords = cursorForTraining.getCount();
                }
            }
        } else {
            return;

            //TODO error. empty cursor
        }

        switch (rIdTranslationDirection) {
            case R.id.rbRandom:
                do {
                    allWordsFromSelectedDictionaries.add(
                            getPairOfWordsForTraining(cursorForTraining, new Random().nextInt(2)));
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
        while (trainingWords.size() < maxCountOfWords){
            nextPosition = new Random().nextInt(allWordsFromSelectedDictionaries.size());
            trainingWords.add(allWordsFromSelectedDictionaries.get(nextPosition));
            allWordsFromSelectedDictionaries.remove(nextPosition);
        }

    }

    private String[] getPairOfWordsForTraining(Cursor cursor, int fromEnToRu) {
        String[] pairOfWords = new String[2];
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

}