package team16.project.team.orbis.global.methods.database;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import team16.project.team.orbis.global.methods.LocalPreferences;
import team16.project.team.orbis.global.variables.FirebaseVariables;

import static team16.project.team.orbis.global.variables.FirebaseVariables.getFirebaseDatabase;

public class UserSettings {

    public static boolean saveUserSetting(String key, String value) {
        return getFirebaseDatabase().getReference("/userSettings/" + FirebaseVariables.getFirebaseAuth().getCurrentUser().getUid() + "/" + key)
                .setValue(value)
                .isSuccessful();
    }

    public static void loadAllSettings(final Context context, final String sharedPreferenceName) {
        getFirebaseDatabase().getReference("/userSettings/" + FirebaseVariables.getFirebaseAuth().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot setting : dataSnapshot.getChildren()) {
                            String key = dataSnapshot.getKey();
                            String value = (String) setting.getValue();
                            if (value.toLowerCase().equals("true") || value.toLowerCase().equals("false")) {
                                LocalPreferences.saveBooleanToShared(context,
                                        key,
                                        Boolean.parseBoolean(value));
                            } else if (tryParseInt(value)) {
                                LocalPreferences.saveIntToShared(context,
                                        key,
                                        Integer.parseInt(value));
                            } else {
                                LocalPreferences.saveStringToShared(
                                        context,
                                        key,
                                        value);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                    private boolean tryParseInt(String toTry) {
                        try {
                            Integer.parseInt(toTry);
                            return true;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                });
    }
}
