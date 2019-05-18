package team16.project.team.orbis.global.userLocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import team16.project.team.orbis.global.mapView.MapTouchView;
import team16.project.team.orbis.global.userLocation.util.KalmanFilter;
import team16.project.team.orbis.global.userLocation.util.Point3D;
import static android.content.ContentValues.TAG;

/**
 * Created by Ivan Mykolenko on 28/01/2018.
 */

public class GpsLocation implements LocationListener {
    private Context context;
    private LocationManager lm;
    private Criteria criteria;
    private TextView textView, textView2;//TextView to send updates to
    private long locationRequestInterval = 1000;
    private float locationRequestDistance = 0;
    private Location oldLocation;
    private String bestProvider;
    private final int TIME_THRESHOLD = 5 * 1000; // 5 Seconds
    private final int ACCURACY_THRESHOLD = 10; // 10 meters radius accuracy precision

    //Filtering//
    private KalmanFilter kalmanFilter;
    private float currentSpeed = 0.0f; // Meters/second
    private long runStartTimeInMillis;
    private Location predictedLocation;
    private boolean logging = false, counting = true;
    private int goodLocations = 0, rejectedLocations = 0;
    private boolean locationPassed =false;

    //MapView//
    private Canvas canvas;
    private Bitmap map;
    private MapTouchView mapTouchView;
    private Paint userPointerPaint;
    private int userPointerRadius = 10;
    private Point3D userPointer; //Point to send updates to


    ///////////////////////////////////Constructors////////////////////////////////////
    public GpsLocation(Context context) {
        super();
        this.context = context; //ApplicationgetContext
        init();
    }
    public GpsLocation(Context context, Point3D userPointer) {
        super();
        this.context = context;
        getUpdates(userPointer);
        init();
    }
    public GpsLocation(Context context, MapTouchView mapTouchView) {
        super();
        this.context = context;
        setMapTouchView(mapTouchView);
        init();
    }
    public GpsLocation(Context context, TextView textView) { // textView - is a text field to which the location gets printed
        super();
        this.context = context;
        this.textView = textView;
        init();
    }

    public GpsLocation(Context context, TextView textView, TextView textView2) { // textView - is a text field to which the location gets printed
        super();
        this.context = context;
        this.textView = textView;
        this.textView2 = textView2;
        init();
    }
    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Common initialization method for the constructors.
     */
    private void init() {
        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        kalmanFilter = new KalmanFilter(3);
        oldLocation = new Location("");
        predictedLocation = new Location("");

        // Criteria to select the most suitable provider
        criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_HIGH); // Chose desired power consumption level
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // Choose accuracy requirement
        criteria.setSpeedRequired(false); // Chose if speed for first location fix is required
        criteria.setAltitudeRequired(false); // Altitude?
        criteria.setBearingRequired(false); // Bearing
        criteria.setCostAllowed(true); // Choose if this provider can waste money
        bestProvider = lm.getBestProvider(criteria,true);

