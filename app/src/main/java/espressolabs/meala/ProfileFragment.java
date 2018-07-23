package espressolabs.meala;

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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

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

        // update user name
        TextView nameTextView = view.findViewById(R.id.txtName);

        // Setup RecyclerView
        listView = view.findViewById(R.id.profile_list);
        listView.setLayoutManager(new LinearLayoutManager(context));

        // Setup adapter
        adapter = new ProfileViewAdapter(mListener, Glide.with(this), mColumnCount);
        //adapter.setViewSize(prefs.getInt(PREFS_VIEW_SIZE, -1));
        listView.setAdapter(adapter);
        listView.setItemAnimator(new ItemAnimator(context, adapter));

        // Load initial data (get data from firebase)
        ArrayList<MacroListItem> items = new ArrayList<>(1);
        items.add(new MacroListItem("%",50));
        items.add(new MacroListItem("%",90));
        adapter.setItems(items);

        // Initializations
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        FCMHelper.init(context);

        // Load initial data
        dbRef.child("items").orderByChild("status").equalTo("ACTIVE").addListenerForSingleValueEvent(new ValueEventListener() {
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

        // current user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        nameTextView.setText(firebaseUser.getDisplayName());
    }

    public interface OnStatisticClickListener {
        //void onListFragmentInteraction(RecipeContent.Recipe item);
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