package team16.project.team.orbis.global.methods.database;

import android.graphics.Color;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import team16.project.team.orbis.global.objectclass.Building;
import team16.project.team.orbis.global.objectclass.BuildingRunnable;
import team16.project.team.orbis.global.objectclass.ListRunnable;
import team16.project.team.orbis.global.variables.FirebaseVariables;

import static team16.project.team.orbis.global.variables.FirebaseVariables.getFirebaseDatabase;

/**
 * This class deals with a user's current map choice. This covers setting the current choice, checking a user's choice etc...
 */
public class UserBuilding {
    /**
     * This gets a user's allowed maps
     *
     * @param withMaps The code to run when the maps are retrieved
     */
    public static void getAllowedBuilding(final ListRunnable withMaps) {

        final FirebaseUser user = FirebaseVariables.getFirebaseAuth().getCurrentUser();

        // Connect to the database and get node building, and listen for a single item of data (not constantly listen for changes)
        getFirebaseDatabase().getReference("/building/").addListenerForSingleValueEvent(new ValueEventListener() {

            List<Building> allowedBuildings = new ArrayList<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // For every building
                for (DataSnapshot building : dataSnapshot.getChildren()) {
                    // Get the email address restrictions by splitting the restrictions value with a comma delimiter
                    String restrictions[] = ((String) (building.child("restrictions").getValue())).split(",");
                    String restrictionType = (String) building.child("restriction_type").getValue();

                    // If there are restrictions
                    if (!restrictionType.equals("null")) {
                        if (!user.isAnonymous()) {
                            boolean matched = false;
                            // While not at the end of the restrictions and the user's email address has not matched one of the requirements
                            for (int i = 0; i < restrictions.length && !matched; i++) {
                                // If filtering by domain
                                if (restrictionType.equals("domain")) {
                                    // If the user's email address domain equals the current restriction restriction
                                    if (user.getEmail().split("@")[1].equals(restrictions[i])) {
                                        matched = true;
                                    }
                                }
                                // If filtering by full email address
                                else if (restrictionType.equals("full")) {
                                    // If full email match
                                    if (user.getEmail().equals(restrictions[i])) {
                                        matched = true;
                                    }
                                }

                            }
                            if (matched) {
                                // Add the building to the allowed list
                                addBuildingToAllowed(building);
                            }
                        }
                    } else {
                        // Add the building to the allowed list
                        addBuildingToAllowed(building);
                    }

                }

                withMaps.setList(allowedBuildings);
                withMaps.run();
            }

            /**
             * This building gives adds the building to the list of allowed buildings
             * @param building The building to be added
             */
            private void addBuildingToAllowed(DataSnapshot building) {
                Building toAdd = getBuilding(building);
                allowedBuildings.add(toAdd);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * This method converts a DataSnapshot, which contains children with the keys required for a Building's fields, into a Building
     *
     * @param building The snapshot containing the children with the keys required for a Building's fields
     * @return The Building
     */
    @NonNull
    private static Building getBuilding(DataSnapshot building) {
        // Get the children with the correct keys, and make them Strings
        String buildingId = building.getKey();
        String buildingName = building.child("name").getValue().toString();
        String buildingColourString = building.child("colour").getValue().toString();
        String buildingFloorString = building.child("floors").getValue().toString();
        String buildingLongitudeString = building.child("longitude").getValue().toString();
        String buildingLatitudeString = building.child("latitude").getValue().toString();

        String buildingLowestFloorString = "0";
        if (building.child("lowest_floor_value").getValue() != null) {
            buildingLowestFloorString = building.child("lowest_floor_value").getValue().toString();
        }

        // Convert the Strings to ints of doubles if required
        int buildingFloors = Integer.parseInt(buildingFloorString);
        int buildingLowestFloor = Integer.parseInt(buildingLowestFloorString);
        int buildingColour = Color.parseColor(buildingColourString);
        double buildingLongitude = Double.parseDouble(buildingLongitudeString);
        double buildingLatitude = Double.parseDouble(buildingLatitudeString);

        return new Building(
                buildingId,
                buildingName,
                buildingLongitude,
                buildingLatitude,
                buildingFloors,
                buildingColour,
                buildingLowestFloor);
    }

    /**
     * Save/update a user's map choice
     *
     * @param mapId the id of the map which will be saved against the user
     * @return whether or not the update operation was successful
     */
    public static boolean saveUserBuildingChoice(String mapId) {
        return UserSettings.saveUserSetting("map", mapId);
    }

    /**
     * Given a user ID, do an operation with the user's selected building, i.e. fetch the building id, then fetch the building, and then operate with that building
     *
     * @param mapSet    What to do if the Building is set
     * @param mapNotSet What to do if the Building is not set
     */
    public static void getBuildingFromUserId(final BuildingRunnable mapSet, final Runnable mapNotSet) {
        getFirebaseDatabase().getReference("/userSettings/" + FirebaseVariables.getFirebaseAuth().getCurrentUser().getUid() + "/" + "map").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = (String) dataSnapshot.getValue();
                if (value == null || value.equals("null")) {
                    mapNotSet.run();
                    return;
                }
                getBuildingFromBuildingId((String) dataSnapshot.getValue(), mapSet);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void getBuildingFromBuildingId(String buildingId, BuildingRunnable mapSet) {
        getBuildingFromBuildingId(buildingId, mapSet, new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    public static void getBuildingFromBuildingId(String buildingId, final BuildingRunnable mapFound, final Runnable mapNotFound) {
        getFirebaseDatabase().getReference("/building/" + buildingId + "/").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object buildingName = dataSnapshot.child("name").getValue();

                if (buildingName == null || buildingName.toString().equals("null")) {
                    mapNotFound.run();
                }

                mapFound.setBuilding(getBuilding(dataSnapshot));
                mapFound.run();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
