package team16.project.team.orbis;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import team16.project.team.orbis.global.methods.LocalPreferences;
import team16.project.team.orbis.global.methods.database.UserBuilding;
import team16.project.team.orbis.global.methods.database.UserSettings;
import team16.project.team.orbis.global.objectclass.Building;
import team16.project.team.orbis.global.objectclass.BuildingRunnable;
import team16.project.team.orbis.global.uiclass.AppCompatUiSwitchActivity;
import team16.project.team.orbis.global.variables.FirebaseVariables;

/**
 * Underlying code for the Login screen, which provides login/sign up functionality
 */
public class LoginActivity extends AppCompatUiSwitchActivity {

    // UI references.
    private EditText mEmailView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mEmailSignInButton;
    private Button mSkipSignInButton;

    private String email;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Hide title bar (make it fullscreen)
        getSupportActionBar().hide();

        // Remove focus from email entry box, and give it to the logo, hiding the keyboard
        removeFocusFromEmailEntry();

        setupVariables();
        setupListeners();
        addAppHomeScreenShortcuts();
        signoutIfInAction();
        changeActivityIfSignedIn();
    }

    /**
     * If the login screen has been called so the current user is logged out, log the user out
     */
    private void signoutIfInAction() {
        // If an Action has been given to the Intent which showed the login screen...
        if (getIntent() != null) {
            if (getIntent().getAction() != null) {
                // If that Action is to log the user out
                if (getIntent().getAction().equals(getString(R.string.logout_action))) {
                    // Log the user out
                    FirebaseVariables.getFirebaseAuth().signOut();
                    LocalPreferences.deleteAll(getApplicationContext());
                }
            }
        }
    }

    /**
     * If the version of Android on the device is higher than or equal to Nougat, add long press app icon actions
     */
    private void addAppHomeScreenShortcuts() {
        if (nougatCheck()) {
            // If the user is signed in and has verified their email address add the user signed in shortcuts
            if (FirebaseVariables.getFirebaseAuth().getCurrentUser() != null && FirebaseVariables.getFirebaseAuth().getCurrentUser().isEmailVerified()) {
                addSignedInShortcuts();
            } else {
                // Else add the user signed out shortcuts
                addSignOutShortcuts();
            }
        }
    }

    /**
     * Add the long press app icon actions for if the user is signed out
     */
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private void addSignOutShortcuts() {
        // Get an instance of the home screen shortcut manager
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

        // Add a sign in shortcut, which opens the login page with the action of signing in
        ShortcutInfo loginShortcut = createShortcut(getString(R.string.home_screen_shortcut_sign_in_id),
                getString(R.string.signin_home_shortcut),
                Icon.createWithResource(this, R.drawable.sign_in_home_icon),
                LoginActivity.class,
                getString(R.string.login_action_home_shortcut));

        // Convert the shortcut to add into a list, and set it as the app icon shortcut
        shortcutManager.setDynamicShortcuts(Collections.singletonList(loginShortcut));
    }

    /**
     * Add the long press app icon actions for if the user is signed in
     */
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private void addSignedInShortcuts() {
        List<ShortcutInfo> homeShortcuts = new ArrayList<ShortcutInfo>();

        // Get an instance of the home screen shortcut manager
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

        // Add a sign out shortcut, which opens the login page with the action of signing out
        ShortcutInfo logoutShortcut = createShortcut(getString(R.string.home_screen_shortcut_logout_id),
                getString(R.string.logout_home_shortcut),
                Icon.createWithResource(this, R.drawable.sign_in_home_icon),
                LoginActivity.class,
                getString(R.string.logout_action));

        // Add a change map shortcut, which opens the change map page (the action does not matter here)
        ShortcutInfo changeMapShortcut = createShortcut(getString(R.string.home_screen_shortcut_change_map_id),
                getString(R.string.change_map_home_shortcut),
                Icon.createWithResource(this, R.drawable.sign_in_home_icon),
                MapChoiceActivity.class,
                getString(R.string.change_map_action_home_shortcut));

        // Add a settings shortcut, which opens the settings page (the action does not matter here)
        ShortcutInfo settingsShortcut = createShortcut(getString(R.string.home_screen_shortcut_settings_id),
                getString(R.string.settings_home_shortcut),
                Icon.createWithResource(this, R.drawable.sign_in_home_icon),
                SettingsActivity.class,
                getString(R.string.settings_action_home_shortcut));

        homeShortcuts.add(changeMapShortcut);
        homeShortcuts.add(settingsShortcut);
        homeShortcuts.add(logoutShortcut);

        // Add the shortcuts to the app
        shortcutManager.setDynamicShortcuts(homeShortcuts);
    }

    /**
     * This method creates a shortcut which will be added to the app icon.
     *
     * @param id       The id of the shortcut to be made
     * @param label    The text to go on the shortcut
     * @param icon     The shortcut's icon
     * @param toSwitch The Activity to show
     * @param action   The action for the Activity
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private ShortcutInfo createShortcut(String id, String label, Icon icon, Class toSwitch, String action) {
        return new ShortcutInfo.Builder(this, id)
                .setShortLabel(label)
                .setLongLabel(label)
                .setIcon(icon)
                .setIntent(new Intent(getApplicationContext(), toSwitch).setAction(action))
                .build();
    }

    /**
     * Returns whether or not the device is running Android Nougat or higher
     *
     * @return True if higher than or equal to Nougat, false otherwise
     */
    private boolean nougatCheck() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;
    }

    /**
     * Remove focus from the email entry text area on app start, hiding the keyboard, by putting the focus on the logo
     */
    private void removeFocusFromEmailEntry() {
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    /**
     * Check if the user is signed in
     */
    private void changeActivityIfSignedIn() {
        FirebaseUser user = FirebaseVariables.getFirebaseAuth().getCurrentUser();

        // If the user has got passed the sign in page
        if (user != null) {
            // And the user has not skipped sign in
            if (!user.isAnonymous()) {
                // If the user's email address is not verified
                if (!user.isEmailVerified()) {
                    // Go to the verify email Activity
                    switchActivity(VerifyEmailActivity.class);
                }

                // Else, check if the user has set a map. If so run the code from getMapSetRunnable(), else run the code from getMapNotSetRunnable()
                UserBuilding.getBuildingFromUserId(getMapSetRunnable(),
                        getMapNotSetRunnable());
            } else {
                // If the user has skipped sign in and has not set a map
                if (LocalPreferences.getObjectFromShared(getApplicationContext(),
                        getString(R.string.building_key_shared_pref),
                        Building.class) == null) {
                    // Change to the Map Choice screen
                    switchActivity(MapChoiceActivity.class);

                } else {
                    // Else go to the main screen
                    switchActivity(MainActivity.class);
                }
            }
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
     * Setup the listeners for the buttons on the screen
     */
    private void setupListeners() {
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // Sign in with the user's email address
                attemptLogin(false);
            }
        });
        mSkipSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign in anonymously
                attemptLogin(true);
            }
        });
    }

    /**
     * Setup the UI variable references
     */
    private void setupVariables() {
        mEmailView = findViewById(R.id.email);
        mLoginFormView = findViewById(R.id.email_login_form);
        mProgressView = findViewById(R.id.login_progress);
        mEmailSignInButton = findViewById(R.id.signin);
        mSkipSignInButton = findViewById(R.id.skip_signin);
    }

    /**
     * Login the user
     *
     * @param anonymous Whether or not the user will be signed in anonymously (the user has pressed skip)
     */
    private void attemptLogin(boolean anonymous) {
        // Reset errors
        mEmailView.setError(null);

        // If the sign in is not anonymous
        if (!anonymous) {
            // Get the user's email address and make it lower case for comparison purposes
            email = mEmailView.getText().toString().toLowerCase();

            // If the email address is not valid, do nothing
            if (!checkEmailAddress()) {
                return;
            }

            // If the user is running Nougat or higher
            if (nougatCheck()) {
                addSignedInShortcuts();
            }

            // Show the progress animation
            showProgress(true);
            doSignIn();
        } else {
            // Show the progress animation
            showProgress(true);
            signInAnonymously();
        }
    }

    /**
     * Sign the user in anonymously (with no email)
     */
    private void signInAnonymously() {
        FirebaseVariables.getFirebaseAuth().signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // If it worked
                if (task.isSuccessful()) {
                    // Make the user select a map
                    switchActivity(MapChoiceActivity.class, getString(R.string.set_map_choice));
                } else {
                    // Stop the progress animation
                    showProgress(false);
                    // Let the user know the login failed
                    createToast(getString(R.string.error_login));
                }

            }
        });
    }

    /**
     * Sign the user in with their email address
     */
    private void doSignIn() {
        // Use the user's email address and a default password.
        // As passwords are not used in the app but Firebase requires it, this has been provided
        FirebaseVariables.getFirebaseAuth().signInWithEmailAndPassword(email, getString(R.string.empty_password)).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // If it worked
                if (task.isSuccessful()) {
                    loadSettings();
                    // Make the user verify their identity
                    UserBuilding.getBuildingFromUserId(getMapSetRunnable(), getMapNotSetRunnable());
                } else {
                    // Sign the user up.
                    // The only way this can fail is if the user entered an email address not in the system, so the user will be signed up if that is the case.
                    createUser();
                }
            }
        });
    }

    private void loadSettings() {
        UserSettings.loadAllSettings(getApplicationContext(), getString(R.string.shared_preferences_name));
    }

    /**
     * Create the user with their email address
     */
    private void createUser() {
        // Tell the user their account is being created
        createToast(getString(R.string.creating_account));

        // Sign the user up with their email address and a default password.
        // As passwords are not used in the app but Firebase requires it, this has been provided
        FirebaseVariables.getFirebaseAuth().createUserWithEmailAndPassword(email, getString(R.string.empty_password)).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // If it worked
                if (task.isSuccessful()) {
                    // TODO: Is the below line of code needed?
                    FirebaseVariables.getFirebaseDatabase().getReference(FirebaseVariables.getFirebaseAuth().getCurrentUser().getUid()).setValue(null);
                    FirebaseVariables.getFirebaseAuth().getCurrentUser().sendEmailVerification();
                    switchActivity(VerifyEmailActivity.class);
                } else {
                    // Let the user know sign up failed
                    createToast(getString(R.string.error_login));
                }
            }
        });
    }

    /**
     * This creates a toast message on the screen
     *
     * @param text The message to show
     */
    private void createToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    /**
     * Check the given email address is valid
     *
     * @return True if it is valid, false if not
     */
    private boolean checkEmailAddress() {
        // If not email address provided...
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            return false;
        } else if (!isEmailSyntaxValid(email)) {
            // If the email address is not valid
            mEmailView.setError(getString(R.string.error_invalid_email));
            return false;
        }
        return true;
    }

    /**
     * Check if the given email address is of valid syntax
     *
     * @param email The email address to check
     * @return True if valid, false if not
     */
    private boolean isEmailSyntaxValid(String email) {
        // Does the email address contain @ and . and not a space
        return (email.contains("@")
                && !email.contains(" ")
                && email.contains("."));
    }

    /**
     * Shows the progress UI and hides the LoginActivity form.
     * Code generated by Android Studio
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}