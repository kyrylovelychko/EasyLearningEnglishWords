package com.k.easylearningenglishwords.ui.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.k.easylearningenglishwords.ui.activities.MainActivity;
import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.data.SQLite.DatabaseDescription;

import java.util.Date;


public class RenameDictionaryDialog extends DialogFragment {

    public interface RenameDictionaryDialogListener {
        void onSelectDictionary(Uri dictionaryUri);
    }

    EditText dictionaryName;
    // Новое имя словаря
    String newName;
    // Старое имя словаря
    String oldDictionaryName;

    private CoordinatorLayout coordinatorLayout;
    private RenameDictionaryDialogListener listener;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Из Bundle Необходимо получить текущее имя словаря, прежде чем его менять на другое.
        // Если не получили - плохо.
        if (getArguments().size() == 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.exc_empty_saved_instance_state));
        }

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_dictionary, null);
        dictionaryName = (EditText) view.findViewById(R.id.dictionaryNameEditText);
        // Проставляем EditText текущее имя словаря
        dictionaryName.setText(getArguments().getString(MainActivity.DICTIONARY_NAME));
        // Сохраняем старое (пока еще текущее) имя словаря
        oldDictionaryName = dictionaryName.getText().toString();

        coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinatorLayout);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.title_dictionary_name)
                .setView(view)
                .setCancelable(false)
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Получем новое название словаря, которое ввел и захотел сохранить пользователь
                        newName = dictionaryName.getText().toString().trim();
                        // Сравниваем старое и новое название. Если не отличаются -
                        // уведомляем пользователя и ничего не делаем
                        if (oldDictionaryName.equals(newName)) {
                            Snackbar.make(coordinatorLayout, R.string.snack_dictionary_name_not_changed, Snackbar.LENGTH_LONG).show();
                            return;
                        }

                        // Обновляем в таблице словарей дату последнего изменения для
                        // переименовываемого словаря
                        refreshInDictionariesTable();
                    }
                });

        return builder.create();
    }

    // Обновление в таблице словарей дату последнего изменения для переименовываемого словаря
    private void refreshInDictionariesTable() {
        // Находим словарь по старому имени в таблице словарей
        // Это нужно для получения _ID этого словаря - потом по ID будем обновлять
        Cursor cursor = getActivity().getContentResolver().
                query(DatabaseDescription.Dictionaries.CONTENT_URI,
                        null,
                        DatabaseDescription.Dictionaries.COLUMN_NAME + "=?",
                        new String[]{oldDictionaryName},
                        null);
        if (cursor.getCount() != 0) {
            // Если такой словарь найден, обновляем по полю _ID для него поля названия и даты
            // последнего изменения
            cursor.moveToFirst();
            long id = cursor.getInt(cursor.getColumnIndex(DatabaseDescription.Dictionaries._ID));
            ContentValues cv = new ContentValues();
            cv.put(DatabaseDescription.Dictionaries.COLUMN_NAME, newName);
            cv.put(DatabaseDescription.Dictionaries.COLUMN_DATE_OF_CHANGE, new Date().getTime() / 1000);
            int updatedRows = getActivity().getContentResolver().update(
                    DatabaseDescription.Dictionaries.buildDictionariesUri(id),
                    cv,
                    null,
                    null);
            if (updatedRows > 0) {
                // Если удалось обновить информацию в таблице словарей,
                // вызываем метод обновления таблицы слов - там тоже у слов надо обновить поле словаря
                Uri dictionaryUri = DatabaseDescription.Dictionaries.buildDictionariesUri(id);
                refreshInWordsTable(dictionaryUri);
            }
        }
    }

    // Обновление поля имени словаря в таблице слов.
    // Обновление происходит по старому имени словаря
    private void refreshInWordsTable(Uri dictionaryUri) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseDescription.Words.COLUMN_DICTIONARY, newName);
        int updatedRows = getActivity().getContentResolver().update(
                DatabaseDescription.Words.CONTENT_URI,
                cv,
                DatabaseDescription.Words.COLUMN_DICTIONARY + "=?",
                new String[]{oldDictionaryName});
        if (updatedRows > 0) {
            Snackbar.make(coordinatorLayout, R.string.snack_dictionary_name_changed, Snackbar.LENGTH_LONG).show();
        }
        getFragmentManager().popBackStack();
        listener.onSelectDictionary(dictionaryUri);

    }

    // Сразу достаем для пользователя клавиатуру
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    // Присоединяем слушатель для связи с MainActivity
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (RenameDictionaryDialogListener) context;
    }

    // Отсоединяем слушатель для связи с MainActivity
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}