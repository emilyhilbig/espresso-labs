package espressolabs.meala.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import espressolabs.meala.R;
import espressolabs.meala.utils.Utils;

public class NameDialogFragment extends AppCompatDialogFragment {

    public static final String ARG_DEFAULT_INPUT = "defaultInput";

    private NameDialogListener listener;

    public static NameDialogFragment newInstance(String defaultInput) {
        NameDialogFragment f = new NameDialogFragment();

        Bundle args = new Bundle();
        args.putString(ARG_DEFAULT_INPUT, defaultInput);
        f.setArguments(args);

        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_name, null);

        builder.setView(view)
                .setTitle(R.string.dialog_name_title)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(android.R.string.ok), (dialogInterface, id) -> {
                    Dialog dialog = (Dialog) dialogInterface;
                    EditText input = dialog.findViewById(R.id.input_name);
                    listener.onDialogChangeName(input.getText().toString());
                    Utils.toggleKeyboard(getActivity());
                });

        Dialog d = builder.create();

        d.setOnShowListener(dialogInterface -> {
            Utils.toggleKeyboard(getActivity());

            final Dialog dialog = (Dialog) dialogInterface;
            final EditText input = dialog.findViewById(R.id.input_name);
            if (input != null) {
                String defaultInput = getArguments().getString(ARG_DEFAULT_INPUT);
                if (defaultInput != null) {
                    input.setText(defaultInput);
                    input.setSelection(defaultInput.length());
                }
                input.setOnEditorActionListener((textView, keyCode, keyEvent) -> {
                    dialog.dismiss();
                    listener.onDialogChangeName(input.getText().toString());
                    return true;
                });
            }
        });

        return d;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        listener = (NameDialogListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        listener = null;
    }

    public interface NameDialogListener {
        void onDialogChangeName(String name);
    }

}
