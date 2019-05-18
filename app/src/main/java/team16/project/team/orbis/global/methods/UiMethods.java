package team16.project.team.orbis.global.methods;

import android.content.Context;
import android.content.Intent;

import java.io.Serializable;
import java.util.Map;

import team16.project.team.orbis.MapChoiceActivity;
import team16.project.team.orbis.global.objectclass.Building;


/**
 * This class contains general UI methods which Activities may commonly use
 */

public class UiMethods {
    /**
     * Switch from one Activity to another
     * @param contextFrom The context of the Activity to switch from
     * @param newActivity The class of the Activity to switch to
     */
    public static void switchActivity(Context contextFrom, Class newActivity) {
        Intent switchScreen = new Intent(contextFrom, newActivity);
        contextFrom.startActivity(switchScreen);
    }

    /**
     * Switch from one Activity to another with an action
     * @param contextFrom The context of the Activity to switch from
     * @param newActivity The class of the Activity to switch to
     * @param action The Action to send to the new Activity
     */
    public static void switchActivity(Context contextFrom, Class newActivity, String action) {
        Intent switchScreen = new Intent(contextFrom, newActivity);
        switchScreen.setAction(action);
        contextFrom.startActivity(switchScreen);
    }
}
