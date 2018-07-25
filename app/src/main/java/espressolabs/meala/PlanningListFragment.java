package espressolabs.meala;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import espressolabs.meala.firebase.FirebaseDatabaseConnectionWatcher;
import espressolabs.meala.model.MealListItem;
import espressolabs.meala.runnables.PresenceUpdater;
import espressolabs.meala.ui.interaction.PlanningListAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlanningListFragment} factory method to
 * create an instance of this fragment.
 */
public class PlanningListFragment extends Fragment {

    public static final String TAG = "PlanningListFragment";
    private List<MealListItem> items;

    private PlanningListAdapter adapter;
    private RecyclerView listView;
    private int day;
    private String uid = "null";

    private FirebaseDatabaseConnectionWatcher fbDbConnectionWatcher;
    private FirebaseDatabase database;
    private PresenceUpdater presenceUpdater;
    private DatabaseReference planDatabase;
    private SharedPreferences prefs;

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

    public void setDay(int day){
        this.day = day;
    }

    public void addMeal(MealListItem meal) {
        planDatabase.child(Integer.toString(day)).child(meal.meal.toString()).setValue(meal);

        if (adapter != null) {
            adapter.addItem(meal);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.planner_list, container, false);

        // Set the listview
        Context context = view.getContext();
        listView = view.findViewById(R.id.planner_list);
        listView.setLayoutManager(new LinearLayoutManager(context));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Context context = getContext();

        // Setup adapter
        adapter = new PlanningListAdapter(items);
        listView.setAdapter(adapter);

        // Initializations
        database = FirebaseDatabase.getInstance();
        planDatabase = database.getReference().child("plan");

        ValueEventListener currentListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                day = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        planDatabase.child("current").addValueEventListener(currentListener);

        ValueEventListener planListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot plan = dataSnapshot.child(Integer.toString(day));
                if (plan.exists()) {
                    Log.v(TAG, plan.toString());

                    String snackKey = MealListItem.Meal.SNACK.toString();
                    String breakfastKey = MealListItem.Meal.BREAKFAST.toString();
                    String lunchKey = MealListItem.Meal.LUNCH.toString();
                    String dinnerKey = MealListItem.Meal.DINNER.toString();

                    String[] keys = {snackKey, breakfastKey, lunchKey, dinnerKey};

                    items.clear();
                    for (String key : keys){
                        MealListItem meal = plan.child(key).getValue(MealListItem.class);
                        if (meal != null) {
                            items.add(meal);
                        }
                    }

                    adapter.setItems(items);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        planDatabase.addValueEventListener(planListener);

        adapter.update();
    }
}