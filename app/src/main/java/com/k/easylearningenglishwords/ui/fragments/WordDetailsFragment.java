package com.k.easylearningenglishwords.ui.fragments;

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

import com.k.easylearningenglishwords.ui.activities.MainActivity;
import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.data.SQLite.DatabaseDescription.Words;

public class WordDetailsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface WordDetailsFragmentListener {

        //Вызывается при редактировании слова
        void onEditWord(Uri wordUri);

        //Вызывается при удалении слова
        void onDeleteWord(Uri wordUri);
    }

    private static final int WORD_LOADER = 0;

    // Сообщает MainActivity о действии во фрагменте
    private WordDetailsFragmentListener listener;
    // Uri выбранного слова
    private Uri wordUri;

    private TextView enWord;
    private TextView ruWord;
    private TextView dictionary;

    public WordDetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_word_details, container, false);

        enWord = (TextView) view.findViewById(R.id.en_word);
        ruWord = (TextView) view.findViewById(R.id.ru_word);
        dictionary = (TextView) view.findViewById(R.id.dictionary);

        FloatingActionButton editWordFAB = (FloatingActionButton) view.findViewById(R.id.editWordFAB);
        editWordFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEditWord(wordUri);
            }
        });

        Bundle arguments = getArguments();
        if (arguments != null) {
            wordUri = arguments.getParcelable(MainActivity.WORD_URI);
            getLoaderManager().initLoader(WORD_LOADER, null, this);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_word_details);
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

//    // Удаление слова
//    private void deleteWord() {
//        // FragmentManager используется для отображения confirmDelete
//        confirmDelete.show(getFragmentManager(), "confirm delete");
//    }
//
//    // DialogFragment для подтверждения удаления слова
//    private final DialogFragment confirmDelete = new DialogFragment(){
//        // Создание объекта AlertDialog и его возвращение
//        @NonNull
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//
//            builder.setTitle(R.string.confirm_delete_title);
//            builder.setMessage(R.string.confirm_delete_message);
//
//            builder.setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    // объект ContentResolver используется для вызова delete в DatabaseContentProvider.
//                    // последние два аргумента равны null, потому что идентификатор записи удаляемого
//                    // контакта встроен в URI
//                    getActivity().getContentResolver().delete(wordUri, null, null);
//                    listener.onDeletWord();// Оповещение слушателя
//                }
//            });
//
//            builder.setNegativeButton(R.string.button_cancel, null);
//            return builder.create();
//        }
//    };

}
