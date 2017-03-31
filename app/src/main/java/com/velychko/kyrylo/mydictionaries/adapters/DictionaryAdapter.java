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
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription.Words;

public class DictionaryAdapter extends RecyclerView.Adapter<DictionaryAdapter.ViewHolder> {

    public interface DictionaryClickListener {
        void onClick(Uri wordUri);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView word;
        private long rowId;

        public ViewHolder(View itemView) {
            super(itemView);
            word = (TextView) itemView.findViewById(R.id.word);
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_in_dictionary, parent, false);
        return new ViewHolder(view);
    }

    // Назначает текст элемента списка
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String wordEn;
        String wordRu;

        cursor.moveToPosition(position);
        holder.setRowId(cursor.getLong(cursor.getColumnIndex(DatabaseDescription.Words._ID)));
        wordEn = cursor.getString(cursor.getColumnIndex(Words.COLUMN_EN));
        wordRu = cursor.getString(cursor.getColumnIndex(Words.COLUMN_RU));
        switch (cursor.getInt(cursor.getColumnIndex(Words.COLUMN_FROM_EN_TO_RU))){
            case Words.FROM_EN_TO_RU_TRUE:
                holder.word.setText(wordEn + " - " + wordRu);
                break;
            case Words.FROM_EN_TO_RU_FALSE:
                holder.word.setText(wordRu + " - " + wordEn);
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
