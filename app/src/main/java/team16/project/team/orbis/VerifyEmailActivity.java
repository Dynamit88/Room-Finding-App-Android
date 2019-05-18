package team16.project.team.orbis;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import team16.project.team.orbis.global.methods.LocalPreferences;
import team16.project.team.orbis.global.methods.database.UserBuilding;
import team16.project.team.orbis.global.objectclass.BuildingRunnable;
import team16.project.team.orbis.global.uiclass.AppCompatUiSwitchActivity;
import team16.project.team.orbis.global.variables.FirebaseVariables;

/**
 * Underlying code for the Email Verification screen, which provides functionality to check if a user's email address is verified
 */
public class VerifyEmailActivity extends AppCompatUiSwitchActivity {
    // UI references
    private Button mResendVerification;
    private Button mCancel;
    private Date emailSentLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_verify_email);

        // Set the title of the Activity to be different than the Activity name
        getSupportActionBar().setTitle(R.string.validate_email_header);
        setupVariables();
        setupListeners();
        checkEmailVerified();
    }

    /**
     * Setup the listeners for the buttons on the screen
     */
    private void setupListeners() {
        mResendVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int fiveMinutesAsJavaUnit = 300000;
                if (emailSentLast == null) {
                    FirebaseVariables.getFirebaseAuth().getCurrentUser().sendEmailVerification();
                    emailSentLast = new Date();
                    createToast(getString(R.string.email_verification_sent));
                } else if (new Date().getTime() - emailSentLast.getTime() >= fiveMinutesAsJavaUnit) {
                    FirebaseVariables.getFirebaseAuth().getCurrentUser().sendEmailVerification();
                    emailSentLast = new Date();
                    createToast(getString(R.string.email_verification_sent));
                } else {
                    createToast("Please wait five minutes from when the last verification was sent (" + new SimpleDateFormat("HH:mm", Locale.UK).format(emailSentLast) + ")");
                }
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseVariables.getFirebaseAuth().getCurrentUser().delete();
                FirebaseVariables.getFirebaseAuth().signOut();
                switchActivity(LoginActivity.class);
            }
        });
    }

    /**
     * Setup the UI references declared at the start of the class
     */
    private void setupVariables() {
        mResendVerification = findViewById(R.id.resend_email);
        mCancel = findViewById(R.id.cancel);
    }

    /**
     * Check if the user's email address is verified and if so, execute the appropriate code
     */
    private void checkEmailVerified() {
        // If the user's email address is verified
        if (FirebaseVariables.getFirebaseAuth().getCurrentUser().isEmailVerified()) {
            // Check the user has set a map. If so run the code in the map set runnable, else run the code in the map not set runnable
            UserBuilding.getBuildingFromUserId(getMapSetRunnable(), getMapNotSetRunnable());
        }
    }

    /**
     * Get the code to run if the user has set their map
     *
     * @return The code to run
     */
    @NonNull
    private BuildingRunnable getMapSetRunnable() {
        return new BuildingRunnable() {
            @Override
            public void run() {
                // Save clicked building to local preferences
                LocalPreferences.saveObjectToShared(getApplicationContext(), getString(R.string.building_key_shared_pref), getBuilding());
                switchActivity(MainActivity.class);
            }
        };
    }

    /**
     * Get the code to run if the user has not set their map
     *
     * @return The code to run
     */
    @NonNull
    private Runnable getMapNotSetRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                switchActivity(MapChoiceActivity.class, getString(R.string.set_map_choice));
            }
        };
    }

    /**
     * This creates a toast message on the screen
     *
     * @param text The message to show
     */
    private void createToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}
