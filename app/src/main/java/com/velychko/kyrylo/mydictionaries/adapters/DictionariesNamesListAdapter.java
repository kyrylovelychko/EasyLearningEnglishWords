package com.velychko.kyrylo.mydictionaries.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.velychko.kyrylo.mydictionaries.R;

import java.util.ArrayList;

public class DictionariesNamesListAdapter
        extends RecyclerView.Adapter<DictionariesNamesListAdapter.ViewHolder> {

    // Поля класса:
    ArrayList<String> dictionariesNamesArray;

    // Конструктор
    public DictionariesNamesListAdapter(ArrayList<String> dictionariesNamesArray) {
        this.dictionariesNamesArray = dictionariesNamesArray;
    }

    // Подготовка нового элемента списка и его объекта ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_dictionary_name, parent, false);
        return new ViewHolder(view);
    }

    // Заполнение полей элемента списка
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setDictionaryName(dictionariesNamesArray.get(position));
    }

    // Возвращает количество элементов, предоставляемых адаптером
    @Override
    public int getItemCount() {
        return dictionariesNamesArray == null ? 0 : dictionariesNamesArray.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView dictionaryName;

        public ViewHolder(View itemView) {
            super(itemView);
            dictionaryName = (TextView) itemView.findViewById(R.id.elementDictionaryName);
        }

        public void setDictionaryName(String dictionaryName) {
            this.dictionaryName.setText(dictionaryName);
        }
    }

}
