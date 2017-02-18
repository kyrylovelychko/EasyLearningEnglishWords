package com.k.easylearningenglishwords.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
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

import com.k.easylearningenglishwords.MainActivity;
import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.data.DatabaseDescription.Words;

public class WordDetailsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{

    public interface WordDetailsFragmentListener {

        //Вызывается при редактировании слова
        void onWordEdited(Uri wordUri);

        //Вызывается при удалении слова
        void onWordDeleted();
    }

    // Идентифицирует Loader
    private static final int WORD_LOADER = 0;

    private WordDetailsFragmentListener listener;// MainActivity
    private Uri wordUri;// Uri выбранного слова

    private TextView enWord;
    private TextView ruWord;
    private TextView dictionary;

    public WordDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);// У фрагмента есть команды меню

        // Получение объекта Bundle с аргументами и извлечение URI
        Bundle arguments = getArguments();

        if (arguments != null){
            wordUri = arguments.getParcelable(MainActivity.WORD_URI);
        }

        // Заполнение макета WordDetailsFragment
        View view = inflater.inflate(R.layout.fragment_word_details, container, false);

        // Получение компонентов EditTexts
        enWord = (TextView) view.findViewById(R.id.en_word);
        ruWord = (TextView) view.findViewById(R.id.ru_word);
        dictionary = (TextView) view.findViewById(R.id.dictionary);

        // Загрузка слова
        getLoaderManager().initLoader(WORD_LOADER, null, this);

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

    // Вызывается LoaderManager для создания Loader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Создание CursorLoader на основании аргумента id
        CursorLoader cursorLoader;

        switch (id){
            case WORD_LOADER:
                cursorLoader = new CursorLoader(getActivity(),
                        wordUri,// Uri отображаемого контакта
                        null,// Все столбцы
                        null,// Все записи
                        null,// Без аргументов
                        null);// Порядок сортировки
                break;
            default:
                cursorLoader = null;
                break;
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()){
            int enIndex = data.getColumnIndex(Words.COLUMN_EN);
            int ruIndex = data.getColumnIndex(Words.COLUMN_RU);
            int dictIndex = data.getColumnIndex(Words.COLUMN_DICTIONARY_ID);

            enWord.setText(data.getString(enIndex));
            ruWord.setText(data.getString(ruIndex));
            dictionary.setText(data.getString(dictIndex));
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
        switch (item.getItemId()){
            case R.id.action_delete_word:
                deleteWord();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Удаление слова
    private void deleteWord() {
        // FragmentManager используется для отображения confirmDelete
        confirmDelete.show(getFragmentManager(), "confirm delete");
    }

    // DialogFragment для подтверждения удаления слова
    private final DialogFragment confirmDelete = new DialogFragment(){
        // Создание объекта AlertDialog и его возвращение
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.confirm_delete_title);
            builder.setMessage(R.string.confirm_delete_message);

            builder.setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // объект ContentResolver используется для вызова delete в DatabaseContentProvider.
                    // последние два аргумента равны null, потому что идентификатор записи удаляемого
                    // контакта встроен в URI
                    getActivity().getContentResolver().delete(wordUri, null, null);
                    listener.onWordDeleted();// Оповещение слушателя
                }
            });

            builder.setNegativeButton(R.string.button_cancel, null);
            return builder.create();
        }
    };
}
