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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

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

    public static final String TAG = "ShoppingListFragment";
    private static final int STATE_STARTING = -1;
    private static final int STATE_LOADING = 0;
    private static final int STATE_EMPTY = 1;
    private static final int STATE_LIST = 2;
    public final String PREFS_NAME = "name";
    public final String PREFS_VIEW_SIZE = "viewSize";
    public final String FIREBASE_TOPIC_ADD = "add";
    public ActiveUsersUpdater activeUsersUpdater;
    private String name = "Anonymous";
    private PlanningListAdapter adapter;
    private DatabaseReference dbRef;
    private SharedPreferences prefs;
    private RecyclerView listView;
    private String lastAddedOwnKey;
    private int state = STATE_STARTING;
    private FirebaseDatabaseConnectionWatcher fbDbConnectionWatcher;
    private FirebaseDatabase database;
    private PresenceUpdater presenceUpdater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopping_list, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Context context = getContext();

        // Setup RecyclerView
        listView = view.findViewById(R.id.shopping_list);
        listView.setLayoutManager(new LinearLayoutManager(context));

        // Initializations
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        FCMHelper.init(context);

        // Subscribe to topic which broadcasts the new item notifications
        FirebaseMessaging.getInstance().subscribeToTopic(FIREBASE_TOPIC_ADD);


        // Setup adapter
        adapter = new PlanningListAdapter(context, listView);
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
        dbRef.child("items").orderByChild("status").equalTo("ACTIVE").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<MealListItem> items = new ArrayList<>((int) dataSnapshot.getChildrenCount());

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    Log.v(TAG, "Single " + dsp.toString());
                    MealListItem item = MealListItem.fromSnapshot(dsp);
                    items.add(item);
                }

                adapter.setItems(items);

                // Listen for changes
                final long startAt = (items.size() > 0) ? items.get(0).createdAt : System.currentTimeMillis();
                final long lastAt = (items.size() > 0) ? items.get(items.size() - 1).createdAt : System.currentTimeMillis();
                dbRef.child("items").orderByChild("createdAt").startAt(startAt).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                        Log.v(TAG, "Added " + dataSnapshot.toString());

                        MealListItem item = MealListItem.fromSnapshot(dataSnapshot);
                        if (item.createdAt > lastAt) {
                            adapter.addItem(item);

                            if (lastAddedOwnKey != null && lastAddedOwnKey.equals(item.key)) {
                                listView.scrollToPosition(0);
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                        Log.v(TAG, "Changed " + dataSnapshot.toString());
                        MealListItem item = MealListItem.fromSnapshot(dataSnapshot);
                        adapter.updateItem(item);
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        Log.v(TAG, "Removed " + dataSnapshot.toString());
                        MealListItem item = MealListItem.fromSnapshot(dataSnapshot);
                        adapter.removeItem(item);
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                        Log.v(TAG, "Moved " + dataSnapshot.toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Cancelled " + databaseError.toString());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Cancelled " + databaseError.toString());
            }
        });

        setupConnectionWatcher();
        setupPresenceUpdater();
        setupActiveUsersUpdater();

        setUIState(STATE_LOADING);

        // Load name or open dialog
        String pname = prefs.getString(PREFS_NAME, name);
        if (pname == null) {
            openNameChangeDialog();
        } else {
            changeName(pname);
        }
    }

    private void setupConnectionWatcher() {
        Snackbar.make(getView(), R.string.snackbar_database_connecting, Snackbar.LENGTH_INDEFINITE).show();
        fbDbConnectionWatcher = new FirebaseDatabaseConnectionWatcher();
        fbDbConnectionWatcher.addListener(new FirebaseDatabaseConnectionWatcher.OnConnectionChangeListener() {
            @Override
            public void onConnected() {
                Snackbar.make(getView(), R.string.snackbar_database_connected, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onDisconnected() {
                if (getView() != null) {
                    Snackbar.make(getView(), R.string.snackbar_database_reconnecting, Snackbar.LENGTH_INDEFINITE).show();
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
                    Snackbar.make(getView(), getString(R.string.snackbar_user_connected, connectedName), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onDisconnected(String disconnectedName) {
                if (!name.equals(disconnectedName)) {
                    Snackbar.make(getView(), getString(R.string.snackbar_user_disconnected, disconnectedName), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onActivesChanged(ArrayList<String> actives) {

            }
        });
        activeUsersUpdater.start();
    }

    private void setUIState(int newState) {
        View view = getView();
        View[] views = new View[]{
                view.findViewById(R.id.loading_shopping_list),
                view.findViewById(R.id.empty_shopping_list),
                view.findViewById(R.id.shopping_list)
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

    @Override
    public void onResume() {
        super.onResume();

        FirebaseDatabase.getInstance().goOnline();

        presenceUpdater.start();
        activeUsersUpdater.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        FirebaseDatabase.getInstance().goOffline();

        presenceUpdater.stop();
        activeUsersUpdater.stop();
    }

    public void changeName(String newName) {
        name = newName;
        prefs.edit().putString(PREFS_NAME, name).apply();
        presenceUpdater.setKey(name);
    }

    public void createItem(String itemName, String itemDescription) {
        MealListItem listItem = new MealListItem(name, itemName, itemDescription, MealListItem.Meal.DINNER);
        DatabaseReference ref = dbRef.child("items").push();
        ref.setValue(listItem);

        lastAddedOwnKey = ref.getKey();
        // sendItemNotification(listItem);
    }

    public void openNameChangeDialog() {
        NameDialogFragment nameDialogFragment = NameDialogFragment.newInstance(this.name);
        nameDialogFragment.show(getFragmentManager(), "NameDialogFragment");
    }

    public void openAddItemDialog() {
        ItemDialogFragment itemDialogFragment = new ItemDialogFragment();
        itemDialogFragment.show(getFragmentManager(), "ItemDialogFragment");
    }

    public void openUsersDialog() {
        UsersDialogFragment usersDialogFragment = UsersDialogFragment.newInstance(activeUsersUpdater);
        usersDialogFragment.show(getFragmentManager(), "UserDialogFragment");
    }

    private void sendItemNotification(MealListItem item) {
        FCMHelper.sendNotification(
                FIREBASE_TOPIC_ADD,
                getString(R.string.notification_title_new_item),
                getString(R.string.notification_body_new_item, item.name, item.createdBy)
        );
    }
}