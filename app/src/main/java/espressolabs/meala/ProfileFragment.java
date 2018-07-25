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
import android.util.Log;
import android.view.LayoutInflater;;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import espressolabs.meala.firebase.FirebaseDatabaseConnectionWatcher;
import espressolabs.meala.model.MacroListItem;
import espressolabs.meala.runnables.ActiveUsersUpdater;
import espressolabs.meala.runnables.PresenceUpdater;
import espressolabs.meala.ui.interaction.ItemAnimator;
import espressolabs.meala.ui.interaction.ProfileViewAdapter;
import espressolabs.meala.utils.FCMHelper;

public class ProfileFragment extends Fragment {
    private ProfileViewAdapter adapter;
    private RecyclerView listView;
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private ProfileFragment.OnStatisticClickListener mListener;
    public static final String TAG = "ProfileFragment";
    private FirebaseUser user = null;

    private static final int STATE_STARTING = -1;
    private static final int STATE_LOADING = 0;
    private static final int STATE_EMPTY = 1;
    private static final int STATE_LIST = 2;
    private int state = STATE_STARTING;

    private SharedPreferences prefs;
    private DatabaseReference dbRef;
    private FirebaseDatabaseConnectionWatcher fbDbConnectionWatcher;
    private FirebaseDatabase database;
    private PresenceUpdater presenceUpdater;
    public ActiveUsersUpdater activeUsersUpdater;
    private String name = "Anonymous";


    public ProfileFragment() {
        // Required empty public constructor
    }

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Context context = getContext();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            this.user = user;
        }

        TextView nameTextView = view.findViewById(R.id.txtName);
        //Button saveButton = view.findViewById(R.id.btnSave);

        // Setup RecyclerView
        listView = view.findViewById(R.id.profile_list);
        listView.setLayoutManager(new LinearLayoutManager(context));

        // Initializations
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        FCMHelper.init(context);

        ArrayList<MacroListItem> items2 = new ArrayList<>(1);
        items2.add(new MacroListItem("Calories",1740, true));
        items2.add(new MacroListItem("Fat",70, true)); // gram
        items2.add(new MacroListItem("Protein",70, true)); // gram
        items2.add(new MacroListItem("Carbs",310, true)); // gram
        items2.add(new MacroListItem("Sugar",90, true)); // gram
        items2.add(new MacroListItem("Sodium", (float)2.3, true)); // gram'
        for(MacroListItem item : items2)
        {
            dbRef.child("user").child("XuZYv8FxBBfjdiRIeQ1szaLAlhA3").child("goals").child(item.name).setValue(item);
            dbRef.child("user").child("0M7KbpeXQEhh654RHSNoa21fWGf2").child("goals").child(item.name).setValue(item);
        }

        // Load initial data
        ArrayList<MacroListItem> items = new ArrayList<>(1);
        dbRef.child("user").child(this.user.getUid()).child("goals").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<MacroListItem> items = new ArrayList<>((int) dataSnapshot.getChildrenCount());
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    Log.v(TAG, "Single " + dsp.toString());
                    MacroListItem item = MacroListItem.fromSnapshot(dsp);
                    items.add(item);
                }

                adapter.setItems(items);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Cancelled " + databaseError.toString());
            }
        });

        // update current user name
        nameTextView.setText(this.user.getDisplayName());

        // Setup adapter
        adapter = new ProfileViewAdapter(mListener, Glide.with(this),
                (v, item, goalText, hasFocus) -> {
                    if (!hasFocus) {
                        item.value = Float.parseFloat(goalText.getText().toString());
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("/user/" + this.user.getUid() + '/' + "goals" + '/' + item.name, item); // or if later know the key for each macro, then use key

                        dbRef.updateChildren(updates);

                        Toast.makeText(context, String.format("Added to goals"), Toast.LENGTH_SHORT).show();
                    }
                },
                mColumnCount);

        listView.setAdapter(adapter);
        listView.setItemAnimator(new ItemAnimator(context, adapter));
        /*
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
        });*/
    }

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

    private void setupConnectionWatcher() {
        Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.snackbar_database_connecting, Snackbar.LENGTH_INDEFINITE).show();
        fbDbConnectionWatcher = new FirebaseDatabaseConnectionWatcher();
        fbDbConnectionWatcher.addListener(new FirebaseDatabaseConnectionWatcher.OnConnectionChangeListener() {
            @Override
            public void onConnected() {
                Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.snackbar_database_connected, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onDisconnected() {
                if (getView() != null) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.snackbar_database_reconnecting, Snackbar.LENGTH_INDEFINITE).show();
                }
            }
        });
    }

    private void setupActiveUsersUpdater() {
        presenceUpdater = new PresenceUpdater(dbRef);
        presenceUpdater.start();
    }

    private void setupPresenceUpdater() {
        activeUsersUpdater = new ActiveUsersUpdater(dbRef);
        activeUsersUpdater.addListener(new ActiveUsersUpdater.OnUserConnectionChanged() {
            @Override
            public void onConnected(String connectedName) {
                if (!name.equals(connectedName)) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.snackbar_user_connected, connectedName), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onDisconnected(String disconnectedName) {
                if (!name.equals(disconnectedName)) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.snackbar_user_disconnected, disconnectedName), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onActivesChanged(ArrayList<String> actives) {

            }
        });
        activeUsersUpdater.start();
    }
}