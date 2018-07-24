package espressolabs.meala;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import espressolabs.meala.dialog.ItemDialogFragment;
import espressolabs.meala.dialog.NameDialogFragment;
import espressolabs.meala.dialog.UsersDialogFragment;
import espressolabs.meala.firebase.FirebaseDatabaseConnectionWatcher;
import espressolabs.meala.model.MealListItem;
import espressolabs.meala.runnables.ActiveUsersUpdater;
import espressolabs.meala.runnables.PresenceUpdater;
import espressolabs.meala.ui.interaction.ItemAnimator;
import espressolabs.meala.ui.interaction.ItemTouchHelperCallback;
import espressolabs.meala.ui.interaction.PlanningListAdapter;
import espressolabs.meala.utils.FCMHelper;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlanningListFragment} factory method to
 * create an instance of this fragment.
 */
public class PlanningListFragment extends Fragment {

    public static final String TAG = "PlanningListFragment";
    private ArrayList<MealListItem> items;

    private PlanningListAdapter adapter;
    private RecyclerView listView;
    private int day;
    private String uid = "null";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        items = new ArrayList<MealListItem>();
        adapter = new PlanningListAdapter(items);

        // setup Firebase user
        // TODO this isn't used yet
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }
    }

    public static PlanningListFragment newInstance(int day) {
        PlanningListFragment fragment = new PlanningListFragment();
        Bundle args = new Bundle();
        args.putInt("day", day);
        fragment.setArguments(args);

        return fragment;
    }

    public void addMeal(MealListItem meal) {
        Log.v(TAG, "add meal called");
        if (adapter != null) {
            Log.v(TAG, "Added meal");
            adapter.addItem(meal);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.planner_list, container, false);
        Bundle args = getArguments();
        day = args.getInt("day");

        // Set the adapter
        Context context = view.getContext();
        listView = view.findViewById(R.id.planner_list);
        listView.setLayoutManager(new LinearLayoutManager(context));
        if (!items.isEmpty()) {
       //     listView.findViewById(R.id.loading_planning_list).setVisibility(View.INVISIBLE);
        }

        String dayText = Integer.toString(day);
    //    ((TextView) view.findViewById(R.id.planner_list_title)).setText(dayText);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Context context = getContext();

        // Setup RecyclerView
        listView = view.findViewById(R.id.planner_list);
        listView.setLayoutManager(new LinearLayoutManager(context));

       // final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
       //         (this, R.layout.planner_list, items);

        // Setup adapter
        adapter = new PlanningListAdapter(items);
        listView.setAdapter(adapter);
        listView.setItemAnimator(new ItemAnimator(context, adapter));
    }
}