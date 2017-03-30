package com.k.easylearningenglishwords.adapters;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.data.sqlite.DatabaseDescription;
import com.k.easylearningenglishwords.data.sqlite.DatabaseDescription.Dictionaries;

import java.text.SimpleDateFormat;
import java.util.Date;

;

public class DictionariesListAdapter extends RecyclerView.Adapter<DictionariesListAdapter.ViewHolder> {

    // Интерфейс реализуется DictionariesListFragment для обработки
    // прикосновения к элементу в списке RecyclerView
    public interface DictionariesListClickListener {
        void onClick(Uri dictionaryUri);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private TextView dateOfChange;
        private TextView countOfWords;
        private long rowID;

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

        // Идентификатор записи базы данных для контакта во ViewHolder
        public void setRowID(long rowID) {
            this.rowID = rowID;
        }

        public void setName(TextView name) {
            this.name = name;
        }
    }


    // Переменные экземпляров DictionariesListAdapter
    private Cursor cursorDictionariesList = null;
    private Cursor cursorCountOfWords = null;
    private final DictionariesListClickListener clickListener;
    private String text;

    // Конструктор
    public DictionariesListAdapter(DictionariesListClickListener clickListener) {
        this.clickListener = clickListener;
    }

    // Подготовка нового элемента списка и его объекта ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        text = parent.getResources().getString(R.string.layout_tv_word_s);
        // Заполнение макета
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_in_dictionaries_list, parent, false);
        return new ViewHolder(view);// ViewHolder текущего элемента
    }

    // Назначает текст элемента списка
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursorDictionariesList.moveToPosition(position);
        holder.setRowID(cursorDictionariesList.getLong(cursorDictionariesList.getColumnIndex(Dictionaries._ID)));
        String dictionaryName = cursorDictionariesList.getString(cursorDictionariesList.getColumnIndex(Dictionaries.COLUMN_NAME));
        holder.name.setText(dictionaryName);

        long milisec = cursorDictionariesList.getLong(cursorDictionariesList.getColumnIndex(Dictionaries.COLUMN_DATE_OF_CHANGE)) * 1000;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        holder.dateOfChange.setText(sdf.format(new Date(milisec)));

        holder.countOfWords.setText(text + " " + String.valueOf(getCountOfWordsInDictionary(dictionaryName)));
    }

    // Возвращает количество элементов, предоставляемых адаптером
    @Override
    public int getItemCount() {
        return (cursorDictionariesList != null) ? cursorDictionariesList.getCount() : 0;
    }

    public void swapCursorDictionariesList(Cursor cursor){
        this.cursorDictionariesList = cursor;
        notifyDataSetChanged();
    }

    public void swapCursorCountOfWords(Cursor cursor){
        this.cursorCountOfWords = cursor;
        notifyDataSetChanged();
    }

    private int getCountOfWordsInDictionary(String dictionaryName) {
        int count = 0;
        String currentName;
        if (cursorCountOfWords.moveToFirst()) {
            do {
                currentName = cursorCountOfWords.getString(cursorCountOfWords.getColumnIndex(
                        DatabaseDescription.Words.COLUMN_DICTIONARY));
                if (currentName.equals(dictionaryName)) {
                    count = cursorCountOfWords.getInt(cursorCountOfWords.getColumnIndex("counter"));
                    return count;
                }
            } while (cursorCountOfWords.moveToNext());
        }

        return 0;
    }

}