        // User marker style
        userPointerPaint = new Paint();
        userPointerPaint.setAntiAlias(true);
        userPointerPaint.setColor(Color.BLUE);
    }

    /**
     * The method is called to begin user location updates.
     */
    @SuppressLint("MissingPermission")
    public void initiateUpdates () {
        if(isPermissionsGranted()) { // If user has given permissions to access location services
            if (isGPSenabled()) { // If precise location provider is enabled
                runStartTimeInMillis = (long)(SystemClock.elapsedRealtimeNanos() / 1000000); // Begin process
                lm.requestSingleUpdate(lm.NETWORK_PROVIDER,this,null); // Request fast location update
                lm.requestLocationUpdates(locationRequestInterval, locationRequestDistance, criteria, this, null); // Begin constant location updates
            }
            else {
                if (logging) {Log.e(TAG, "GPS disabled!");}
            }
        }
        else {
            if(logging) {Log.e(TAG, "Permissions denied!");}
        }
    }

    ///////////////////////// Location Listener Methods//////////////////////////////////
    @Override
    public void onLocationChanged(Location location) {
        // On user's movement call method to update location and return new information
        // to a TextView and/or a MapTouchView
        updateUserLocation(location);
    }
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }
    @Override
    public void onProviderEnabled(String s) {
        // Notify user
        Toast.makeText(context,"Provider enabled: " + bestProvider, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onProviderDisabled(String s) {
        // Notify user
        Toast.makeText(context,"Provider disabled: " + bestProvider, Toast.LENGTH_SHORT).show();
    }
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * The updateUserLocation method is called every time the Location Listener receives a new location
     * update. This method calls filtering procedure and returns filtered data back to the user.
     * @param location is a Location object returned by the Location Listener.
     */
    public void updateUserLocation (Location location){
        if (location != null) {
            if (!locationPassed){ // Allow quick location fix once
                if (textView != null) { // if a text view has been assigned
                    textView.setText(formatLocation(location));
                }
                if (mapTouchView != null){ // if a map has been assigned
                    redrawMapTouchViewCanvas(location.getLatitude(), location.getLongitude());
                }
                locationPassed = true; // Location fix passed
            }

            if (filterLocation(location)) { //isBetterLocation(getOldLocation(),location)
                if (textView != null) { // if a text view has been assigned
                    textView.setText(formatLocation(predictedLocation));
                }
                if (mapTouchView != null){ // if a map has been assigned
                    redrawMapTouchViewCanvas(predictedLocation.getLatitude(), predictedLocation.getLongitude());
                }
                if (counting) {
                    goodLocations++; // Increment successful locations counter
                }
                if(logging){Log.d(TAG,"Good location found (count: " + goodLocations +") " + formatLocation(predictedLocation));}
            }
            else {
                if (counting) {
                    rejectedLocations++; // Location rejected
                }
            }
            if (textView2 != null) {
                textView2.setText("Updated: " + goodLocations + "\t Rejected: " + rejectedLocations + "\t Provider: " + getBestProvider());
            }
            setOldLocation(predictedLocation);
        }
        else{
            if (textView !=null) {
                textView.setText("Location is null. " + Calendar.getInstance().getTime());
            }
            if(logging){Log.d(TAG,"Location is null.");}
        }
        setOldLocation(predictedLocation);
    }

    /**
     * Location updates filter. Filters out mal location updates. Kalman's filter is used to calculate
     * precise user location based on good and mal location data.
     * @param location is a Location object.
     * @return true if a Location object has passed filtering and is good enough to be returned to a user.
     */
    private boolean filterLocation(Location location){
        long age = getLocationAge(location);
        if(age > TIME_THRESHOLD){ // Check if new location is new enough
            if (logging) {Log.d(TAG, "Location is old");}
            return false;
        }
        if(location.getAccuracy() <= 0){ // Check whether the location is valid
            if (logging){Log.d(TAG, "Latitude and longitude values are invalid.");}
            return false;
        }
        if (location.getLongitude() == getOldLocation().getLongitude() && location.getLatitude() == getOldLocation().getLatitude()) {
            if (logging) {
                Log.d(TAG, "Location has not changed.");
            }
            return false;
        }

        if(location.getAccuracy() > ACCURACY_THRESHOLD){ // If location is less accurate than ACCURACY_THRESHOLD, return false.
            if (logging){Log.d(TAG, "Accuracy is too low.");}
            return false;
        }

        //////////////////////////////// Kalman Filter ////////////////////////////////
        float Qvalue;
        long locationTimeInMillis = (long)(location.getElapsedRealtimeNanos() / 1000000);
        long elapsedTimeInMillis = locationTimeInMillis - runStartTimeInMillis;
        if(currentSpeed == 0.0f){
            Qvalue = 3.0f; // 3 meters per second
        }else{
            Qvalue = currentSpeed; // Meters per second
        }
        kalmanFilter.Process(location.getLatitude(), location.getLongitude(), location.getAccuracy(), elapsedTimeInMillis, Qvalue);
        predictedLocation.setLatitude(kalmanFilter.getLatitude()); // Predicted latitude
        predictedLocation.setLongitude(kalmanFilter.getLongitude()); // Predicted longitude
        float predictedDeltaInMeters =  predictedLocation.distanceTo(location); // Distance between location and predicted location

        if(predictedDeltaInMeters > 60){
            if (logging){Log.d(TAG, "Kalman Filter detects mal GPS, we should probably remove this from track");}
            kalmanFilter.setConsecutiveRejectCount (kalmanFilter.getConsecutiveRejectCount()+ 1);
            if(kalmanFilter.consecutiveRejectCount > 3){
                kalmanFilter = new KalmanFilter(3); // Reset Kalman's filter if it rejects more than 3 times in raw.
            }
            return false;
        }
        else{
            kalmanFilter.consecutiveRejectCount = 0;
        }
        if (logging){Log.d(TAG, "Location quality is good enough.");}
        currentSpeed = location.getSpeed();
        return true;
    }

    /**
     * The method isBetterLocation compares two Location objects in regards of geolocation's
     * accuracy and time the location data was taken. Returns true if newLocation is better than
     * oldLocation.
     * @param oldLocation is a Location object.
     * @param newLocation is a Location object.
     * @return true if newLocation is better than oldLocation.
     */
    public boolean isBetterLocation(Location oldLocation, Location newLocation) {
        // If there is no old location, of course the new location is better.
        if (oldLocation == null) {
            return true;
        }
        boolean isNewer = newLocation.getTime() > oldLocation.getTime(); // Check if new location is newer in time.
        boolean isMoreAccurate = newLocation.getAccuracy() < oldLocation.getAccuracy(); // Check if new location more accurate. Accuracy is radius in meters, so less is better.

        if (isMoreAccurate && isNewer) { // More accurate and newer is always better.
            return true;
        } else if (isMoreAccurate && !isNewer) {
            // More accurate but not newer can lead to bad fix because of user movement.
            // Let us set a threshold for the maximum tolerance of time difference.
            long timeDifference = newLocation.getTime() - oldLocation.getTime();

            // If time difference is not greater then allowed threshold we accept it.
            if (timeDifference > -TIME_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    /**
     * getLocationAge finds a difference between the current time and the time location data has been collected.
     * @param newLocation is a Location object.
     * @return how old the newLocation is.
     */
    private long getLocationAge(Location newLocation){
        long locationAge;
        if(android.os.Build.VERSION.SDK_INT >= 17) { // Check baseband
            long currentTimeInMilli = (long)(SystemClock.elapsedRealtimeNanos() / 1000000);
            long locationTimeInMilli = (long)(newLocation.getElapsedRealtimeNanos() / 1000000);
            locationAge = currentTimeInMilli - locationTimeInMilli;
        }else{
            locationAge = System.currentTimeMillis() - newLocation.getTime();
        }
        return locationAge;
    }


    /**
     * Converts latitude and longitude user coordinates into an appropriate pixel equivalent for the map image
     * and places the user marker on the map.
     * @param latitude is user's latitude returned by the location listener.
     * @param longitude is user's longitude returned by the location listener.
     */
    private void redrawMapTouchViewCanvas (double latitude, double longitude){
        if (mapTouchView != null) { // Validating parameters
            if(canvas != null) {
                map = mapTouchView.getMapBitmap();
                if(map != null) {
                    // Converting geolocation coordinates into map pixel coordinates
                        double x = Math.abs(latitude - mapTouchView.getBottomLeftCornerCoordinates().getLatitude()) * mapTouchView.getScaleRatioX();
                        double y = Math.abs(longitude - mapTouchView.getBottomLeftCornerCoordinates().getLongitude()) * mapTouchView.getScaleRatioY();
                        Bitmap mutableBitmap = map.copy(Bitmap.Config.ARGB_8888, true); //Create new mutable bitmap
                        // Place user marker
                        canvas.setBitmap(mutableBitmap);
                        canvas.drawCircle((float) y, (float) (canvas.getHeight() - x), getUserPointerRadius(), userPointerPaint);
                        // Return map with user marker back to the user
                        mapTouchView.setImageBitmap(mutableBitmap);
                }
                else{
                    if(logging){Log.d(TAG,"Map bitmap is null");}
                }
            }
            else{
                if(logging){Log.d(TAG,"Canvas is null");}
            }
        }
        else{
            if(logging){Log.d(TAG, "MapTouch view is null");}
        }
    }


    /**
     * Places user marker on the map in the place of last known user's location.
     */
    @SuppressLint("MissingPermission")
    public void updateMapViewWithLastKnownLocation(){
        if(isPermissionsGranted()) {
            Location fix = lm.getLastKnownLocation(bestProvider);
            redrawMapTouchViewCanvas(fix.getLatitude(),fix.getLongitude());
        }
        else{
            if(logging){Log.d(TAG,"Permissions denied");}
        }
    }


    /**
     * Method to assign a MapTouchView object to receive user location updates to.
     * @param mapTouchView is a MapTouchView object on XML layout.
     */
    public void setMapTouchView(MapTouchView mapTouchView){
        this.mapTouchView = mapTouchView;
        canvas = mapTouchView.getCanvas();
        map = mapTouchView.getMapBitmap();
    }


    /**
     * Assign Point3D object to receive user location updates.
     * @param userPointer a Point3D object user location updates are returned to.
     */
    public void getUpdates (Point3D userPointer){
        this.userPointer = userPointer;
    }


    /**
     * Assign TextView object to receive user location updates.
     * @param textView a textView object user location updates are returned to in a form of a string.
     * @param filteringUpdatesOnly is a condition on which the class return either location or filter updates
     * to the TextView.
     */
    public void getUpdates (TextView textView, Boolean filteringUpdatesOnly) {
        if (filteringUpdatesOnly) {
            this.textView2 = textView;
        }
        else{
            this.textView = textView;
        }
    }

    /**
     * Stop requesting user location updates.
     */
    public void stopUpdates(){ // Location Listener method
        lm.removeUpdates(this);
        mapTouchView.setImageBitmap(map.copy(Bitmap.Config.ARGB_8888, true));
    }

    /**
     * Returns the most suitable location service's provider in accordance to Criteria object.
     * @return best provider's name.
     */
    public String getBestProvider(){
        return lm.getBestProvider(criteria,true);
    }

    /**
     * Formatted location.
     * @param location is a Location object.
     * @return a string with Latitude, Longitude and Altitude coordinates.
     */
    private String formatLocation(Location location) {
        return String.format("Coordinates:\nLatitde = %f\nLongitude = %f\nAltitude = %f\n\nTime:\n%tT",location.getLatitude(),location.getLongitude(), location.getAltitude(),location.getTime());
    }

    /**
     * Setter for oldLocation variable.
     * @param oldLocation is a Location object.
     */
    private void setOldLocation(Location oldLocation) {
        this.oldLocation = oldLocation;
    }

    /**
     * Getter for oldLocation variable.
     * @return
     */
    public Location getOldLocation() {
        return oldLocation;
    }

    /**
     * Check whether permissions to access user location have been granted.
     * @return true if permissions granted.
     */
    public boolean isPermissionsGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check GPS state.
     * @return boolean.
     */
    public boolean isGPSenabled (){return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);}

    /**
     * Check Network state.
     * @return boolean.
     */
    public boolean isNetworkEnabled (){return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}


    /**
     * Generates a new intent to navigate to device's settings.
     * @return an Intent object.
     */
    public Intent goToLocationSettingsIntent(){
        return  new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    }

    public void startLogging (){
        logging = true;
    }
    public void stopLogging (){
        logging = false;
    }
    public void startCounting (){
        counting = true;
    }
    public void stopCounting (){
        counting = false;
    }

    /**
     * Change color of the user marker on a map.
     * @param color is a Color integer.
     */
    public void setUserPointerColor(int color){
        userPointerPaint.setColor(color);
    }

    /**
     * Change the size of the user marker on a map.
     * @param radius is a radius of user's marker circle.
     */
    public void setUserPointerRadius(int radius){
        userPointerRadius = radius;
    }

    /**
     * Getter for user marker's radius variable.
     * @return int radius.
     */
    public int getUserPointerRadius(){
        return userPointerRadius;
    }

}

