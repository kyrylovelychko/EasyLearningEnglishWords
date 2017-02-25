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
import android.view.View;
import android.view.ViewGroup;

import com.k.easylearningenglishwords.ItemDevider;
import com.k.easylearningenglishwords.MainActivity;
import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.adapters.DictionaryAdapter;
import com.k.easylearningenglishwords.data.DatabaseDescription;

public class DictionaryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface DictionaryFragmentListener {

        //Вызывается при выборе слова
        void onWordSelected(Uri wordUri);

        //Вызывается при нажатии кнопки добавления нового слова
        void onAddWord(int rIdFragmentFrom);
    }

    private static final int DICTIONARY_LOADER = 0;
    private static final int DICTIONARY_NAME_LOADER = 1;

    private Uri dictionaryUri;

    private DictionaryFragmentListener listener;

    private DictionaryAdapter dictionaryAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_dictionary, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.wordsRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getBaseContext()));

        dictionaryAdapter = new DictionaryAdapter(new DictionaryAdapter.DictionaryClickListener() {
            @Override
            public void onClick(Uri wordUri) {
                listener.onWordSelected(wordUri);
            }
        });
        recyclerView.setAdapter(dictionaryAdapter);

        recyclerView.addItemDecoration(new ItemDevider(getContext()));

        recyclerView.setHasFixedSize(true);

        FloatingActionButton addWordFAB = (FloatingActionButton) view.findViewById(R.id.addWordFAB);
        addWordFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onAddWord(R.id.fragmentContainer);
            }
        });

        Bundle arguments = getArguments();
        if (arguments != null) {
            dictionaryUri = arguments.getParcelable(MainActivity.DICTIONARY_URI);
            getLoaderManager().initLoader(DICTIONARY_NAME_LOADER, null, this);
        }

        return view;
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
                        null,
                        null,
                        DatabaseDescription.Words.COLUMN_DATE_OF_CHANGE + " COLLATE NOCASE DESC");
            case DICTIONARY_NAME_LOADER:
                return new CursorLoader(getActivity(),
                        dictionaryUri,
                        null,
                        null,
                        null,
                        null);
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
            case DICTIONARY_NAME_LOADER:
                data.moveToFirst();
                getActivity().setTitle(data.getString(
                        data.getColumnIndex(DatabaseDescription.Dictionaries.COLUMN_NAME)));
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        dictionaryAdapter.swapCursor(null);
    }

}
