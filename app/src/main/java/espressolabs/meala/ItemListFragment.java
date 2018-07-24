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

import espressolabs.meala.dialog.ItemDialogFragment;
import espressolabs.meala.dialog.NameDialogFragment;
import espressolabs.meala.dialog.UsersDialogFragment;
import espressolabs.meala.firebase.FirebaseDatabaseConnectionWatcher;
import espressolabs.meala.model.ShoppingListItem;
import espressolabs.meala.runnables.ActiveUsersUpdater;
import espressolabs.meala.runnables.PresenceUpdater;
import espressolabs.meala.ui.interaction.ItemAnimator;
import espressolabs.meala.ui.interaction.ItemTouchHelperCallback;
import espressolabs.meala.ui.interaction.ShoppingListAdapter;
import espressolabs.meala.utils.FCMHelper;

import static espressolabs.meala.utils.Constants.PREFS_NAME;
import static espressolabs.meala.utils.Constants.PREFS_VIEW_SIZE;

public abstract class ItemListFragment extends Fragment{

    public static final String TAG = "ItemListFragment";
    private static final int STATE_STARTING = -1;
    private static final int STATE_LOADING = 0;
    private static final int STATE_EMPTY = 1;
    private static final int STATE_LIST = 2;
    public final String FIREBASE_TOPIC_ADD = "add";
    public ActiveUsersUpdater activeUsersUpdater;
    private String name;
    private ShoppingListAdapter adapter;
    private DatabaseReference dbRef;
    private SharedPreferences prefs;
    private RecyclerView listView;
    private String lastAddedOwnKey;
    private int state = STATE_STARTING;
    private FirebaseDatabaseConnectionWatcher fbDbConnectionWatcher;
    private FirebaseDatabase database;
    private PresenceUpdater presenceUpdater;

    protected abstract String getItemType();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recycler_list, null);
    }
    protected void deleteItem(ShoppingListAdapter.ViewHolder vh){
        // Delete - marks as deleted
        ShoppingListItem item = vh.data;
        Log.v(TAG, "Delete " + item);
        item.delete();
        dbRef.child("items").child(item.key).setValue(item);
    }
    protected void moveToShoppingList(ShoppingListAdapter.ViewHolder vh){
        // Delete - marks as deleted
        ShoppingListItem item = vh.data;
        Log.v(TAG, "Moved to Shopping List " + item);
        item.makeActive();
        dbRef.child("items").child(item.key).setValue(item);
    }
    protected void archiveItem(ShoppingListAdapter.ViewHolder vh){
        // Archive - move to pantry
        ShoppingListItem item = vh.data;
        Log.v(TAG, "Archive " + item);
        item.archive();
        dbRef.child("items").child(item.key).setValue(item);
    }
    protected abstract void swipeItemLeft(ShoppingListAdapter.ViewHolder vh);
    protected abstract void swipeItemRight(ShoppingListAdapter.ViewHolder vh);

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Context context = getContext();

        // Setup RecyclerView
        listView = view.findViewById(R.id.recycler_list);
        listView.setLayoutManager(new LinearLayoutManager(context));

        // Handle swipes
        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelperCallback(
                        context,
                        new ItemTouchHelperCallback.OnSwipeListener() {

                            @Override
                            public void onSwipeLeft(ShoppingListAdapter.ViewHolder vh) {
                                swipeItemLeft(vh);
                            }

                            @Override
                            public void onSwipeRight(ShoppingListAdapter.ViewHolder vh) {
                                swipeItemRight(vh);
                            }

                        }
                )
        );
        helper.attachToRecyclerView(listView);


        // Initializations
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        FCMHelper.init(context);

        // Subscribe to topic which broadcasts the new item notifications
        FirebaseMessaging.getInstance().subscribeToTopic(FIREBASE_TOPIC_ADD);


        // Setup adapter
        adapter = new ShoppingListAdapter(context, listView);
        adapter.setViewSize(prefs.getInt(PREFS_VIEW_SIZE, -1));
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
//        dbRef.child("items").orderByChild("status").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        // Load initial data need a listbased event listener
        dbRef.child("items").orderByChild("status").equalTo(getItemStatus()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<ShoppingListItem> items = new ArrayList<>((int) dataSnapshot.getChildrenCount());
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    Log.v(TAG, "Single " + dsp.toString());
                    ShoppingListItem item = ShoppingListItem.fromSnapshot(dsp);
                    // check
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

                        ShoppingListItem item = ShoppingListItem.fromSnapshot(dataSnapshot);
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
                        ShoppingListItem item = ShoppingListItem.fromSnapshot(dataSnapshot);
                        adapter.updateItem(item);
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        Log.v(TAG, "Removed " + dataSnapshot.toString());
                        ShoppingListItem item = ShoppingListItem.fromSnapshot(dataSnapshot);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_change_name:
                openNameChangeDialog();
                break;

            case R.id.menu_change_view:
                if (adapter.getViewSize() == ShoppingListAdapter.VIEW_SIZE_COMPACT) {
                    item.setTitle(R.string.menu_change_view_compact);
                    adapter.setViewSize(ShoppingListAdapter.VIEW_SIZE_COZY);
                } else if (adapter.getViewSize() == ShoppingListAdapter.VIEW_SIZE_COZY) {
                    item.setTitle(R.string.menu_change_view_cozy);
                    adapter.setViewSize(ShoppingListAdapter.VIEW_SIZE_COMPACT);
                }

                prefs.edit().putInt(PREFS_VIEW_SIZE, adapter.getViewSize()).apply();
                break;

            case R.id.menu_show_actives:
                openUsersDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
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

    public void createItem(String itemName, String itemDescription, int itemPrice, boolean itemUrgent) {
        ShoppingListItem listItem = new ShoppingListItem(name, itemName, itemDescription, itemPrice, itemUrgent);
        DatabaseReference ref = dbRef.child("items").push();
        ref.setValue(listItem);

        lastAddedOwnKey = ref.getKey();
        // sendItemNotification(listItem);
    }

    public void openNameChangeDialog() {
        NameDialogFragment nameDialogFragment = NameDialogFragment.newInstance(this.name);
        nameDialogFragment.show(getFragmentManager(), "NameDialogFragment");
    }


    public void openUsersDialog() {
        UsersDialogFragment usersDialogFragment = UsersDialogFragment.newInstance(activeUsersUpdater);
        usersDialogFragment.show(getFragmentManager(), "UserDialogFragment");
    }

    private void sendItemNotification(ShoppingListItem item) {
        FCMHelper.sendNotification(
                FIREBASE_TOPIC_ADD,
                getString(R.string.notification_title_new_item),
                getString(R.string.notification_body_new_item, item.name, item.createdBy)
        );
    }

    protected String getItemStatus(){
        switch (getItemType()){
            case "Pantry Item":
                return "ARCHIVED";
            case "Shopping Item":
                return "ACTIVE";
            default:
                return "INVALID";
        }
    }
}
