package com.k.easylearningenglishwords.ui.activities;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.Utils.Constants;
import com.k.easylearningenglishwords.data.SQLite.DatabaseDescription.Words;
import com.k.easylearningenglishwords.data.SQLite.DatabaseHelper;

import java.util.ArrayList;
import java.util.Random;

public class TranslateWordTrainingActivity extends AppCompatActivity {

    //region ===== Поля для UI =====
    TextView tvTextToTranslate;
    EditText etTranslatedText;
    Button btnStopTraining;
    //endregion


    String[][] trainingWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_word_training);

        initViewComponents();

        getExtras();
    }


    private void initViewComponents() {
        tvTextToTranslate = (TextView) findViewById(R.id.tv_text_to_translate);
        etTranslatedText = (EditText) findViewById(R.id.et_translated_text);
        btnStopTraining = (Button) findViewById(R.id.btn_stop_training);
    }

    public void getExtras() {
        Intent intent = getIntent();
        ArrayList namesOfDictionaries = intent.getParcelableArrayListExtra(
                Constants.EXTRA_KEY_CHECKED_DICTIONARIES_LIST);
        int rIdTranslationDirection = intent.getIntExtra(
                Constants.EXTRA_KEY_TRANSLATION_DIRECTION_ID, 0);
        int positionCountOfWords = intent.getIntExtra(
                Constants.SPREF_SPN_POSITION_COUNT_OF_WORDS, 1);

        prepareDataForTraining(namesOfDictionaries, rIdTranslationDirection, positionCountOfWords);
    }

    private void prepareDataForTraining(ArrayList<String> namesOfDictionaries,
                                        int rIdTranslationDirection, int positionCountOfWords) {
        Cursor cursorForTraining;
        int maxCountOfWords = 0;
//        String[][] allWordsFromSelectedDictionaries;

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
            trainingWords = new String[maxCountOfWords][2];
        } else {
            //TODO error. empty cursor
        }


        int i = 0;
        Random random = new Random();
        switch (rIdTranslationDirection) {
            case R.id.rbRandom:
                do {
                    getPairOfWordsForTraining(cursorForTraining, random.nextInt(2), i);
                    i++;
                } while (cursorForTraining.moveToNext() && maxCountOfWords > i);
                break;
            case R.id.rbLikeInADictionary:
                do {
                    int fromEnToRu = cursorForTraining.getInt(
                            cursorForTraining.getColumnIndex(Words.COLUMN_FROM_EN_TO_RU));
                    getPairOfWordsForTraining(cursorForTraining, fromEnToRu, i);
                    i++;
                } while (cursorForTraining.moveToNext() && maxCountOfWords > i);
                break;
            case R.id.rbEnToRu:
                do {
                    getPairOfWordsForTraining(cursorForTraining, 1, i);
                    i++;
                } while (cursorForTraining.moveToNext() && maxCountOfWords > i);
                break;
            case R.id.rbRuToEn:
                do {
                    getPairOfWordsForTraining(cursorForTraining, 0, i);
                    i++;
                } while (cursorForTraining.moveToNext() && maxCountOfWords > i);
                break;
        }
    }

    private void getPairOfWordsForTraining(Cursor cursor, int fromEnToRu, int index) {
        switch (fromEnToRu) {
            case 0:
                trainingWords[index][0] = cursor.getString(cursor.getColumnIndex(Words.COLUMN_RU));
                trainingWords[index][1] = cursor.getString(cursor.getColumnIndex(Words.COLUMN_EN));
                break;
            case 1:
                trainingWords[index][0] = cursor.getString(cursor.getColumnIndex(Words.COLUMN_EN));
                trainingWords[index][1] = cursor.getString(cursor.getColumnIndex(Words.COLUMN_RU));
                break;
        }
    }

}
