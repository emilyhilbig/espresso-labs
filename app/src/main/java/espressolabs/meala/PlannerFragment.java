package espressolabs.meala;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.List;

import espressolabs.meala.ui.interaction.PlanningListAdapter;
import espressolabs.meala.PlanningListFragment;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlannerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlannerFragment extends Fragment {
    private FloatingActionButton fabAdd;
    private int selectedDay;

    public PlannerFragment() {
        // Required empty public constructor
        selectedDay = 20180626;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if (calendar != null) {
            selectedDay = calendar.getCurrentDate().hashCode();
        }

        // Setting ViewPager for each Tabs
        ViewPager viewPager = view.findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        calendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                selectedDay = date.hashCode();
                viewPager.setCurrentItem(selectedDay/31);
            }
        });

        fabAdd = view.findViewById(R.id.fab_add_plan);
        fabAdd.setOnClickListener(v ->
            Toast.makeText(getContext(),  Integer.toString(selectedDay), Toast.LENGTH_SHORT).show()
        );

        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new PlannerFragment.Adapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentStatePagerAdapter {

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int day) {
            Fragment fragment = new PlanningListFragment();
            Bundle args = new Bundle();
            args.putInt("day", day);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return 31;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Day " + position;
        }
    }

}
