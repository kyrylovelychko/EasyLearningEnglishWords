package com.k.easylearningenglishwords.adapters;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.data.SQLite.DatabaseDescription.Dictionaries;

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
        private TextView date_of_change;
        private long rowID;

        // Настройка объекта ViewHolder элемента RecyclerView
        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.dictionary_name);
            date_of_change = (TextView) itemView.findViewById(R.id.dictionary_date_of_change);
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
    private Cursor cursor = null;
    private final DictionariesListClickListener clickListener;

    // Конструктор
    public DictionariesListAdapter(DictionariesListClickListener clickListener) {
        this.clickListener = clickListener;
    }

    // Подготовка нового элемента списка и его объекта ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Заполнение макета
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_dictionary, parent, false);
        return new ViewHolder(view);// ViewHolder текущего элемента
    }

    // Назначает текст элемента списка
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.setRowID(cursor.getLong(cursor.getColumnIndex(Dictionaries._ID)));
        holder.name.setText(cursor.getString(cursor.getColumnIndex(Dictionaries.COLUMN_NAME)));
        long milisec = cursor.getLong(cursor.getColumnIndex(Dictionaries.COLUMN_DATE_OF_CHANGE)) * 1000;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        holder.date_of_change.setText(sdf.format(new Date(milisec)));
    }

    // Возвращает количество элементов, предоставляемых адаптером
    @Override
    public int getItemCount() {
        return (cursor != null) ? cursor.getCount() : 0;
    }

    public void swapCursor(Cursor cursor){
        this.cursor = cursor;
        notifyDataSetChanged();
    }

}
