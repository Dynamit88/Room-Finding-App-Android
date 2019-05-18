package team16.project.team.orbis.global.uiclass;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import team16.project.team.orbis.AppCompatPreferenceActivity;
import team16.project.team.orbis.R;
import team16.project.team.orbis.global.methods.LocalPreferences;
import team16.project.team.orbis.global.objectclass.Building;

/**
 * This extends AppCompatPreferenceActivity, and provides code for retrieving the colour of a building and setting it as the colour of the toolbar of a Preference Activity
 */

public class AppCompatPreferenceColourActivity extends AppCompatPreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int buildingColour = ((Building) (LocalPreferences.getObjectFromShared(getApplicationContext(),
                getString(R.string.building_key_shared_pref),
                Building.class))).getColour();

        // Set the toolbar's primary colour to be the building's colour
        setPrimaryColour(getSupportActionBar(), buildingColour);

        // Set the toolbar's primary colour to be the building's colour 35% darker
        setSecondaryColour(buildingColour);
    }

    /**
     * Set the secondary colour of the toolbar, by reducing the brightness by 35%
     *
     * @param buildingColour The primary colour
     */
    private void setSecondaryColour(int buildingColour) {
        int darkenedColour = getDarkenedColour(buildingColour);
        // Set the secondary colour to be the darkened colour
        getWindow().setStatusBarColor(darkenedColour);
    }

    /**
     * Given a colour, darken it by 35%
     *
     * @param buildingColour The colour
     * @return The darkened colour
     */
    public int getDarkenedColour(int buildingColour) {
        // Get each of the RGB elements of the colour, and reduce it by 35%
        double darkenedBuildingRed = Color.red(buildingColour) * 0.65;
        double darkenedBuildingBlue = Color.blue(buildingColour) * 0.65;
        double darkenedBuildingGreen = Color.green(buildingColour) * 0.65;

        // Get the Android integer representation of the darkened RGB colour, discarding any decimal places
        return convertColourFromRgb(
                (int) darkenedBuildingRed,
                (int) darkenedBuildingBlue,
                (int) darkenedBuildingGreen);
    }


    /**
     * With a set of integer values for red, green abd blue, create an integer colour representation
     *
     * @param darkenedBuildingRed   The red colour
     * @param darkenedBuildingBlue  The blue colour
     * @param darkenedBuildingGreen The green colour
     * @return The integer representation of the colour
     */
    private int convertColourFromRgb(int darkenedBuildingRed, int darkenedBuildingBlue, int darkenedBuildingGreen) {
        return Color.rgb(
                darkenedBuildingRed,
                darkenedBuildingGreen,
                darkenedBuildingBlue);
    }

    /**
     * Set the primary colour of the ActionBar
     *
     * @param aBar           The ActionBar
     * @param buildingColour The colour
     */
    private void setPrimaryColour(ActionBar aBar, int buildingColour) {
        aBar.setBackgroundDrawable(new ColorDrawable(buildingColour));
    }

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
}
