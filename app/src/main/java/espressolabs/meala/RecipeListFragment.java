package espressolabs.meala;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import espressolabs.meala.model.RecipeItem;
import espressolabs.meala.utils.ShuffleList;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import espressolabs.meala.firebase.FirebaseDatabaseConnectionWatcher;
import espressolabs.meala.utils.FCMHelper;

import static espressolabs.meala.utils.Constants.FAVORITES;
import static espressolabs.meala.utils.Constants.SHORTLIST;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnRecipeClickListener}
 * interface.
 */
public class RecipeListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
//    private final String trendingRecipesURL = "http://food2fork.com/api/search?key=b5dcbe5da1e613679d57211a729e279e";
    private final String trendingRecipesURL = "https://api.edamam.com/search?q=trending&app_id=bf3f8e76&app_key=b0dd996509e596490aa1f702c052c5f5";
    private List<RecipeItem> items;

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_LIST_TYPE = "list_type";
    private int mColumnCount = 1;
    private String list_type;
    private String uid = "null";

    private static final String TAG = "RecipeListFragment";
    private static final int STATE_STARTING = -1;
    private static final int STATE_LOADING = 0;
    private static final int STATE_EMPTY = 1;
    private static final int STATE_LIST = 2;
    private int state = STATE_STARTING;

    private SwipeRefreshLayout refreshLayout;
    private RecipeRecyclerViewAdapter adapter;
    private OnRecipeClickListener mListener;

    private DatabaseReference dbRef;
    private RecyclerView listView;
    private FirebaseDatabaseConnectionWatcher fbDbConnectionWatcher;
    private FirebaseDatabase database;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecipeListFragment() {
    }

    public static RecipeListFragment newInstance(int columnCount, String list_type) {
        RecipeListFragment fragment = new RecipeListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putString(ARG_LIST_TYPE, list_type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            list_type = getArguments().getString(ARG_LIST_TYPE);
        }

        items = new ArrayList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_list_with_refresh, container, false);

        // Set the adapter
        Context context = view.getContext();
        listView = view.findViewById(R.id.recycler_list);
        if (mColumnCount <= 1) {
            listView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            listView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        // Setup refresh layout
        refreshLayout = view.findViewById(R.id.swipe_container);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Context context = getContext();

        // Initializations
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        FCMHelper.init(context);

        setupConnectionWatcher();

        // Setup adapter
        adapter = new RecipeRecyclerViewAdapter(
                items,
                mListener,
                (v, item, list_to_add_to) -> {
                    item.interactedAt = System.currentTimeMillis();

                    // Write the new recipe's data simultaneously in the recupe list and the user's favorites list or shortlist.
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("/recipes/" + item.key, item);
                    updates.put("/user/" + uid + '/' + list_to_add_to + '/' + item.key, item);

                    dbRef.updateChildren(updates);

                    Toast.makeText(context, String.format("Added to %s!", list_to_add_to), Toast.LENGTH_SHORT).show();
                },
                Glide.with(this),
                mColumnCount);
        listView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                updateUI();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) { updateUI(); }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                updateUI();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                updateUI();
            }

            private void updateUI() {
                listView.getItemAnimator().isRunning(() -> {
                    if (adapter.getItemCount() > 0) {
                        setUIState(STATE_LIST);
                    } else {
                        setUIState(STATE_EMPTY);
                    }
                });
            }
        });

        // Load initial items
        onRefresh();

        if (list_type == FAVORITES || list_type == SHORTLIST) {
            setUIState(STATE_LOADING);
        } else {
            setUIState(STATE_LIST);
        }
    }

    private void loadTrendingRecipes(String requestURL) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                requestURL, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray results = jsonObject.getJSONArray("hits");

                items = new ArrayList<>(results.length());
                for (int i = 0; i < results.length(); i++) {
                    JSONObject recipeObject = results.getJSONObject(i).getJSONObject("recipe");
                    items.add(new RecipeItem(recipeObject));

                }
                ShuffleList.shuffleList(items);
                adapter.updateRecipeList(items);

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

    private void setupFirebaseLists() {
        Query userDataRef = dbRef.child("user").child(uid).child(list_type).orderByChild("interactedAt");

        userDataRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        refreshLayout.setRefreshing(true);

                        // Create list of items
                        items = new ArrayList<>((int) dataSnapshot.getChildrenCount());
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            RecipeItem item = RecipeItem.fromSnapshot(dsp);
                            items.add(item);
                        }

                        // Update items
                        adapter.updateRecipeList(items);

                        refreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Getting favorites or shortlist failed, log a message
                        Log.w(TAG, "loadFirebaseData:onCancelled:" + list_type, databaseError.toException());
                        // ...
                    }
                });

    }

    private void setupConnectionWatcher() {
        if (!BuildConfig.DEBUG) {
            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.snackbar_database_connecting, Snackbar.LENGTH_INDEFINITE).show();
        }
        fbDbConnectionWatcher = new FirebaseDatabaseConnectionWatcher();
        fbDbConnectionWatcher.addListener(new FirebaseDatabaseConnectionWatcher.OnConnectionChangeListener() {
            @Override
            public void onConnected() {
                if (!BuildConfig.DEBUG) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.snackbar_database_connected, Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDisconnected() {
                if (getView() != null) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.snackbar_database_reconnecting, Snackbar.LENGTH_INDEFINITE).show();
                }
            }
        });
    }

    private void setUIState(int newState) {
        View view = getView();
        View[] views = new View[]{
                view.findViewById(R.id.loading_list),
                view.findViewById(R.id.empty_list),
                view.findViewById(R.id.recycler_list)
        };

        int oldState = this.state;
        this.state = newState;

        if (oldState == STATE_STARTING) {
            for (View v : views) v.setVisibility(View.GONE);
            View newView = views[newState];
            newView.setVisibility(View.VISIBLE);
        } else {
            if (oldState != newState) {
                final View oldView = views[oldState];
                View newView = views[newState];

                oldView.setVisibility(View.VISIBLE);
                newView.setVisibility(View.VISIBLE);
                oldView.setAlpha(1);
                newView.setAlpha(0);

                int duration = 600;
                Interpolator interpolator = new AccelerateInterpolator();

                oldView.animate().alpha(0).setDuration(duration).setInterpolator(interpolator).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        oldView.setVisibility(View.GONE);
                    }
                }).start();
                newView.animate().alpha(1).setDuration(duration).setInterpolator(interpolator).setListener(null).start();

                if (newState == STATE_EMPTY) {
                    View icon = view.findViewById(R.id.empty_list_icon);
                    View text = view.findViewById(R.id.empty_list_text);

                    icon.setRotation(0);
                    icon.setTranslationX(-2000);
                    icon.animate().setDuration(2000).translationX(0).rotation(3 * 360).setInterpolator(new DecelerateInterpolator(2f)).start();

                    text.setAlpha(0);
                    text.setScaleX(0);
                    text.setScaleY(0);
                    text.animate().setStartDelay(1000).setDuration(800).alpha(1).scaleY(1).scaleX(1).setInterpolator(new OvershootInterpolator(2f)).start();
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.grocery, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onResume() {
        super.onResume();

        FirebaseDatabase.getInstance().goOnline();
    }

    @Override
    public void onPause() {
        super.onPause();

        FirebaseDatabase.getInstance().goOffline();
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(() -> {
            refreshLayout.setRefreshing(true);
            switch(list_type) {
                case FAVORITES:
                case SHORTLIST:
                    setupFirebaseLists(); // todo move this out of on refresh
                    break;
                default:
                    loadTrendingRecipes(trendingRecipesURL);
            }
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
        void onListFragmentInteraction(RecipeItem item);
    }
}
