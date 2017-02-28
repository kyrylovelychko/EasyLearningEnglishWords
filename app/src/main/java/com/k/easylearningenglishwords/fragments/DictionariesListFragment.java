package com.k.easylearningenglishwords.fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.k.easylearningenglishwords.ItemDevider;
import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.adapters.DictionariesListAdapter;
import com.k.easylearningenglishwords.data.DatabaseDescription;

public class DictionariesListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {


    public interface DictionariesListFragmentListener {
        //Вызывается при выборе словаря
        void onSelectDictionary(Uri dictionaryUri);

        //Вызывается при нажатии кнопки добавления нового словаря
        void onAddDictionary();

        void onAddWord(int rIdFragmentFrom);
    }

    private static final int DICTIONARIES_LIST_LOADER = 0;

    // Сообщает MainActivity о выборе
    private DictionariesListFragmentListener listener;

    private DictionariesListAdapter dictionariesListAdapter;

    public DictionariesListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);// У фрагмента есть команды меню

        // Заполнение GUI и получение ссылки на RecyclerView
        View view = inflater.inflate(R.layout.fragment_dictionaries_list, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.dictionariesRecyclerView);

        // recyclerView выводит элементы в вертикальном списке
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getBaseContext()));

        // создание адаптера recyclerView и слушателя щелчков на элементах
        dictionariesListAdapter = new DictionariesListAdapter(
                new DictionariesListAdapter.DictionariesListClickListener() {
                    @Override
                    public void onClick(Uri dictionariesUri) {
                        listener.onSelectDictionary(dictionariesUri);
                    }
                });
        recyclerView.setAdapter(dictionariesListAdapter);

        // Присоединение ItemDecorator для вывода разделителей
        recyclerView.addItemDecoration(new ItemDevider(getContext()));

        // Улучшает быстродействие, если размер макета RecyclerView не изменяется
        recyclerView.setHasFixedSize(true);

        // Получение FloatingActionButton и настройка слушателя
        FloatingActionButton addDictionaryFAB = (FloatingActionButton) view.findViewById(R.id.addDictionaryFAB);
        addDictionaryFAB.setOnClickListener(new View.OnClickListener() {
            // Отображение AddEditWordFragment при касании FAB
            @Override
            public void onClick(View v) {
                listener.onAddDictionary();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Мои словари");
    }

    // Присваивание DictionariesListFragment при присоединении фрагмента
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (DictionariesListFragmentListener) context;
    }

    // Удаление DictionariesListFragment при отсоединении фрагмента
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    // Инициализация Loader при создании активности этого фрагмента
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DICTIONARIES_LIST_LOADER, null, this);
    }

    // Вызывается из MainActivity при обновлении базы данных другим фрагментом
    public void updateDictionariesList() {
        dictionariesListAdapter.notifyDataSetChanged();
    }

    // Вызывается LoaderManager для создания Loader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Создание CursorLoader на основании аргумента id
        switch (id) {
            case DICTIONARIES_LIST_LOADER:
                return new CursorLoader(getActivity(),
                        DatabaseDescription.Dictionaries.CONTENT_URI,// Uri таблицы словарей
                        null,// все столбцы
                        null,// все записи
                        null,// без аргументов
                        DatabaseDescription.Dictionaries.COLUMN_DATE_OF_CHANGE + " COLLATE NOCASE DESC");// сортировка
            default:
                return null;
        }
    }

    // Вызывается LoaderManager при завершении загрузки
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        dictionariesListAdapter.swapCursor(data);
    }

    // Вызывается LoaderManager при сбросе Loader
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        dictionariesListAdapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_dictionaries_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_word:
                listener.onAddWord(R.id.fragmentContainer);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
