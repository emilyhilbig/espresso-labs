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
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.util.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import espressolabs.meala.model.ShoppingListItem;

import static android.app.Activity.RESULT_OK;


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
        startActivityForResult(intent, ScannerActivity.BARCODE_REQUEST);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ScannerActivity.BARCODE_REQUEST:
                if (resultCode == RESULT_OK) { handleScannedBarcode(data); }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void handleScannedBarcode(Intent data){
        String barcode = data.getStringExtra("barcode");

        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = "https://api.edamam.com/api/food-database/parser?app_key=e473e92b642d9dd7af3c456a06067234&app_id=1449a83d&upc="
                + barcode;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                (String response) -> {
                    JSONObject object = null;
                    try {
                        object = new JSONObject(response);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
//                    Log.d("object",object.toString());
                    String item = parseResponse(object);
                    if(!item.isEmpty()){
                        pantryFragment.createArchivedItem(item,"",0,false);
                    }
//                    Toast.makeText(getContext(), "Response is: " +object, Toast.LENGTH_LONG).show();
                },
                (VolleyError error) -> {
                    Toast.makeText(getContext(), "UPC not found!", Toast.LENGTH_LONG).show();
                    // delay?
                    shoppingListFragment.openAddItemDialog();
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private String parseResponse(JSONObject object){
        if(object == null){
            return "";
        }
        String name = "";
        try {
            name = ((JSONObject) object.getJSONArray("hints").get(0)).getJSONObject("food").getString("label");
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        return name;
    }
}