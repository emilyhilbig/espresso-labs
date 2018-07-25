package espressolabs.meala;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;


import espressolabs.meala.dialog.ShortlistDialogFragment;
import espressolabs.meala.firebase.FirebaseDatabaseConnectionWatcher;
import espressolabs.meala.model.MealListItem;
import espressolabs.meala.model.RecipeItem;
import espressolabs.meala.runnables.PresenceUpdater;
import espressolabs.meala.ui.interaction.PlanningListAdapter;
import espressolabs.meala.PlanningListFragment;
import espressolabs.meala.utils.FCMHelper;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlannerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlannerFragment extends Fragment {
    private FloatingActionButton fabAdd;
    private PlannerFragment.Adapter adapter;
    private int selectedDay;
    private static final String TAG = "PlannerFragment";

    public final String FIREBASE_TOPIC_ADD_MEAL = "add_meal";
    public PlanningListFragment planningListFragment;

    private FirebaseDatabaseConnectionWatcher fbDbConnectionWatcher;
    private FirebaseDatabase database;
    private PresenceUpdater presenceUpdater;
    private DatabaseReference planDatabase;
    private SharedPreferences prefs;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private ArrayList<String> shortlist = new ArrayList<>();


    public PlannerFragment() {
        // Required empty public constructor
        Calendar jcal = Calendar.getInstance();
        int year = jcal.get(Calendar.YEAR);
        int month = jcal.get(Calendar.MONTH);
        int day = jcal.get(Calendar.DAY_OF_MONTH);

        selectedDay = year * 10000 + month * 100 + day;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getContext();

        // Initializations
        database = FirebaseDatabase.getInstance();
        planDatabase = database.getReference().child("plan");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();

        planDatabase.child("current").setValue(selectedDay);

        DatabaseReference userDatabase = database.getReference().child("user").child(uid);
        DatabaseReference shortlistDatabase = userDatabase.child("shortlist");

        ValueEventListener shortlistListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    Log.v(TAG, "Single " + dsp.toString());
                    RecipeItem item = RecipeItem.fromSnapshot(dsp);
                    shortlist.add(item.getTitle());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        shortlistDatabase.addValueEventListener(shortlistListener);

        // Subscribe to topic which broadcasts the new item notifications
        FirebaseMessaging.getInstance().subscribeToTopic(FIREBASE_TOPIC_ADD_MEAL);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_planner, container, false);

        // Set up toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Change toolbar text
        TextView toolbar_title = view.findViewById(R.id.toolbar_title);
        toolbar_title.setText(R.string.title_planner);

        // Set up calendar
        MaterialCalendarView calendar = view.findViewById(R.id.calendarView);
        calendar.setVisibility(View.VISIBLE);
        calendar.setTopbarVisible(false);
        View tabs = view.findViewById(R.id.tabs);
        tabs.setVisibility(View.GONE);

        // Setting ViewPager for each Tabs
        ViewPager viewPager = view.findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        // select the current day by default
        if (calendar != null) {
            Calendar jcal = Calendar.getInstance();
            calendar.setDateSelected(jcal.getTime(), true);

            selectedDay = calendar.getSelectedDate().hashCode();
            viewPager.setCurrentItem(calendar.getSelectedDate().getDay());
        }

        calendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                selectedDay = date.hashCode();
                Log.v(TAG, "Changing date selected to: " + Integer.toString(selectedDay));

                viewPager.setCurrentItem(date.getDay());
                planDatabase.child("current").setValue(selectedDay);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageScrollStateChanged(int state) { }

            @Override
            public void onPageSelected(int position) {
                CalendarDay c = calendar.getSelectedDate();
                Calendar jcal = Calendar.getInstance();
                jcal.set(c.getYear(), c.getMonth(), position);

                selectedDay = CalendarDay.from(jcal).hashCode();
                Log.v(TAG, "Changing date selected to: " + Integer.toString(selectedDay));


                calendar.setDateSelected(c, false);
                calendar.setDateSelected(jcal, true);
                planDatabase.child("current").setValue(selectedDay);
            }

        });

        fabAdd = view.findViewById(R.id.fab_add_plan);
        fabAdd.setOnClickListener(v -> {
            //Toast.makeText(getContext(), Integer.toString(selectedDay), Toast.LENGTH_SHORT).show();
            openAddMealDialog();
        });

        return view;
    }

    public void addMeal(String recipe, MealListItem.Meal mealType) {
        planDatabase.child(Integer.toString(selectedDay)).child(mealType.toString())
                .setValue(new MealListItem(
                        currentUser.getDisplayName(),
                        recipe,
                        "",
                        mealType));

    }

    public void openAddMealDialog() {
        Log.v(TAG, shortlist.toString());
        ShortlistDialogFragment dialogFragment = ShortlistDialogFragment.newInstance(shortlist);
        dialogFragment.show(getFragmentManager(), "ShortlistDialogFragment");
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new PlannerFragment.Adapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentStatePagerAdapter {
        List<PlanningListFragment> mFrags = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
            for (int i = 0; i <= 8; i++) { // create one for each day of the month, plus each end
                mFrags.add(PlanningListFragment.newInstance(i));
            }
        }

        @Override
        public PlanningListFragment getItem(int day) {
            int index = day % mFrags.size();

            PlanningListFragment fragment = mFrags.get(index);
            fragment.setDay(day);

            return fragment;
        }

        @Override
        public int getCount() {
            return 100;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Day " + position;
        }
    }
}
