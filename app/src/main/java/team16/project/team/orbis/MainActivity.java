package team16.project.team.orbis;

import android.*;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import team16.project.team.orbis.global.mapView.MapTouchView;
import team16.project.team.orbis.global.methods.LocalPreferences;
import team16.project.team.orbis.global.methods.database.UserBuilding;
import team16.project.team.orbis.global.objectclass.Building;
import team16.project.team.orbis.global.uiclass.AppCompatColourActivity;
import team16.project.team.orbis.global.userLocation.GpsLocation;
import team16.project.team.orbis.global.variables.FirebaseVariables;

/**
 * Underlying code for the Main screen, which provides the map of the building and navigation functionality
 */
public class MainActivity extends AppCompatColourActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView mEmailText;
    private Toolbar mToolbar;
    private Building mSelectedBuilding;
    private LinearLayout mFloorButtons;
    private LinearLayout mSidebarHeader;
    private LinearLayout mMainLayout;
    private MapTouchView mMapView;
    private BitmapFactory.Options bitmapOptions;
    private GpsLocation gpsLocation;
    private boolean trackingLocation = false;
    private List<Button> buttonsOnScreen;
    private Map<Integer, Bitmap> mapsToShow;
    private TextView testTextView; //For location testing purposes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main_screen);
        setupVariables();
        saveMaps();
        setupActionBar(mToolbar);
        setupFloatingButton();
        setupSidebar();
        setupNavigationView();
        generateFloorButtons();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(trackingLocation) {
            stopUserLocationUpdates();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (trackingLocation){
            beginUserLocationUpdates();
        }
    }



    /**
     * This method sets up the references for the variables declared above
     */
    private void setupVariables() {
        mSelectedBuilding = (Building) LocalPreferences.getObjectFromShared(getApplicationContext(), getString(R.string.building_key_shared_pref), Building.class);
        mToolbar = findViewById(R.id.toolbar);
        mFloorButtons = findViewById(R.id.floor_buttons);
        mMainLayout = findViewById(R.id.main_layout);
        mapsToShow = new HashMap<>();
        buttonsOnScreen = new ArrayList<>();
        mMapView = (MapTouchView) findViewById(R.id.mapView);

        // These are the corner points of the USB. If implementing user location services
        // these are best to be stored at the firebase.
        mMapView.setTopRightCornerCoordinates(54.97395, -1.62453);   // Top right corner point of the map image (max, max)
        mMapView.setBottomLeftCornerCoordinates(54.97315, -1.62606);
        mMapView.setZoom(2f); // Initial zoom
        gpsLocation = new GpsLocation(getApplicationContext(), mMapView);
        bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inDither = true;
        bitmapOptions.inScaled = true;
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// Important


        testTextView = findViewById(R.id.testText); // For testing purposes
        trackingLocation = true;
    }


    /**
     * Requests user permissions if not granted, checks whether location services are activated and initiates
     * user location updates.
     */
    private void beginUserLocationUpdates(){
        if(!gpsLocation.isPermissionsGranted()){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.INTERNET}, 0);
        }

        // Checking if the Location service is enabled
        if (!gpsLocation.isNetworkEnabled() || !gpsLocation.isGPSenabled()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Location service is not On. Location services have to be in High accuracy mode for Orbis to work properly.");
            alertDialogBuilder.setPositiveButton("Enable Locations service", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    startActivity(gpsLocation.goToLocationSettingsIntent());
                }
            });
            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        else{
            gpsLocation.initiateUpdates();
            gpsLocation.getUpdates(testTextView, true);
            trackingLocation = true;
        }
    }


    /**
     * Stop user location updates.
     */
    private void stopUserLocationUpdates(){
        gpsLocation.stopUpdates();
        trackingLocation = false;
    }


    /**
     * Save the maps for the building floors locally
     */
    private void saveMaps() {
        for (int i = 0; i < mSelectedBuilding.getFloors(); i++) {
            int currentI = i;

            // Create the base path to the Firebase images
            final String basicFirebaseChildReference = "map/" + mSelectedBuilding.getId() + "/" + currentI;

            // Get location where image is stored if it is a jpg
            StorageReference jpgRef = FirebaseVariables.getFirebaseStorageReference().child(basicFirebaseChildReference + ".jpg");
            // Get location where image is stored if it is a jpeg
            final StorageReference jpegRef = FirebaseVariables.getFirebaseStorageReference().child(basicFirebaseChildReference + ".jpeg");
            // Get location where image is stored if it is a png
            final StorageReference pngRef = FirebaseVariables.getFirebaseStorageReference().child(basicFirebaseChildReference + ".png");

            // Code to run if the map is found
            final OnSuccessListener<byte[]> mapFound = getMapFoundListener(currentI);

            // Search for the maps
            attemptToFindMaps(jpgRef, jpegRef, pngRef, mapFound);
        }
    }

    /**
     * Searches for the map
     *
     * @param jpgRef   Reference for the possible jpg
     * @param jpegRef  Reference for the possible jpeg
     * @param pngRef   Reference for the possible png
     * @param mapFound Code for if the map is found
     */
    private void attemptToFindMaps(StorageReference jpgRef, final StorageReference jpegRef, final StorageReference pngRef, final OnSuccessListener<byte[]> mapFound) {
        // Get 1 byte (1024*1024) at the location
        final int byteSizeAsInt = 1024 * 1024;

        // Try and find the image as a jpg
        jpgRef.getBytes(byteSizeAsInt).addOnSuccessListener(mapFound).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // If the image isn't a jpg, try and find the image as a jpeg
                jpegRef.getBytes(byteSizeAsInt).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If the image isn't a jpeg, try and find the image as a png
                        pngRef.getBytes(byteSizeAsInt).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // If it still fails, the image doesn't exist and neither does the building
                                UserBuilding.saveUserBuildingChoice("null");
                                // Show a dialog saying the building has been deleted
                                showFailureDialog();
                            }
                        }).addOnSuccessListener(mapFound);
                    }
                }).addOnSuccessListener(mapFound);
            }
        }).addOnSuccessListener(mapFound);
    }

    /**
     * Show a dialog saying that the Building has been deleted from the database
     */
    private void showFailureDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder
                .setMessage(mSelectedBuilding.getName() + "'s map has been deleted.\n\nPress OK to choose a new one.")
                .setTitle("Oh no!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switchActivity(MapChoiceActivity.class);
                    }
                })
                .create()
                .show();
    }

    /**
     * Returns the listener, which holds code to be run should a map be found
     *
     * @param floor The floor of the building which the map belongs to
     * @return The listener
     */
    @NonNull
    private OnSuccessListener<byte[]> getMapFoundListener(final int floor) {
        return new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
//                Bitmap imageFromBytes = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap imageFromBytes = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bitmapOptions);
                Bitmap map = imageFromBytes.copy(Bitmap.Config.ARGB_8888, true);

                mapsToShow.put(floor,map );
                if (floor == 0) {

                    mMapView.showMap(mapsToShow.get(0));
                }
            }
        };
    }

    /**
     * Generate the number of floor buttons required (for as many floors as there are)
     */
    private void generateFloorButtons() {
        int highestFloor = mSelectedBuilding.getLowestFloorValue() + mSelectedBuilding.getFloors();

        for (int i = mSelectedBuilding.getLowestFloorValue(), j = 0; i < highestFloor; i++, j++) {
            final Button floorButton = new Button(this);
            final int buttonSize = 20;

            String buttonText = getFloorRepresentation(i);
            floorButton.setText(buttonText);
            // Set the background colour to be the building's colour
            floorButton.setBackgroundColor(mSelectedBuilding.getColour());
            // Set the text colour to be white
            floorButton.setTextColor(Color.WHITE);
            // Set the id to be the floor number starting from 1 (has to be an integer, and not necessarily unique)
            floorButton.setId(j);
            // Set the width of the button to be the screen's width divided by the number of floors, and the height to match the parent element
            floorButton.setLayoutParams(new LinearLayout.LayoutParams(
                    getScreenWidth() / mSelectedBuilding.getFloors(),
                    LinearLayout.LayoutParams.MATCH_PARENT));
            // Set the font size
            floorButton.setTextSize(buttonSize);

            // Set the listener of the button to change the map displayed
            final int floor = j;
            floorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Set the map image to be the image saved for the floor
                    mMapView.showMap(mapsToShow.get(floor));
                    if(trackingLocation) { // If tracking user location place last known location on the new floor
                        gpsLocation.updateMapViewWithLastKnownLocation();
                    }
                    for (Button button : buttonsOnScreen) {
                        button.setTextColor(Color.WHITE);
                    }
                    floorButton.setTextColor(getDarkenedColour(mSelectedBuilding.getColour()));
                }
            });

            // Add the element to the view
            mFloorButtons.addView(floorButton);
            // Add the button to the list of the buttons on screen
            buttonsOnScreen.add(floorButton);
        }

        // Change the view colour to match that of the last button
        mFloorButtons.setBackgroundColor(mSelectedBuilding.getColour());
    }

    /**
     * Get the string representation of the floor number for the floor button
     *
     * @param i Floor number (starting from 0)
     * @return The string representation
     */
    private String getFloorRepresentation(int i) {
        // Set the text to be the value of i (the floor)
        String buttonText = Integer.toString(i);

        // If the floor is the first floor to be added, set the text to be G (ground)
        if (i == 0) {
            buttonText = "G";
        }
        return buttonText;
    }

    /**
     * Get the width of the device's screen
     *
     * @return The width
     */
    private int getScreenWidth() {
        // DisplayMetrics can hold display dimensions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        // Store the screen's display dimensions in the displayMetrics
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // Return the screen's width
        return displayMetrics.widthPixels;
    }

    /**
     * Setup the toolbar's information
     *
     * @param mToolbar The toolbar
     */
    private void setupActionBar(Toolbar mToolbar) {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(mSelectedBuilding.getName());
    }

    /**
     * This method sets the default selected item in the sidebar
     */
    private void setupNavigationView() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * This method populates the sidebar and sets up the pre-configured listeners
     */
    private void setupSidebar() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * This method sets up the floating button (plus button) and its action
     */
    private void setupFloatingButton() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchActivity(SearchActivity.class);
            }
        });
    }


    /**
     * Android Studio generated code
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     * It also populates the sidebar with the user's email address/appropriate text
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_screen, menu);

        // These have to be initialised here as they have not been created when setupVariables() is called
        mEmailText = findViewById(R.id.text_email);
        mSidebarHeader = findViewById(R.id.layout_user);

        setUserEmailAndColour();

        return true;
    }

    /**
     * Populates the sidebar with the user's email address/appropriate text
     */
    private void setUserEmailAndColour() {
        setUserEmailText();
        setSidebarColour();
    }

    private void setUserEmailText() {
        // If the user is not anonymous
        if (!FirebaseVariables.getFirebaseAuth().getCurrentUser().isAnonymous()) {
            // Add the user's email address to the sidebar
            mEmailText.setText(FirebaseVariables.getFirebaseAuth().getCurrentUser().getEmail());
        } else {
            // Add appropriate text
            mEmailText.setText(getString(R.string.anonymous_user));
        }
    }

    private void setSidebarColour() {
        mSidebarHeader.setBackgroundColor(mSelectedBuilding.getColour());
    }

    /**
     * This handles navigation when items in the sidebar are clicked
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_changemap) {
            switchActivity(MapChoiceActivity.class, getString(R.string.update_map_choice));
        } else if (id == R.id.nav_events) {
            switchActivity(EventsActivity.class);
        } else if (id == R.id.nav_logout) {
            switchActivity(LoginActivity.class, getString(R.string.logout_action));
        } else if (id == R.id.nav_quick) {

        } else if (id == R.id.nav_route) {

        } else if (id == R.id.nav_settings) {
            switchActivity(SettingsActivity.class);
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}