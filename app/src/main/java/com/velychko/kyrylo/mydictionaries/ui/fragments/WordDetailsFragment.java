package com.velychko.kyrylo.mydictionaries.ui.fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.velychko.kyrylo.mydictionaries.R;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription.Words;

import static com.velychko.kyrylo.mydictionaries.utils.Constants.ARGS_WORD_URI;

public class WordDetailsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface WordDetailsFragmentListener {
        //Вызывается при редактировании слова
        void onEditWord(Uri wordUri);

        //Вызывается при удалении слова
        void onDeleteWord(Uri wordUri);

        // Принудительное закрытие SnackBar
        void dismissSnackBar();
    }

    //region ===== Константы =====
    private static final int WORD_LOADER = 0;
    //endregion

    //region ===== Поля класса =====
    // Сообщает MainActivity о действии во фрагменте
    private WordDetailsFragmentListener listener;
    // Uri выбранного слова
    private Uri wordUri;

    // На английском
    private TextView enWord;
    // На русском
    private TextView ruWord;
    // Словарь
    private TextView dictionary;
    //endregion
    private FloatingActionButton editWordFAB;

    public WordDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_word_details, container, false);

        initViewComponents(view);

        getArgumentsFromBundle();

        getActivity().setTitle(R.string.title_word_details);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (WordDetailsFragmentListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    // Инициализация компонентов экрана
    private void initViewComponents(View view) {
        enWord = (TextView) view.findViewById(R.id.en_word);
        ruWord = (TextView) view.findViewById(R.id.ru_word);
        dictionary = (TextView) view.findViewById(R.id.dictionary);

        editWordFAB = (FloatingActionButton) getActivity().findViewById(R.id.FAB);
        editWordFAB.show();
        editWordFAB.setImageResource(R.drawable.ic_edit_black_24dp);
        editWordFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.dismissSnackBar();
                listener.onEditWord(wordUri);
            }
        });
    }

    // Получение данных из аргументов Bundle
    private void getArgumentsFromBundle() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            wordUri = arguments.getParcelable(ARGS_WORD_URI);
            getLoaderManager().initLoader(WORD_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case WORD_LOADER:
                return new CursorLoader(getActivity(),
                        wordUri,// Uri отображаемого контакта
                        null,// Все столбцы
                        null,// Все записи
                        null,// Без аргументов
                        null);// Порядок сортировки
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            enWord.setText(data.getString(data.getColumnIndex(Words.COLUMN_EN)));
            ruWord.setText(data.getString(data.getColumnIndex(Words.COLUMN_RU)));
            dictionary.setText(data.getString(data.getColumnIndex(Words.COLUMN_DICTIONARY)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_word_details_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_word:
                listener.onDeleteWord(wordUri);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}