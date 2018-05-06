package eaton.connor.coincollection;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.EditText;

/**
 * Created by Connor on 5/6/2018.
 */

public class SerialNumberDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.serial_number_input, null))
                // Add action buttons
                .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText SN = (EditText) getDialog().findViewById(R.id.search_sn);

                        EditText grade = (EditText) getDialog().findViewById(R.id.search_grade);
                        Intent intent = new Intent(getActivity(), ParsingActivity.class);
                        intent.putExtra(ParsingActivity.SerialNumber, SN.getText().toString());
                        intent.putExtra(ParsingActivity.Grade, grade.getText().toString());
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SerialNumberDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
