package com.k.easylearningenglishwords.fragments.dialogs;

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

import com.k.easylearningenglishwords.MainActivity;
import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.data.DatabaseDescription;

import java.util.Date;


public class RenameDictionaryDialog extends DialogFragment {

    public interface RenameDictionaryDialoglistener{
        void onSelectDictionary(Uri dictionaryUri);
    }


    EditText dictionaryName;
    String name;
    String oldDictionaryName;

    private CoordinatorLayout coordinatorLayout;
    private RenameDictionaryDialoglistener listener;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments().size() == 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.exept_empty_saved_instance_state));
        }
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_dictionary, null);
        dictionaryName = (EditText) view.findViewById(R.id.dictionaryNameEditText);
        dictionaryName.setText(getArguments().getString(MainActivity.DICTIONARY_NAME));
        oldDictionaryName = dictionaryName.getText().toString();

        coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinatorLayout);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Название словаря")
                .setView(view)
                .setCancelable(false)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        name = dictionaryName.getText().toString().trim();
                        if (oldDictionaryName.equals(name)) {
                            Snackbar.make(coordinatorLayout, "Название словаря не изменилось", Snackbar.LENGTH_LONG).show();
                            return;
                        }

                        refreshInDictionariesTable();
                    }
                });

        return builder.create();
    }


    private void refreshInDictionariesTable() {
//        Cursor cursor = new DatabaseHelper(getActivity()).getReadableDatabase().
        Cursor cursor = getActivity().getContentResolver().
                query(DatabaseDescription.Dictionaries.CONTENT_URI,
                        null,
                        DatabaseDescription.Dictionaries.COLUMN_NAME + "=?",
                        new String[]{oldDictionaryName},
                        null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            long id = cursor.getInt(cursor.getColumnIndex(DatabaseDescription.Dictionaries._ID));
            ContentValues cv = new ContentValues();
//            cv.put(DatabaseDescription.Dictionaries._ID, id);
            cv.put(DatabaseDescription.Dictionaries.COLUMN_NAME, name);
            cv.put(DatabaseDescription.Dictionaries.COLUMN_DATE_OF_CHANGE, new Date().getTime() / 1000);
            int updatedRows = getActivity().getContentResolver().update(
                    DatabaseDescription.Dictionaries.buildDictionariesUri(id),
                    cv,
                    null,
                    null);
            if (updatedRows > 0) {
                Uri dictionaryUri = DatabaseDescription.Dictionaries.buildDictionariesUri(id);
                refreshInWordsTable(dictionaryUri);
            }
        }
    }

    private void refreshInWordsTable(Uri dictionaryUri) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseDescription.Words.COLUMN_DICTIONARY, name);
//        cv.put(DatabaseDescription.Words.COLUMN_DATE_OF_CHANGE, new Date().getTime() / 1000);
        int updatedRows = getActivity().getContentResolver().update(
                DatabaseDescription.Words.CONTENT_URI,
                cv,
                DatabaseDescription.Words.COLUMN_DICTIONARY + "=?",
                new String[]{oldDictionaryName});
        if (updatedRows > 0) {
            Snackbar.make(coordinatorLayout, "Название словаря обновлено", Snackbar.LENGTH_LONG).show();
            getFragmentManager().popBackStack();
            listener.onSelectDictionary(dictionaryUri);
        } else {
            Snackbar.make(coordinatorLayout, "Название словаря не обновлено", Snackbar.LENGTH_LONG).show();
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (RenameDictionaryDialoglistener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
