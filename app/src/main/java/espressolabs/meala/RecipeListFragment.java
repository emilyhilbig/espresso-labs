package espressolabs.meala;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import espressolabs.meala.model.RecipeContent;
import espressolabs.meala.model.RecipeContent.Recipe;
import espressolabs.meala.utils.ShuffleList;

import static java.sql.DriverManager.println;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnRecipeClickListener}
 * interface.
 */
public class RecipeListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout refreshLayout;
    private final String URL_DATA = "http://food2fork.com/api/search?key=b5dcbe5da1e613679d57211a729e279e";
    private List<Recipe> trendingRecipes;
    private RecipeRecyclerViewAdapter adapter;
    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 1;
    private OnRecipeClickListener mListener;

    private void loadUrlData() {
//        final ProgressDialog progressDialog = new ProgressDialog(getContext());
//        progressDialog.setMessage("Loading...");
//        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                URL_DATA, response -> {
    //                progressDialog.dismiss();

                    trendingRecipes.clear();

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray array = jsonObject.getJSONArray("recipes");
                        println("Got array");
                        for (int i = 0; i < array.length(); i++) {
                            println("Got obj");
                            JSONObject jo = array.getJSONObject(i);
                            Recipe recipe = new Recipe(i, jo.getString("title"), jo.getString("publisher"),
                                    jo.getString("image_url"), jo.getString("source_url"));
                            trendingRecipes.add(recipe);
                        }
                        ShuffleList.shuffleList(trendingRecipes);
                        adapter.updateRecipeList(trendingRecipes);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        refreshLayout.setRefreshing(false);
                    }
                }, error -> {
                    Toast.makeText(getContext(), "Currently unable to fetch data", Toast.LENGTH_SHORT).show();
                    refreshLayout.setRefreshing(false);
                });

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(stringRequest);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        trendingRecipes = new ArrayList<>();

    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecipeListFragment() {
    }

    public static RecipeListFragment newInstance(int columnCount) {
        RecipeListFragment fragment = new RecipeListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.refresh_recycler_view, container, false);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.itemsRecyclerView);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        adapter = new RecipeRecyclerViewAdapter(RecipeContent.ITEMS, mListener, Glide.with(this), mColumnCount);
        recyclerView.setAdapter(adapter);

        // Setup refresh layout
        refreshLayout = view.findViewById(R.id.swipe_container);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        Log.d("faaaaaaa", "here");
        onRefresh();

        return view;
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(() -> {
            refreshLayout.setRefreshing(true);
            loadUrlData();
        }, 0);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRecipeClickListener) {
            mListener = (OnRecipeClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRecipeClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnRecipeClickListener {
        void onListFragmentInteraction(Recipe item);
    }

}
