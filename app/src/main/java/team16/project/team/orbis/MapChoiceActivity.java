package team16.project.team.orbis;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Collections;
import java.util.Comparator;

import team16.project.team.orbis.global.methods.LocalPreferences;
import team16.project.team.orbis.global.methods.UserPermissions;
import team16.project.team.orbis.global.methods.database.UserBuilding;
import team16.project.team.orbis.global.objectclass.Building;
import team16.project.team.orbis.global.objectclass.BuildingRunnable;
import team16.project.team.orbis.global.objectclass.ListRunnable;
import team16.project.team.orbis.global.uiclass.AppCompatUiSwitchActivity;
import team16.project.team.orbis.global.variables.FirebaseVariables;


/**
 * This class lets a user choose which map/building they want to navigate around
 */
public class MapChoiceActivity extends AppCompatUiSwitchActivity {

    // The callback constant for location permission requesting
    private static final int LOCATION_PERMISSION_CALLBACK = 1000;
    // UI References
    private ListView mapList;
    private MapView dialogMap;
    private Button ok, cancel;
    private Dialog mapDialog;
    private FusedLocationProviderClient fusedLocationProviderClient;
    // Used for sorting buildings based on user's location
    private Comparator<Building> compareBuilding;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_choice);

        // If the user used a deep link to get to map choice but is not signed in, make them sign in
        if (userSignedOut()) {
            switchActivity(LoginActivity.class);
            return;
        }

        setupVariables();
        switchMapIfRelevant();

        // if map not switched then...

        setupListeners(savedInstanceState);

        // Request the user's location and sort the buildings based on this (should it be approved)
        requestLocationAndSort();
        // Load and show allowed maps
        UserBuilding.getAllowedBuilding(
                getWithMapsRunnable(getApplicationContext()));
    }

    private boolean userSignedOut() {
        return FirebaseVariables.getFirebaseAuth().getCurrentUser() == null;
    }

    private void switchMapIfRelevant() {
        // Get the URI which launched the application
        Intent intent = getIntent();

        // If the application was launched by a URI
        if (intent.getData() != null) {
            // Get the ID of the building by getting the
            // path of the URI and split it by the trailing /,
            // and get the value after the / (index 1)
            final String mapId = intent.getData().getPath().split("/")[1];
            UserBuilding.getBuildingFromBuildingId(mapId, new BuildingRunnable() {
                @Override
                public void run() {
                    UserBuilding.saveUserBuildingChoice(mapId);
                    // Save clicked building to local preferences
                    LocalPreferences.saveObjectToShared(getApplicationContext(), getString(R.string.building_key_shared_pref), getBuilding());
                    switchActivity(MainActivity.class);
                }
            }, new Runnable() {
                @Override
                public void run() {
                    createToast("Map not found");
                }
            });
        }
    }

    /**
     * Deals with requesting location
     */
    private void requestLocationAndSort() {
        // Request the location permission (if already granted this will have no affect)
        UserPermissions.requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_CALLBACK);

        // Sort the buildings if the permission was granted (a final permission check occurs)
        sortBuildingsIfAllowed();
    }

    /**
     * Setup the listeners for the interactive elements
     *
     * @param savedInstanceState Needed for Google Maps map to show
     */
    private void setupListeners(final Bundle savedInstanceState) {
        // Set the list click listener
        mapList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the clicked building (has to be final as referenced in inner class)
                final Building clickedBuilding = (Building) parent.getItemAtPosition(position);

                // Show the Google Maps map
                dialogMap.onCreate(savedInstanceState);

                // Get the Google Map instance and set it up
                dialogMap.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        // Get the coordinates of the building in a Java format
                        LatLng point = new LatLng(clickedBuilding.getLongitude(), clickedBuilding.getLatitude());

                        // Set the building location in a way so the Google Map can change
                        CameraUpdate buildingLocation =
                                CameraUpdateFactory.newLatLng(point);

                        // Change the map location
                        googleMap.moveCamera(buildingLocation);

                        // Change the map zoom
                        googleMap.setMinZoomPreference(15);

                        // Add a marker on the building location
                        googleMap.addMarker(new MarkerOptions()
                                .position(point)
                                .title(clickedBuilding.getName()));

                        // Update the map
                        dialogMap.onResume();

                    }
                });

                // Set the ok button listener
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // If the user is signed in
                        if (!FirebaseVariables.getFirebaseAuth().getCurrentUser().isAnonymous()) {
                            if (getIntent() != null) {
                                if (getIntent().getAction() != null) {
                                    // Save the user's map choice to Firebase
                                    UserBuilding.saveUserBuildingChoice(clickedBuilding.getId());
                                }
                            }
                        }

                        // Save clicked building to local preferences
                        LocalPreferences.saveObjectToShared(getApplicationContext(), getString(R.string.building_key_shared_pref), clickedBuilding);

                        // Change to the main screen
                        switchActivity(MainActivity.class);
                    }
                });


                // Set the cancel button listener
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Dismiss the dialog
                        mapDialog.dismiss();
                    }
                });

                // Show the dialog
                mapDialog.show();
            }
        });
    }

    /**
     * Setup the variables declared at the top of the class
     */
    private void setupVariables() {
        mapDialog = new Dialog(this);
        mapList = findViewById(R.id.map_list);

        // Set the dialog layout to be the custom map layout
        mapDialog.setContentView(R.layout.text_map_dialog);

        dialogMap = mapDialog.findViewById(R.id.building_location);
        ok = mapDialog.findViewById(R.id.ok_building);
        cancel = mapDialog.findViewById(R.id.cancel_building);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
    }

    /**
     * Get the ListRunnable which contains the code to run when the maps are found
     *
     * @param context The current application context
     */
    private ListRunnable<Building> getWithMapsRunnable(final Context context) {
        return new ListRunnable<Building>() {
            @Override
            public void run() {
                if (compareBuilding != null) {
                    Collections.sort(getList(), compareBuilding);
                }
                // Create an adapter for the list
                ArrayAdapter listAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, getList());
                // Set the adapter (the data)
                mapList.setAdapter(listAdapter);
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        // Based on Android documentation

        switch (requestCode) {
            // If the permission granted is for location (defined from the callback constant defined at top of class)
            case LOCATION_PERMISSION_CALLBACK: {
                // If the permission is granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sortBuildingsIfAllowed();
                }
                return;
            }
        }
    }

    /**
     * Sort the buildings based on their locations
     */
    private void sortBuildingsIfAllowed() {
        // If the location permission is not granted (this should not happen, as this has been checked previously but is a final check
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            createToast("Not allowed to access location, the buildings will not be sorted by your location");
        } else {
            // Get the location requester
            LocationRequest locationRequester = getLocationRequest();

            // Get the location callback
            LocationCallback locationCallback = getLocationCallback();

            fusedLocationProviderClient.requestLocationUpdates(locationRequester, locationCallback, null);
        }
    }

    /**
     * Get the code to run when a location is found
     *
     * @return The code to run
     */
    @NonNull
    private LocationCallback getLocationCallback() {
        return new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // For every non-null location found
                for (final Location location : locationResult.getLocations()) {
                    if (location != null) {
                        compareBuilding = new Comparator<Building>() {
                            @Override
                            public int compare(Building o1, Building o2) {
                                Location firstLocation = getLocationFromBuilding("First Location", o1);
                                Location secondLocation = getLocationFromBuilding("Second Location", o2);
                                return orderBuildingByLocation(firstLocation, secondLocation, location);
                            }
                        };
                    }
                }
            }

            /**
             * Returns the ordering for a building in the list to be shown on the Activity
             * @param firstLocation The location of the first building
             * @param secondLocation The location of the second building
             * @return The ordering value
             */
            private int orderBuildingByLocation(Location firstLocation, Location secondLocation, Location userLocation) {
                // If the distance from the first building's location is more than that from the second building's location, place it higher in the list
                if (firstLocation.distanceTo(userLocation) > secondLocation.distanceTo(userLocation)) {
                    return 1;
                }
                // If the distance from the first building's location is less than that from the second building's location, place it lower in the list
                else if (firstLocation.distanceTo(userLocation) < secondLocation.distanceTo(userLocation)) {
                    return -1;
                }
                // Place it the two buildings consecutively
                else {
                    return 0;
                }
            }

            /**
             * Get a Location object representing a building's location, given a building
             * @param provider The string to represent the location
             * @param building The building of which to get the location from
             * @return The location
             */
            @NonNull
            private Location getLocationFromBuilding(String provider, Building building) {
                Location firstLocation = new Location(provider);
                firstLocation.setLongitude(building.getLatitude());
                firstLocation.setLatitude(building.getLongitude());
                return firstLocation;
            }
        };
    }

    /**
     * Get the LocationRequest object which defines how to get the user's location
     *
     * @return
     */
    @NonNull
    private LocationRequest getLocationRequest() {
        // Every 0.1 seconds
        final long interval = (long) (0.1 * 1000);

        // So as to not drain battery but ensure that location is not null
        final int numberUpdates = 5;

        LocationRequest lr = LocationRequest.create();
        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        lr.setInterval(interval);
        lr.setNumUpdates(numberUpdates);
        return lr;
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

