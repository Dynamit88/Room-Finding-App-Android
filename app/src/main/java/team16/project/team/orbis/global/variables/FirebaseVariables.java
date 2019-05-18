package team16.project.team.orbis.global.variables;


import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * This class returns global Firebase variables (for authentication and database access)
 */
public class FirebaseVariables {

    private static FirebaseAuth auth;
    private static FirebaseDatabase db;
    private static StorageReference sr;
    private static FirebaseAnalytics fa;

    public static FirebaseAuth getFirebaseAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    public static FirebaseDatabase getFirebaseDatabase() {
        if (db == null) {
            db = FirebaseDatabase.getInstance();
            db.setPersistenceEnabled(false);
        }
        return db;
    }


    public static StorageReference getFirebaseStorageReference() {
        if (sr == null) {
            sr = FirebaseStorage.getInstance().getReference();
        }
        return sr;
    }

    public static FirebaseAnalytics getFirebaseAnalyticsReference(Context context) {
        if (fa == null) {
            fa = FirebaseAnalytics.getInstance(context);
        }
        return fa;
    }
}
