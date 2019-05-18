package team16.project.team.orbis.global.userLocation.util;

/**
 * Created by Ivan Mykolenko on 09/02/2018.
 */

public class Point3D {
    double latitude;        //54
    double longitude;       //-1
    double altitude;

    /**
     * Instantiates a new Point3D with its data set to zero.
     */
    public Point3D (){
    }

    /**
     * Instantiates a new Point3D with the specified coordinates.
     * @param latitude the latitude coordinate.
     * @param longitude the longitude coordinate.
     */
    public Point3D (double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Instantiates a new Point3D with the specified coordinates.
     * @param latitude the latitude coordinate.
     * @param longitude the longitude coordinate.
     * @param altitude the altitude coordinate.
     */
    public Point3D (double latitude, double longitude, double altitude){
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public double getLatitude (){
        return latitude;
    }

    public double getLongitude (){
        return longitude;
    }

    public double getAltitude (){
        return  altitude;
    }

    public String toString (){
        return getClass().getName() + "[Latitude=" + latitude + ",Longitude=" + longitude + ",Altitude="+ altitude +"]";
    }

    /**
     * Sets the point's coordinates.
     * @param latitude the latitude coordinate.
     * @param longitude the longitude coordinate.
     */
    public void setLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Sets the point's coordinates.
     * @param latitude the latitude coordinate.
     * @param longitude the longitude coordinate.
     * @param altitude the altitude coordinate.
     */
    public void setLocation(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    /**
     * Sets the point's coordinates by copying them from another point.
     * @param p the point to copy the data from.
     */
    public void setLocation(Point3D p) {
        setLocation(p.getLatitude(), p.getLongitude(), p.getAltitude());
    }


    /**
     * Finds the square of the distance between the two specified points.
     * @param latitude1 the latitude coordinate of the first point.
     * @param longitude1 the longitude coordinate of the first point.
     * @param latitude2 the latitude coordinate of the second point.
     * @param longitude2 the longitude coordinate of the second point.
     * @return the square of the distance between the two specified points.
     */
    public static double distanceSq(double latitude1, double longitude1, double latitude2, double longitude2) {
        latitude2 -= latitude1;
        longitude2 -= longitude1;
        return latitude2 * latitude2 + longitude2 * longitude2;
    }
    /**
     * Finds the square of the distance between this point and the specified point.
     * @param latitude the latitude coordinate of the point.
     * @param longitude the longitude coordinate of the point.
     * @return the square of the distance between this point and the specified point.
     */
    public double distanceSq(double latitude, double longitude) {
        return distanceSq(getLatitude(), getLongitude(), latitude, longitude);
    }
    /**
     * Finds the square of the distance between this point and the specified point.
     * @param p the other point.
     * @return the square of the distance between this point and the specified point.
     */
    public double distanceSq(Point3D p) {
        return distanceSq(getLatitude(), getLongitude(), p.getLatitude(), p.getLongitude());
    }

    /**
     * Finds the distance between this point and the specified point.
     * @param latitude the latitude coordinate of the point.
     * @param longitude the longitude coordinate of the point.
     * @return the distance between this point and the specified point.
     */
    public double distance(double latitude, double longitude) {
        return Math.sqrt(distanceSq(latitude, longitude));
    }
    /**
     * Finds the distance between the two specified points.
     * @param latitude1 the latitude coordinate of the first point.
     * @param longitude1 the longitude coordinate of the first point.
     * @param latitude2 the latitude coordinate of the second point.
     * @param longitude2 the longitude coordinate of the second point.
     * @return the distance between the two specified points.
     */
    public static double distance(double latitude1, double longitude1, double latitude2, double longitude2) {
        return Math.sqrt(distanceSq(latitude1, longitude1, latitude2, longitude2));
    }
    /**
     * Finds the distance between this point and the specified point.
     * @param p the other point.
     * @return the distance between this point and the specified point.
     */
    public double distance(Point3D p) {
        return Math.sqrt(distanceSq(p));
    }

}
