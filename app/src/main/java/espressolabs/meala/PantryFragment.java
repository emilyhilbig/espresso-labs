package espressolabs.meala;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import espressolabs.meala.ui.interaction.ShoppingListAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class PantryFragment extends ItemListFragment {

    public static final String TYPE = "Pantry Item";
    @Override
    protected String getItemType() { return TYPE;}

    @Override
    protected void swipeItemLeft(ShoppingListAdapter.ViewHolder vh){
        super.moveToShoppingList(vh);
    }

    @Override
    protected void swipeItemRight(ShoppingListAdapter.ViewHolder vh){
        super.deleteItem(vh);
    }

    public PantryFragment() {
    }

    //    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_pantry, container, false);
//    }

}
