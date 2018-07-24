package espressolabs.meala;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class GroceryFragment extends Fragment {
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabScan;

    public ShoppingListFragment shoppingListFragment;
    public PantryFragment pantryFragment;

    private void animateFab(int position) {
        switch (position) {
            case 0:
                fabAdd.show();
                fabScan.hide();
                break;
            case 1:
                fabScan.show();
                fabAdd.hide();
                break;

            default:
                fabAdd.show();
                fabScan.hide();
                break;
        }
    }


    public void Recipe() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_grocery, container, false);

        // Set up toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView toolbar_title = view.findViewById(R.id.toolbar_title);
        toolbar_title.setText("Grocery");

        // Setting ViewPager for each Tabs
        ViewPager viewPager = view.findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        // Set Tabs inside Toolbar
        TabLayout tabs = view.findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        // Create ViewPager change listener
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                animateFab(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        // Set floating action button
        fabAdd = view.findViewById(R.id.fab_add);
        fabScan = view.findViewById(R.id.fab_scan);

        // FAB listeners
        fabAdd.setOnClickListener(v -> shoppingListFragment.openAddItemDialog());
        fabScan.setOnClickListener(v -> openScanner(v));

        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());
        shoppingListFragment = new ShoppingListFragment();
        pantryFragment = new PantryFragment();
        adapter.addFragment(shoppingListFragment, "Shopping List");
        adapter.addFragment(pantryFragment, "Pantry");
        viewPager.setAdapter(adapter);
    }

    public void openScanner(View view){
        Toast.makeText(getContext(), "Opening scanner", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getContext(), ScannerActivity.class);
        startActivity(intent);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}