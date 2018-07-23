package espressolabs.meala;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.reflect.Field;

import espressolabs.meala.dialog.ItemDialogFragment;
import espressolabs.meala.dialog.NameDialogFragment;
import espressolabs.meala.model.RecipeItem;


class BottomNavigationViewHelper {

    @SuppressLint("RestrictedApi")
    public static void removeShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                item.setChecked(item.getItemData().isChecked());
            }

        } catch (NoSuchFieldException e) {
            Log.e("ERROR NO SUCH FIELD", "Unable to get shift mode field");
        } catch (IllegalAccessException e) {
            Log.e("ERROR ILLEGAL ALG", "Unable to change value of shift mode");
        }
    }
}


public class MainActivity extends AppCompatActivity
        implements RecipeListFragment.OnRecipeClickListener, NameDialogFragment.NameDialogListener, ItemDialogFragment.ItemDialogListener, PopupMenu.OnMenuItemClickListener {

    Fragment mCurFragment;
    Fragment mPrevFragment;

    FragmentManager fragmentManager;

    Fragment homeFragment;
    Fragment plannerFragment;
    Fragment recipesFragment;
    Fragment groceryFragment;
    Fragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Set up bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        BottomNavigationViewHelper.removeShiftMode(bottomNav);

        fragmentManager = getSupportFragmentManager();

        // Create fragments
        homeFragment = new HomeFragment();
        plannerFragment = new PlannerFragment();
        recipesFragment = new RecipeFragment();
        groceryFragment = new GroceryFragment();
        profileFragment = new ProfileFragment();

        // Start with homeFragment
        fragmentManager.beginTransaction().add(R.id.fragment_container, homeFragment).commit();
        mCurFragment = homeFragment;
    }


    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    mPrevFragment = mCurFragment;

                    switch (item.getItemId()) {
                        case R.id.navigation_home:
                            mCurFragment = homeFragment;
                            break;
                        case R.id.navigation_planner:
                            mCurFragment = plannerFragment;
                            break;
                        case R.id.navigation_recipes:
                            mCurFragment = recipesFragment;
                            break;
                        case R.id.navigation_grocery:
                            mCurFragment = groceryFragment;
                            break;
                        case R.id.navigation_profile:
                            mCurFragment = profileFragment;
                            break;
                    }

                    // Check if fragment was added previously
                    if(mCurFragment.getId() != 0 && fragmentManager.findFragmentById(mCurFragment.getId()) != null) {
                        // Check if one fragment was added previously
                        if(mPrevFragment != null) {
                            fragmentManager.beginTransaction().hide(mPrevFragment).show(mCurFragment).commit();
                        } else {
                            fragmentManager.beginTransaction().show(mCurFragment).commit();
                        }
                    } else {
                        // Check if one fragment was added previously
                        if(mPrevFragment != null) {
                            fragmentManager.beginTransaction().add(R.id.fragment_container, mCurFragment).hide(mPrevFragment).commit();
                        } else {
                            fragmentManager.beginTransaction().add(R.id.fragment_container, mCurFragment).commit();
                        }
                    }

                    setTitle(item.getTitle());

                    return true;
                }
            };


    public void showProfilePopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);

        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.profile);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();

                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            case R.id.view_profile:
                startActivity(new Intent(this, ProfileFragment.class));
                finish();
                return true;
            default:
                return false;
        }
    }

    // Listeners

    @Override
    public void onListFragmentInteraction(RecipeItem recipe) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(recipe.getLink()));
        startActivity(browserIntent);
    }


    @Override
    public void onDialogAddItem(String inputName, String inputDescription, int inputPrice, boolean inputUrgent) {
        String itemName = inputName.trim();
        String itemDescription = inputDescription.trim();

        if (itemName.length() > 0) {
            ((GroceryFragment)groceryFragment).shoppingListFragment.createItem(itemName, itemDescription, inputPrice, inputUrgent);
        }
    }

    @Override
    public void onDialogChangeName(String input) {
        String name = input.trim();

        if (name.length() > 0) {
            ((GroceryFragment)groceryFragment).shoppingListFragment.changeName(name);
        }
    }

}