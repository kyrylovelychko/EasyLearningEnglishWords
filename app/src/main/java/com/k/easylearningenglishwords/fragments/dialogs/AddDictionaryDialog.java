package com.k.easylearningenglishwords.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.data.DatabaseDescription;
import com.k.easylearningenglishwords.data.DatabaseDescription.Dictionaries;

import java.util.Date;


public class AddDictionaryDialog extends DialogFragment {

    EditText newDictionaryName;
    private CoordinatorLayout coordinatorLayout;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinatorLayout);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_dictionary, null);
        builder.setTitle(R.string.add_dictionary_request)
                .setView(view)
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.button_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        newDictionaryName = (EditText) getDialog().findViewById(R.id.dictionaryNameEditText);

                        ContentValues cv = new ContentValues();
                        cv.put(Dictionaries.COLUMN_NAME, newDictionaryName.getText().toString());
                        cv.put(Dictionaries.COLUMN_DATE_OF_CHANGE, new Date().getTime()/1000);
                        Uri newDictionaryUri = getActivity().getContentResolver().insert(DatabaseDescription.Dictionaries.CONTENT_URI, cv);
                        if (newDictionaryUri != null) {
                            Snackbar.make(coordinatorLayout, R.string.dictionary_added, Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(coordinatorLayout, R.string.dictionary_not_added, Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .setCancelable(false);


        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}
