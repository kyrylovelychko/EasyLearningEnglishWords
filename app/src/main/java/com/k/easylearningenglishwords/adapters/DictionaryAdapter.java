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
import com.k.easylearningenglishwords.data.sqlite.DatabaseDescription.Words;

public class DictionaryAdapter extends RecyclerView.Adapter<DictionaryAdapter.ViewHolder> {

    public interface DictionaryClickListener {
        void onClick(Uri wordUri);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView wordFrom;
        public TextView wordTo;
        private long rowId;

        public ViewHolder(View itemView) {
            super(itemView);
            wordFrom = (TextView) itemView.findViewById(R.id.wordFrom);
            wordTo = (TextView) itemView.findViewById(R.id.wordTo);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onClick(Words.buildWordsUri(rowId));
                }
            });
        }

        public void setRowId(long rowId) {
            this.rowId = rowId;
        }
    }

    // Переменные экземпляров
    private Cursor cursor = null;
    private final DictionaryClickListener clickListener;

    // Конструктор
    public DictionaryAdapter(DictionaryClickListener clickListener) {
        this.clickListener = clickListener;
    }

    // Подготовка нового элемента списка и его объекта ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Заполнение макета
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_word, parent, false);
        return new ViewHolder(view);
    }

    // Назначает текст элемента списка
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.setRowId(cursor.getLong(cursor.getColumnIndex(DatabaseDescription.Words._ID)));
        switch (cursor.getInt(cursor.getColumnIndex(Words.COLUMN_FROM_EN_TO_RU))){
            case Words.FROM_EN_TO_RU_TRUE:
                holder.wordFrom.setText(cursor.getString(cursor.getColumnIndex(Words.COLUMN_EN)));
                holder.wordTo.setText(cursor.getString(cursor.getColumnIndex(Words.COLUMN_RU)));
                break;
            case Words.FROM_EN_TO_RU_FALSE:
                holder.wordFrom.setText(cursor.getString(cursor.getColumnIndex(Words.COLUMN_RU)));
                holder.wordTo.setText(cursor.getString(cursor.getColumnIndex(Words.COLUMN_EN)));
                break;
        }
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
