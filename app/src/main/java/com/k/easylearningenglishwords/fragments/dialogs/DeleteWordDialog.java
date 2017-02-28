package com.k.easylearningenglishwords.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.k.easylearningenglishwords.MainActivity;
import com.k.easylearningenglishwords.R;
import com.k.easylearningenglishwords.data.DatabaseDescription;


public class DeleteWordDialog extends DialogFragment {

    public interface DeleteWordDialogListener{
        void updateDateOfChangeDictionary(String dictionaryName);
    }

    private DeleteWordDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments().size() == 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.exept_empty_saved_instance_state));
        }

        final Uri wordUri = getArguments().getParcelable(MainActivity.WORD_URI);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setIcon(R.drawable.ic_warning_yellow_24dp)
                .setCancelable(false)
                .setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Cursor cursor = getActivity().getContentResolver().query(
                                wordUri,
                                null,
                                null,
                                null,
                                null);
                        getActivity().getContentResolver().delete(
                                wordUri,
                                null,
                                null);

                        cursor.moveToFirst();
                        String str =cursor.getString(cursor.getColumnIndex(DatabaseDescription.Words.COLUMN_DICTIONARY));
                        listener.updateDateOfChangeDictionary(str);
                        getFragmentManager().popBackStack();
                    }
                })
                .setNegativeButton(R.string.button_cancel, null);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (DeleteWordDialogListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
