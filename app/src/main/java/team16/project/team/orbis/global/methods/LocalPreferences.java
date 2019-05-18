package team16.project.team.orbis.global.methods;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import team16.project.team.orbis.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Class to provide helper functions for dealing with SharedPreferences
 */
public final class LocalPreferences {
    /**
     * Delete all the Shared Preferences
     *
     * @param applicationContext The context required to get Shared Preferences
     */
    public static void deleteAll(Context applicationContext) {
        applicationContext.getSharedPreferences(
                applicationContext.getString(R.string.shared_preferences_name), MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    /**
     * Save a String to the Shared Preferences
     *
     * @param applicationContext The context required to get Shared Preferences
     * @param key                The key of the preference
     * @param value              The value of the preference
     */
    public static void saveStringToShared(Context applicationContext, String key, String value) {
        getSharedPreferences(applicationContext)
                .edit()
                .putString(key, value)
                .apply();
    }

    /**
     * Save an int to the Shared Preferences
     *
     * @param applicationContext The context required to get Shared Preferences
     * @param key                The key of the preference
     * @param value              The value of the preference
     */
    public static void saveIntToShared(Context applicationContext, String key, int value) {
        getSharedPreferences(applicationContext)
                .edit()
                .putInt(key, value)
                .apply();
    }

    /**
     * Save a boolean to the Shared Preferences
     *
     * @param applicationContext The context required to get Shared Preferences
     * @param key                The key of the preference
     * @param value              The value of the preference
     */
    public static void saveBooleanToShared(Context applicationContext, String key, boolean value) {
        getSharedPreferences(applicationContext)
                .edit()
                .putBoolean(key, value)
                .apply();
    }

    /**
     * Save a float to the Shared Preferences
     *
     * @param applicationContext The context required to get Shared Preferences
     * @param key                The key of the preference
     * @param value              The value of the preference
     */
    public static void saveFloatToShared(Context applicationContext, String key, float value) {
        getSharedPreferences(applicationContext)
                .edit()
                .putFloat(key, value)
                .apply();
    }

    /**
     * Save an object to the Shared Preferences. This is done by converting the object to JSON, and then saving the object as this String
     *
     * @param applicationContext The context required to get Shared Preferences
     * @param key                The key of the preference
     * @param value              The value of the preference
     */
    public static void saveObjectToShared(Context applicationContext, String key, Object value) {
        Gson gson = new Gson();

        // Convert the object to JSON
        String objectAsString = gson.toJson(value);

        getSharedPreferences(applicationContext)
                .edit()
                .putString(key, objectAsString)
                .apply();
    }

    /**
     * Get an object from the Shared Preferences. This is done by converting the JSON to an Object. Casting is needed to convert it to the required Object type
     *
     * @param applicationContext The context required to get Shared Preferences
     * @param key                The key of the preference
     * @param objectType         The class of the required object
     * @return The object
     */
    public static Object getObjectFromShared(Context applicationContext, String key, Class objectType) {
        Gson gson = new Gson();

        return gson.fromJson(
                getSharedPreferences(applicationContext).getString(key, null),
                objectType);
    }

    /**
     * Get a string from Shared Preferences
     *
     * @param applicationContext The context required to get Shared Preferences
     * @param key                The key of the preference
     * @return The string
     */
    public static String getStringFromShared(Context applicationContext, String key) {
        return getSharedPreferences(applicationContext)
                .getString(key, null);
    }

    /**
     * Get an integer from Shared Preferences
     *
     * @param applicationContext The context required to get Shared Preferences
     * @param key                The key of the preference
     * @return The integer
     */
    public static int getIntFromShared(Context applicationContext, String key) {
        return getSharedPreferences(applicationContext).getInt(key, -1);
    }

    /**
     * Get a boolean from Shared Preferences
     *
     * @param applicationContext The context required to get Shared Preferences
     * @param key                The key of the preference
     * @return The boolean
     */
    public static boolean getBooleanFromShared(Context applicationContext, String key) {
        return getSharedPreferences(applicationContext).getBoolean(key, false);
    }

    /**
     * Get a float from Shared Preferences
     *
     * @param applicationContext The context required to get Shared Preferences
     * @param key                The key of the preference
     * @return The float
     */
    public static float getFloatFromShared(Context applicationContext, String key) {
        return getSharedPreferences(applicationContext).getFloat(key, -1);
    }

    /**
     * Find out whether or not the user has enabled lifts
     *
     * @param applicationContext The context required to get Shared Preferences
     * @return Whether or not the user has enabled lifts
     */
    public static boolean isLiftEnabled(Context applicationContext) {
        return getSharedPreferences(applicationContext).getBoolean("use_lift", false);
    }

    /**
     * Get the Shared Preferences
     *
     * @param applicationContext The context required to get Shared Preferences
     * @return The Shared Preferences
     */
    private static SharedPreferences getSharedPreferences(Context applicationContext) {
        return applicationContext.getSharedPreferences(applicationContext.getString(R.string.shared_preferences_name), MODE_PRIVATE);
    }

}
