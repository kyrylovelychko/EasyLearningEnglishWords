package com.velychko.kyrylo.mydictionaries.ui.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
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

import com.velychko.kyrylo.mydictionaries.R;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription.Dictionaries;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseHelper;

import java.util.Date;


public class AddDictionaryDialog extends DialogFragment {

    EditText dictionaryName;
    private CoordinatorLayout coordinatorLayout;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinatorLayout);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_dictionary, null);
        builder.setTitle(R.string.title_add_dictionary_request)
                .setView(view)
                .setCancelable(false)
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .setPositiveButton(R.string.dialog_button_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dictionaryName = (EditText) getDialog().findViewById(R.id.dictionaryNameEditText);

                        // Ищем словарь (в таблице словарей) с именем, которое ввел пользователь.
                        // Если нашли, значит словарь с таким именем уже есть - сообщаем пользователю,
                        // новый не добавляем. Если нет, добавляем новый словарь.
                        Cursor dictionaryExistCursor = new DatabaseHelper(getActivity()).getReadableDatabase().
                                query(Dictionaries.TABLE_NAME,
                                        null,
                                        Dictionaries.COLUMN_NAME + "=?",
                                        new String[]{""+ dictionaryName.getText().toString().trim()},
                                        null,
                                        null,
                                        null);
                        if (dictionaryExistCursor.getCount() == 0) {
                            ContentValues cv = new ContentValues();
                            cv.put(Dictionaries.COLUMN_NAME, dictionaryName.getText().toString().trim());
                            cv.put(Dictionaries.COLUMN_DATE_OF_CHANGE, new Date().getTime() / 1000);
                            Uri newDictionaryUri = getActivity().getContentResolver().insert(DatabaseDescription.Dictionaries.CONTENT_URI, cv);
                            if (newDictionaryUri != null) {
                                Snackbar.make(coordinatorLayout, R.string.snack_dictionary_added, Snackbar.LENGTH_LONG).show();
                            } else {
                                Snackbar.make(coordinatorLayout, R.string.snack_dictionary_not_added, Snackbar.LENGTH_LONG).show();
                            }
                        } else {
                            Snackbar.make(coordinatorLayout, R.string.snack_dictionary_has_existed, Snackbar.LENGTH_LONG).show();
                        }

                    }
                });


        return builder.create();
    }

    // Показываем клавиатуру сразу, чтобы пользователь вводил название нового словаря
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}
