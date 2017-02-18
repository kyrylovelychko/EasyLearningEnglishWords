package com.k.easylearningenglishwords.adapters;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.data.DatabaseDescription;

public class DictionariesListAdapter extends RecyclerView.Adapter<DictionariesListAdapter.ViewHolder> {

    // Интерфейс реализуется DictionariesListFragment для обработки
    // прикосновения к элементу в списке RecyclerView
    public interface DictionaryClickListener{
        void onClick(Uri dictionaryUri);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView name;
        public TextView date;
        public TextView words_count;
        private long rowID;

        // Настройка объекта ViewHolder элемента RecyclerView
        public ViewHolder(View itemView) {
            super(itemView);
        }

        // Идентификатор записи базы данных для контакта в ViewHolder
        public void setRowID(long rowID) {
            this.rowID = rowID;
        }
    }


    // Переменные экземпляров DictionariesAdapter
    private Cursor cursor = null;
    private final DictionaryClickListener clickListener;

    public DictionariesListAdapter(DictionaryClickListener clickListener) {
        this.clickListener = clickListener;
    }

    // Подготовка нового элемента списка и его объекта ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Заполнение макета
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dictionary_element, parent, false);
        return new ViewHolder(view);// ViewHolder текущего элемента
    }

    // Назначает текст элемента списка
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.setRowID(cursor.getLong(cursor.getColumnIndex(DatabaseDescription.Dictionaries._ID)));
        holder.name.setText(cursor.getString(cursor.getColumnIndex(DatabaseDescription.Dictionaries.COLUMN_NAME)));
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
