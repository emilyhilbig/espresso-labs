package espressolabs.meala.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import espressolabs.meala.R;
import espressolabs.meala.runnables.ActiveUsersUpdater;

public class UsersDialogFragment extends AppCompatDialogFragment implements ActiveUsersUpdater.OnUserConnectionChanged {

    public static final String ARG_NAMES = "names";

    private ArrayAdapter<String> adapter;
    private static ActiveUsersUpdater activeUsersUpdater;

    public static UsersDialogFragment newInstance(ActiveUsersUpdater a) {
        activeUsersUpdater = a;
        ArrayList<String> names = activeUsersUpdater.getActives();

        UsersDialogFragment f = new UsersDialogFragment();

        Bundle args = new Bundle();
        args.putStringArrayList("names", names);
        f.setArguments(args);

        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        ListView view = (ListView) inflater.inflate(R.layout.dialog_users, null);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, getArguments().getStringArrayList(ARG_NAMES));
        view.setAdapter(adapter);

        builder.setView(view)
                .setTitle(R.string.dialog_users_title)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {

                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        activeUsersUpdater.addListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        activeUsersUpdater.removeListener(this);
        adapter = null;
    }

    @Override
    public void onConnected(String connectedName) {

    }

    @Override
    public void onDisconnected(String disconnectedName) {

    }

    @Override
    public void onActivesChanged(ArrayList<String> actives) {
        adapter.clear();
        adapter.addAll(actives);
    }

}
