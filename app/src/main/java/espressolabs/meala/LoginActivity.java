package espressolabs.meala;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import espressolabs.meala.model.MacroListItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static espressolabs.meala.utils.Constants.PREFS_NAME;


public class LoginActivity extends BaseActivity implements
        View.OnClickListener {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "Auth";

    // UI references.
    private GoogleSignInClient mGoogleSignInClient;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Save user information to database for ease of use
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

                        dbRef.child("user").child(user.getUid()).child("email").setValue(user.getEmail());
                        dbRef.child("user").child(user.getUid()).child("name").setValue(user.getDisplayName());

                        // setup user initial setting
                        ArrayList<MacroListItem> items = new ArrayList<>(1);
                                                items.add(new MacroListItem("Calories",1740, true));
                                                items.add(new MacroListItem("Fat",70, true)); // gram
                                                items.add(new MacroListItem("Protein",70, true)); // gram
                                                items.add(new MacroListItem("Carbs",310, true)); // gram
                                                items.add(new MacroListItem("Sugar",90, true)); // gram
                                                items.add(new MacroListItem("Sodium", (float)2.3, true)); // gram'
                        for(MacroListItem item : items)
                        {
                            dbRef.child("user").child(user.getUid()).child("goals").child(item.name).setValue(item);
                        }

                        // Save user name pref
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        prefs.edit().putString(PREFS_NAME, user.getDisplayName()).apply();

                        // Update UI
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        updateUI(null);
                    }

                    hideProgressDialog();
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> updateUI(null));
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                task -> updateUI(null));
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);

            finish();
            startActivity(new Intent(this, MainActivity.class));
        } else {
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_in_button) {
            signIn();
        }
    }
}

