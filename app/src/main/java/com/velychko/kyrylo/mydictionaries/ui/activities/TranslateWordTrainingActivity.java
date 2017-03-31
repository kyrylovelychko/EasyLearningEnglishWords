package com.velychko.kyrylo.mydictionaries.ui.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.velychko.kyrylo.mydictionaries.R;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription.Words;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseMaster;
import com.velychko.kyrylo.mydictionaries.ui.fragments.dialogs.ExitTrainingDialog;
import com.velychko.kyrylo.mydictionaries.utils.Constants;

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

    //region ===== Поля класса: =====
    // Список с парами "Слово для перевода" - "Правильный ответ"
    private ArrayList<String[]> trainingWords = new ArrayList<>();
    // Индекс текущей позиции в списке trainingWords
    private int indexOfCurrentWord = 0;
    // Выйти из тренировки?
    private boolean doExit = false;
    // Максимальное количесвто слов в тренировке
    private int maxCountOfWords = 0;
    // Правильный ответ
    private String answer;
    // Флаг автоматического перехода к новому слову при тренировке. Если включен - будет происходить
    // автоматический переход к новому слову в тренировке. Если выключен - придется
    // нажимать на кнопку, чтобы перейти к новому слову в тренировке.
    private boolean autoContinue = true;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_word_training);

        initViewComponents();

        Intent intent = getIntent();
        // Получаем список имен словарей для тренировки
        ArrayList namesOfDictionaries = intent.getParcelableArrayListExtra(
                Constants.EXTRA_KEY_CHECKED_DICTIONARIES_LIST);
        // Получаем направление перевода в тренировке
        int rIdTranslationDirection = intent.getIntExtra(
                Constants.EXTRA_KEY_TRANSLATION_DIRECTION_ID, 0);
        // Получаем позицию в Spinner для определения максимального количества слов в тренировке
        int positionCountOfWords = intent.getIntExtra(
                Constants.SPREF_SPN_POSITION_COUNT_OF_WORDS, 1);
        // Получаем флаг автоматического перехода к новому слову в тренировке
        autoContinue = intent.getBooleanExtra(Constants.EXTRA_KEY_AUTO_CONTINUE, false);
        // Если флаг = true, прячем кнопки для перехода к следующему слову
        if (autoContinue) {
            showBtnNextWord(false);
        }

        // Подготовка данных к тренировке на основании полученных выше параметров
        prepareDataForTraining(namesOfDictionaries, rIdTranslationDirection, positionCountOfWords);

        // Вывод первого слова в тренировке
        // Размер ProgressBar = максимальному количеству слов в тренировке
        prbTrainingProgress.setMax(maxCountOfWords);
        tvTrainingProgress.setText(
                "1 " + getResources().getString(R.string.layout_tv_from) + " " + maxCountOfWords);
        // Выводим первое слово для перевода
        tvTextToTranslate.setText(trainingWords.get(0)[0]);
        // Сохраняем правильный ответ
        answer = trainingWords.get(0)[1];
        // Прячем кноки перехода к следующему слову
        showBtnNextWord(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Показываем клавиатуру
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    // Инициализация компонентов экрана
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

    // Подготовка данных к тренировке
    private void prepareDataForTraining(ArrayList<String> namesOfDictionaries,
                                        int rIdTranslationDirection, int positionCountOfWords) {
        // Курсор для получаения слов для тренировки
        Cursor cursorForTraining;
        // Список всех слов из словарей, выбранных пользователем
        ArrayList<String[]> allWordsFromSelectedDictionaries = new ArrayList<>();

        // Получение всех слов из словарей, выбранных пользователем
        cursorForTraining = DatabaseMaster.getInstance(this)
                .getOnlyFullWordsFromSelectedDictionaries(namesOfDictionaries);

        // Получаем максимальное количество слов в тренировке, проходя по курсору
        if (cursorForTraining.moveToFirst()) {
            String[] countOfWordsArray = getResources().getStringArray(R.array.count_of_words);

            if (positionCountOfWords == countOfWordsArray.length - 1) {
                // Если указана последняя позиция в спиннере - это значит, что ответ "Все"(слова)
                // Естанавливаем максимальное количество слов в тренировке = количеству всех слов
                // в словарях, выбранных пользователем
                maxCountOfWords = cursorForTraining.getCount();
            } else {
                // Иначе, смотрим, какое максимальное количество слов для тренировки выбрал
                // пользователь. Если оно больше, чем количество слов в этих словарях,
                // ставим столько слов, сколько есть
                maxCountOfWords = Integer.valueOf(countOfWordsArray[positionCountOfWords]);
                if (cursorForTraining.getCount() < maxCountOfWords) {
                    maxCountOfWords = cursorForTraining.getCount();
                }
            }
        }

        /* На основе направления перевода, выбранного пользователем, наполняем список
        * trainingWords парами слов. Проходим по курсору и методом getPairOfWordsForTraining
        * определяем следующую пару. Первый параметр - курсор, второй параметр - если 1, то
        * направление перевода "en-ru", если 0, то наоборот
        */
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
        // В случайном порядке отбираем столько слов, сколько указано в переменной maxCountOfWords
        while (trainingWords.size() < maxCountOfWords) {
            nextPosition = new Random().nextInt(allWordsFromSelectedDictionaries.size());
            trainingWords.add(allWordsFromSelectedDictionaries.get(nextPosition));
            allWordsFromSelectedDictionaries.remove(nextPosition);
        }
    }

    // Получение пары слов для тренировки в формате "Перевести с", "Перевести на"
    private String[] getPairOfWordsForTraining(Cursor cursor, int fromEnToRu) {
        String[] pairOfWords = new String[2];
        // Это баг системы. Иногда рандом срабатывает неправильно. Приходится перепроверять.
        if (fromEnToRu == 2) {
            fromEnToRu = new Random().nextInt(2);
        }

        // В зависимости от направления перевода, слова сохраняются в определенном порядке
        // Первое слово с индексом 0 - это то, что нужно перевести, а с индексом 1 - правильный ответ
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

    // Слушатель поля для ввода ответа
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            // Если в ответе пользователя столько же символов, сколько и в правильном ответе:
            if (s.length() == answer.length()) {
                // Если пользователь ответил правильно и тексты ответов совпадают:
                if (s.toString().equalsIgnoreCase(answer)) {
                    btnHelpTraining.setClickable(false);
                    etTranslatedText.setBackgroundResource(R.drawable.shape_corners_15_green);
                    // Если при запуске тренировки пользователь выбрал автоматический переход
                    // к новому слову, переходим с задержкой в одну секунду
                    if (autoContinue) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                goToNextWord();
                            }
                        }, 1000);
                    } else {
                        // Отображаем кнопки перехода к следующему слову
                        showBtnNextWord(true);
                    }
                }
            } else if (s.length() > answer.length()) {
                // Если в ответе пользователя больше символов, чем в правильном ответе:
                btnHelpTraining.setClickable(true);
                showBtnNextWord(false);
                etTranslatedText.setBackgroundResource(R.drawable.shape_corners_15_red);
            } else if (s.length() < answer.length()) {
                // Если в ответе пользователя меньше символов, чем в правильном ответе:
                btnHelpTraining.setClickable(true);
                showBtnNextWord(false);
                etTranslatedText.setBackgroundResource(R.drawable.shape_corners_15_white);
            }
        }
    };

    // Переход к номоу слову
    private void goToNextWord() {
        // Увеличиваем индекс текущего слова в списке слов для тренировки
        indexOfCurrentWord++;
        // Если это было не последнее слово
        if (indexOfCurrentWord < trainingWords.size()) {
            // Ставим новое задание
            tvTextToTranslate.setText(trainingWords.get(indexOfCurrentWord)[0]);
            // Очищаем поле для ввода
            etTranslatedText.setText("");
            // Обновляем значение правильного ответа
            answer = trainingWords.get(indexOfCurrentWord)[1];
            // Увеличиваем текущий прогресс на 1
            prbTrainingProgress.setProgress(indexOfCurrentWord + 1);
            tvTrainingProgress.setText("" + (indexOfCurrentWord + 1) + " " +
                    getResources().getString(R.string.layout_tv_from) + " " + maxCountOfWords);
            // Кнопка помощи снова доступна
            btnHelpTraining.setClickable(true);
            // Кнопки перехода к следующему слову недоступны
            showBtnNextWord(false);
        } else {
            // Если это было последнее слово, тренировка завершена. Возвращаемся на предыдущий
            // фрагмент.
            doExit = true;
            super.onBackPressed();
            return;
        }
    }

    // Кнопка выхода из тренировки
    private View.OnClickListener onClickBtnExitTraining() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Показываем диалог подтверждения остановки тренировки.
                showDialogExitTraining();
            }
        };
    }

    // Кнопка помощи.
    private View.OnClickListener onClickBtnHelpTraining() {
        /* Если в набранном ответе пользователь допустил ошибку - будет показан правильный ответ
        * с количеством букв, введенных пользователем. Если пользователь пока вводил правильно,
        * или пока еще не ввел ничего, но нажал на кнопку подсказки - добавится еще одна буква.
        * Если пользователь ввел больше букв, чем в правильном ответе, по нажатию на кнопку будет
        * выведен полный правильный ответ.
        * */
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
    // Если стоит флаг Выйти - выходим, если нет - выдаем диалог подтверждения остановки тренировки
    public void onBackPressed() {
        if (doExit) {
            super.onBackPressed();
            doExit = false;
        } else {
            showDialogExitTraining();
        }
    }

    // Сеттер
    public void setDoExit(boolean doExit) {
        this.doExit = doExit;
    }

    // Вывод диалога подтверждения остановки тренировки
    private void showDialogExitTraining() {
        new ExitTrainingDialog().show(getSupportFragmentManager(), "exit training");
    }

    // Показать/спрятать кнопки перехода к следующему слову в тренировке
    private void showBtnNextWord(boolean show) {
        if (show) {
            btnNextWord.setVisibility(View.VISIBLE);
            btnHidedNext.setVisibility(View.VISIBLE);
        } else {
            btnNextWord.setVisibility(View.GONE);
            btnHidedNext.setVisibility(View.GONE);
        }
    }
}