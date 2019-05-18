package team16.project.team.orbis.global.uiclass;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import java.io.Serializable;

/**
 * This extends AppCompatActivity, and provides functionality for switching Activity
 * Created by Josh on 05/03/2018.
 */

public class AppCompatUiSwitchActivity extends AppCompatActivity {
    /**
     * Switch from one Activity to another
     *
     * @param newActivity The class of the Activity to switch to
     */
    public void switchActivity(Class newActivity) {
        Intent switchScreen = new Intent(this, newActivity);
        startActivity(switchScreen);
    }

    /**
     * Switch from one Activity to another with an action
     *
     * @param newActivity The class of the Activity to switch to
     * @param action      The Action to send to the new Activity
     */
    public void switchActivity(Class newActivity, String action) {
        Intent switchScreen = new Intent(this, newActivity);
        switchScreen.setAction(action);
        startActivity(switchScreen);
    }


    /**
     * Switch from one Activity to another with an action and data
     *
     * @param newActivity The class of the Activity to switch to
     * @param action      The Action to send to the new Activity
     * @param key         The key to associate the data with
     * @param data        The data to send to the new activity
     */
    public void switchActivity(Class newActivity, String action, String key, Serializable data) {
        Intent switchScreen = new Intent(this, newActivity);
        switchScreen.setAction(action);
        switchScreen.putExtra(key, data);
        startActivity(switchScreen);
    }
}
