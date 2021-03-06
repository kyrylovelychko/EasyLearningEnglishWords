package com.velychko.kyrylo.mydictionaries.ui.fragments;

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

import com.velychko.kyrylo.mydictionaries.R;
import com.velychko.kyrylo.mydictionaries.adapters.DictionariesListAdapter;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription.Dictionaries;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription.Words;
import com.velychko.kyrylo.mydictionaries.ui.utils.ItemDivider;

public class DictionariesListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface DictionariesListFragmentListener {
        //Вызывается при выборе словаря в списке словарей
        void onSelectDictionary(Uri dictionaryUri);

        //Вызывается при нажатии кнопки добавления нового словаря
        void onAddDictionary();

        //Вызывается при нажатии кнопки добавления нового слова
        void onAddWord(String dictionaryName);
    }

    //region ===== Константы =====
    // Лоадер для получения списка всех словарей
    private static final int DICTIONARIES_LIST_LOADER = 0;
    // Лоадер для получения количества слов в каждом словаре
    private static final int COUNT_OF_WORDS_IN_DICTIONARY_LOADER = 1;
    //endregion

    //region ===== Поля класса =====
    // Сообщает MainActivity о действии во фрагменте
    private DictionariesListFragmentListener listener;

    private DictionariesListAdapter dictionariesListAdapter;
    //endregion

    // Конструктор
    public DictionariesListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_dictionaries_list, container, false);

        initViewComponents(view);

        getActivity().setTitle(R.string.title_my_dictionaries);

        return view;
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
        // Лоадер для получения списка всех словарей
        getLoaderManager().initLoader(DICTIONARIES_LIST_LOADER, null, this);
        // Лоадер для получения количества слов в каждом словаре
        getLoaderManager().initLoader(COUNT_OF_WORDS_IN_DICTIONARY_LOADER, null, this);
    }

    // Инициализация компонентов экрана
    private void initViewComponents(View view) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.dictionariesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getBaseContext()));
        dictionariesListAdapter = new DictionariesListAdapter(
                new DictionariesListAdapter.DictionariesListClickListener() {
                    @Override
                    public void onClick(Uri dictionariesUri) {
                        listener.onSelectDictionary(dictionariesUri);
                    }
                });
        recyclerView.setAdapter(dictionariesListAdapter);
        recyclerView.addItemDecoration(new ItemDivider(getContext()));

        FloatingActionButton addDictionaryFAB =
                (FloatingActionButton) getActivity().findViewById(R.id.FAB);
        addDictionaryFAB.show();
        addDictionaryFAB.setImageResource(R.drawable.ic_library_add_black_24dp);
        addDictionaryFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onAddDictionary();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case DICTIONARIES_LIST_LOADER:
                return new CursorLoader(getActivity(),
                        Dictionaries.CONTENT_URI,
                        null,
                        null,
                        null,
                        Dictionaries.COLUMN_DATE_OF_CHANGE + " COLLATE NOCASE DESC");
            case COUNT_OF_WORDS_IN_DICTIONARY_LOADER:
                return new CursorLoader(getActivity(),
                        Words.CONTENT_URI,
                        new String[]{Words.COLUMN_DICTIONARY + ", COUNT(_id) AS counter"},
                        new String(Words._ID + " >= 0) GROUP BY (" + Words.COLUMN_DICTIONARY),
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case DICTIONARIES_LIST_LOADER:
                dictionariesListAdapter.swapCursorDictionariesList(data);
                break;
            case COUNT_OF_WORDS_IN_DICTIONARY_LOADER:
                dictionariesListAdapter.swapCursorCountOfWords(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case DICTIONARIES_LIST_LOADER:
                dictionariesListAdapter.swapCursorDictionariesList(null);
                break;
            case COUNT_OF_WORDS_IN_DICTIONARY_LOADER:
                dictionariesListAdapter.swapCursorCountOfWords(null);
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_dictionaries_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_word:
                listener.onAddWord(null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
