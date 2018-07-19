package espressolabs.meala;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import espressolabs.meala.LoginActivity;
import espressolabs.meala.MainActivity;


public class MainEmptyActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent activityIntent;

        mAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // go straight to main if a token is stored
        if (currentUser != null) {
            activityIntent = new Intent(this, MainActivity.class);
        } else {
            activityIntent = new Intent(this, LoginActivity.class);
        }

        startActivity(activityIntent);
        finish();
    }
}