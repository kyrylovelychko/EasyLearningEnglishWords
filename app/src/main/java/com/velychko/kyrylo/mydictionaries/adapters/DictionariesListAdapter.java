package com.velychko.kyrylo.mydictionaries.adapters;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.velychko.kyrylo.mydictionaries.R;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription.Dictionaries;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DictionariesListAdapter
        extends RecyclerView.Adapter<DictionariesListAdapter.ViewHolder> {

    // Интерфейс реализуется DictionariesListFragment для обработки
    // прикосновения к элементу в списке RecyclerView
    public interface DictionariesListClickListener {
        void onClick(Uri dictionaryUri);
    }

    //region ===== Поля класса =====
    // Крусор для получения списка словарей
    private Cursor cursorDictionariesList = null;
    // Курсор для получения количества слов в каждом словаре
    private Cursor cursorCountOfWords = null;
    // Экземпляр внутреннего интерфейса
    private final DictionariesListClickListener clickListener;
    private String textCountOfWords;
    //endregion

    // Конструктор
    public DictionariesListAdapter(DictionariesListClickListener clickListener) {
        this.clickListener = clickListener;
    }

    // Подготовка нового элемента списка и его объекта ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Получение из файла строковых ресурсов текста, который будет стоять перед количеством слов
        textCountOfWords = parent.getResources().getString(R.string.layout_tv_word_s);
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_in_dictionaries_list, parent, false);
        return new ViewHolder(view);
    }

    // Заполнение полей элемента списка
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursorDictionariesList.moveToPosition(position);
        holder.setRowID(cursorDictionariesList.getLong(
                cursorDictionariesList.getColumnIndex(Dictionaries._ID)));
        String dictionaryName = cursorDictionariesList.getString(
                cursorDictionariesList.getColumnIndex(Dictionaries.COLUMN_NAME));
        holder.name.setText(dictionaryName);

        // Получение текущего времени и даты
        long milisec = cursorDictionariesList.getLong(
                cursorDictionariesList.getColumnIndex(Dictionaries.COLUMN_DATE_OF_CHANGE)) * 1000;
        // Формат, в котором будет храниться дата
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        holder.dateOfChange.setText(sdf.format(new Date(milisec)));

        holder.countOfWords.setText(textCountOfWords + " "
                + String.valueOf(getCountOfWordsInDictionary(dictionaryName)));
    }

    // Возвращает количество элементов, предоставляемых адаптером
    @Override
    public int getItemCount() {
        return (cursorDictionariesList != null) ? cursorDictionariesList.getCount() : 0;
    }

    // Обновление курсора для получения списка словарей
    public void swapCursorDictionariesList(Cursor cursor) {
        this.cursorDictionariesList = cursor;
        notifyDataSetChanged();
    }

    // Обновление курсора для получения количества слов в словаре
    public void swapCursorCountOfWords(Cursor cursor) {
        this.cursorCountOfWords = cursor;
        notifyDataSetChanged();
    }

    // Получение количества слов в словаре по названию словаря
    private int getCountOfWordsInDictionary(String dictionaryName) {
        int count = 0;
        String currentName;
        if (cursorCountOfWords.moveToFirst()) {
            // Проходим по всем записям курсора
            do {
                // Получаем название словаря из текущей позиции курсора
                currentName = cursorCountOfWords.getString(cursorCountOfWords.getColumnIndex(
                        DatabaseDescription.Words.COLUMN_DICTIONARY));
                // Если currentName совпадает с искомым названием словаря, переданным в параметрах
                if (currentName.equals(dictionaryName)) {
                    // Получаем количество слов в словаре из курсора
                    count = cursorCountOfWords.getInt(cursorCountOfWords.getColumnIndex("counter"));
                    return count;
                }
            } while (cursorCountOfWords.moveToNext());
        }

        // Если словарь с искомым именем не найден, возвращаем 0
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name; // Название словаря
        private TextView dateOfChange; // Дата последнего изменения словаря
        private TextView countOfWords; // Количество слов в словаре
        private long rowID; // Идентификатор записи базы данных для контакта во ViewHolder

        // Настройка объекта ViewHolder элемента RecyclerView
        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.dictionaryName);
            dateOfChange = (TextView) itemView.findViewById(R.id.dictionaryDateOfChange);
            countOfWords = (TextView) itemView.findViewById(R.id.countOfWords);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onClick(Dictionaries.buildDictionariesUri(rowID));
                }
            });
        }

        public void setRowID(long rowID) {
            this.rowID = rowID;
        }

        public void setName(TextView name) {
            this.name = name;
        }
    }

}
