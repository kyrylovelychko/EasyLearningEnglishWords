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
import com.velychko.kyrylo.mydictionaries.adapters.DictionaryAdapter;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseHelper;
import com.velychko.kyrylo.mydictionaries.ui.utils.ItemDivider;

import static com.velychko.kyrylo.mydictionaries.utils.Constants.ARGS_DICTIONARY_URI;

public class DictionaryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface DictionaryFragmentListener {
        //Вызывается при выборе слова в словаре
        void onSelectWord(Uri wordUri);

        //Вызывается при нажатии кнопки добавления нового слова
        void onAddWord(String dictionaryName);

        //Вызывается при нажатии кнопки удаления словаря
        void onDeleteDictionary(String dictionaryName);

        //Вызывается при нажатии кнопки переименования словаря
        void onRenameDictionary(String dictionaryName);
    }

    //region ===== Константы =====
    private static final int DICTIONARY_LOADER = 0;
    //endregion

    //region ===== Поля класса =====
    //Uri текущего словаря
    private Uri dictionaryUri;
    //Название текущего словаря
    private String dictionaryName;

    // Сообщает MainActivity о действии во фрагменте
    private DictionaryFragmentListener listener;

    private DictionaryAdapter dictionaryAdapter;
    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_dictionary, container, false);

        //В Bundle получили Uri словаря. Используя Uri, находим название
        getDictionaryUriAndName();

        initViewComponents(view);

        getActivity().setTitle(dictionaryName);

        return view;
    }

    // Инициализация компонентов экрана
    private void initViewComponents(View view) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.wordsRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getBaseContext()));

        dictionaryAdapter = new DictionaryAdapter(new DictionaryAdapter.DictionaryClickListener() {
            @Override
            public void onClick(Uri wordUri) {
                listener.onSelectWord(wordUri);
            }
        });
        recyclerView.setAdapter(dictionaryAdapter);

        recyclerView.addItemDecoration(new ItemDivider(getContext()));

        FloatingActionButton addWordFAB =
                (FloatingActionButton) getActivity().findViewById(R.id.FAB);
        addWordFAB.show();
        addWordFAB.setImageResource(R.drawable.ic_add_black_24dp);
        addWordFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onAddWord(dictionaryName);
            }
        });
    }

    //В Bundle получили Uri словаря. Используя Uri, находим название
    private void getDictionaryUriAndName() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            dictionaryUri = arguments.getParcelable(ARGS_DICTIONARY_URI);
            Cursor cursor = new DatabaseHelper(getActivity()).getReadableDatabase().
                    query(DatabaseDescription.Dictionaries.TABLE_NAME,
                            null,
                            DatabaseDescription.Dictionaries._ID + "=?",
                            new String[]{"" + dictionaryUri.getLastPathSegment()},
                            null,
                            null,
                            null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                dictionaryName = cursor.getString(
                        cursor.getColumnIndex(DatabaseDescription.Dictionaries.COLUMN_NAME));
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (DictionaryFragmentListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DICTIONARY_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case DICTIONARY_LOADER:
                return new CursorLoader(getActivity(),
                        DatabaseDescription.Words.CONTENT_URI,
                        null,
                        DatabaseDescription.Words.COLUMN_DICTIONARY + "=?",
                        new String[]{"" + dictionaryName},
                        DatabaseDescription.Words.COLUMN_DATE_OF_CHANGE + " COLLATE NOCASE DESC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case DICTIONARY_LOADER:
                dictionaryAdapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        dictionaryAdapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_dictionary_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_dictionary:
                listener.onDeleteDictionary(dictionaryName);
                return true;
            case R.id.action_rename_dictionary:
                listener.onRenameDictionary(dictionaryName);
        }
        return super.onOptionsItemSelected(item);
    }

}