package espressolabs.meala;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

import espressolabs.meala.model.ShoppingListItem;
import espressolabs.meala.model.StatisticListItem;
import espressolabs.meala.ui.interaction.ItemAnimator;
import espressolabs.meala.ui.interaction.StatisticRecyclerViewAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatisticFragment extends Fragment {

    //public static final String TAG = "StatisticFragment";
    private StatisticRecyclerViewAdapter adapter;
    private RecyclerView listView;
    private Boolean isDaily = true; // use for changing data for daily and weekly view
    private OnStatisticClickListener mListener;
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private static final int STATE_STARTING = -1;
    private static final int STATE_LOADING = 0;
    private static final int STATE_EMPTY = 1;
    private static final int STATE_LIST = 2;
    private int state = STATE_STARTING;

    public StatisticFragment() {
        // Required empty public constructor
    }

/*
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.statistic_list, null);

        /*
        View view = inflater.inflate(R.layout.statistic_list, container, false);
        Bundle args = getArguments();
        // isDaily = args.getBoolean("isDaily");
        //String dayText = Integer.toString(day);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.statistic_list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        adapter = new StatisticRecyclerViewAdapter(mListener, Glide.with(this), mColumnCount);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new ItemAnimator(context, adapter));

        return view;
        */
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Context context = getContext();

        // Setup RecyclerView
        listView = view.findViewById(R.id.statistic_list);
        listView.setLayoutManager(new LinearLayoutManager(context));

        // Setup adapter
        adapter = new StatisticRecyclerViewAdapter(mListener, Glide.with(this), mColumnCount);
        //adapter.setViewSize(prefs.getInt(PREFS_VIEW_SIZE, -1));
        listView.setAdapter(adapter);
        listView.setItemAnimator(new ItemAnimator(context, adapter));
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                updateUI();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                updateUI();
            }

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

        // Load initial data
        ArrayList<StatisticListItem> items = new ArrayList<>(1);
        items.add(new StatisticListItem("%",50));
        items.add(new StatisticListItem("%",90));
        adapter.setItems(items);

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnStatisticClickListener) {
            mListener = (OnStatisticClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStatisticClickListener");
        }*/
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
    public interface OnStatisticClickListener {
        //void onListFragmentInteraction(RecipeContent.Recipe item);
    }

    private void setUIState(int newState) {
        View view = getView();
        View[] views = new View[]{
                view.findViewById(R.id.loading_statistic_list),
                view.findViewById(R.id.empty_statistic_list),
                view.findViewById(R.id.statistic_list)
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
                    View icon = view.findViewById(R.id.empty_shopping_list_icon);
                    View text = view.findViewById(R.id.empty_shopping_list_text);

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

}
