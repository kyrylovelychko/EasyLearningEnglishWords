package com.velychko.kyrylo.mydictionaries.ui.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.velychko.kyrylo.mydictionaries.R;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription.Dictionaries;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription.Words;
import com.velychko.kyrylo.mydictionaries.utils.Constants;


public class DeleteDictionaryDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null && getArguments().size() == 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.exc_empty_saved_instance_state));
        }

        final String dictionaryName = getArguments().getString(Constants.ARGS_DICTIONARY_NAME);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_confirm_delete_dictionary)
                .setMessage(R.string.dialog_message_delete_dictionary)
                .setIcon(R.drawable.ic_warning_yellow_24dp)
                .setPositiveButton(R.string.dialog_button_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().getContentResolver().delete(
                                Dictionaries.CONTENT_URI,
                                Dictionaries.COLUMN_NAME + "=?",
                                new String[]{dictionaryName});
                        getActivity().getContentResolver().delete(
                                Words.CONTENT_URI,
                                Words.COLUMN_DICTIONARY + "=?",
                                new String[]{dictionaryName});
                        getFragmentManager().popBackStack();
                    }
                })
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .setCancelable(false);

        return builder.create();
    }
}
