package espressolabs.meala.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;

import java.lang.reflect.Array;
import java.util.ArrayList;

import espressolabs.meala.R;
import espressolabs.meala.model.MealListItem;
import espressolabs.meala.model.RecipeItem;
import espressolabs.meala.utils.Utils;

public class ShortlistDialogFragment extends AppCompatDialogFragment {

    private ShortlistDialogListener listener;
    private ArrayList<String> shortlist;
    private Spinner shortlistOptionsList;
    private Spinner mealTypeSpinner;

    public static ShortlistDialogFragment newInstance(ArrayList<String> recipes) {
        ShortlistDialogFragment f = new ShortlistDialogFragment();

        Bundle args = new Bundle();
        args.putStringArrayList("recipes", recipes);
        f.setArguments(args);

        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_shortlist, null);

        shortlist = getArguments().getStringArrayList("recipes");

        shortlistOptionsList = view.findViewById(R.id.dialog_shortlist_options);
        ArrayAdapter arrayAdapter = new ArrayAdapter(
                getActivity(),
                android.R.layout.simple_spinner_item,
                shortlist);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shortlistOptionsList.setAdapter(arrayAdapter);

        mealTypeSpinner = view.findViewById(R.id.dialog_shortlist_meal_time);
        ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter(
                getActivity(),
                android.R.layout.simple_spinner_item,
                MealListItem.Meal.values());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealTypeSpinner.setAdapter(spinnerAdapter);

        builder.setView(view)
                .setTitle(R.string.dialog_shortlist_title)
                .setCancelable(true)
                .setNegativeButton(getResources().getString(android.R.string.cancel), (dialogInterface, i) -> Utils.toggleKeyboard(getActivity()))
                .setPositiveButton(R.string.add, (dialogInterface, id) -> {
                    Dialog dialog = (Dialog) dialogInterface;

                    shortlistOptionsList = dialog.findViewById(R.id.dialog_shortlist_options);
                    mealTypeSpinner = dialog.findViewById(R.id.dialog_shortlist_meal_time);

                    String recipe = (String)shortlistOptionsList.getSelectedItem();
                    MealListItem.Meal mealType = MealListItem.Meal.values()[mealTypeSpinner.getSelectedItemPosition()];

                    listener.onDialogAddItem(
                            recipe,
                            mealType
                    );
                    Utils.toggleKeyboard(getActivity());
                });

        Dialog d = builder.create();

        d.setOnShowListener(dialogInterface -> Utils.toggleKeyboard(getActivity()));

        return d;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        listener = (ShortlistDialogListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        listener = null;
    }

    public interface ShortlistDialogListener {
        void onDialogAddItem(String recipe, MealListItem.Meal mealType);
    }

}
