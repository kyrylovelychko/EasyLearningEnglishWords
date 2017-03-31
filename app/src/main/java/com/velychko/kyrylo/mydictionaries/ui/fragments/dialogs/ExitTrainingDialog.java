package com.velychko.kyrylo.mydictionaries.ui.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.velychko.kyrylo.mydictionaries.R;
import com.velychko.kyrylo.mydictionaries.ui.activities.TranslateWordTrainingActivity;


public class ExitTrainingDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_exit_training)
                .setMessage(R.string.dialog_message_confirm_exit_training)
                .setIcon(R.drawable.ic_warning_yellow_24dp)
                .setPositiveButton(R.string.dialog_button_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TranslateWordTrainingActivity activity =
                                (TranslateWordTrainingActivity) getActivity();
                        activity.setDoExit(true);
                        getActivity().onBackPressed();
                    }
                })
                .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TranslateWordTrainingActivity activity =
                                (TranslateWordTrainingActivity) getActivity();
                        activity.setDoExit(false);
                    }
                })
                .setCancelable(false);
        return builder.create();
    }
}
