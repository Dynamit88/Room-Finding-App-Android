package team16.project.team.orbis.global.userLocation.util;

/**
 * Created by Ivan Mykolenko on 30/01/2018.
 */
public class KalmanFilter {
	private final float MinAccuracy = 1;
	private float qMetresPerSecond;
	private long timeStampsMilliseconds;
	private double latitude;
	private double longitude;
	private float variance;
	// P matrix. Negative means object uninitialised.
	// Units irrelevant, as long as same units used throughout.
	public int consecutiveRejectCount;


	public KalmanFilter(float qMetresPerSecond) {
		this.qMetresPerSecond = qMetresPerSecond;
		variance = -1;
		consecutiveRejectCount = 0;
	}

	/**
	 * Kalman filter processing for latitude and longitude.
	 * @param lat_measurement is a new measurement of latitude.
	 * @param lng_measurement is a new measurement of longitude.
	 * @param accuracy is a measurement of 1 standard deviation error in metres.
	 * @param timeStampsMilliseconds is a time of measurement.
	 * @param qMetresPerSecond approximation of user speed.
	 */
	public void Process(double lat_measurement, double lng_measurement, float accuracy, long timeStampsMilliseconds, float qMetresPerSecond) {
		this.qMetresPerSecond = qMetresPerSecond;
		if (accuracy < MinAccuracy)
			accuracy = MinAccuracy;
		if (variance < 0) {
			// if variance < 0, object is unitialised, so initialise with current values
			this.timeStampsMilliseconds = timeStampsMilliseconds;
			latitude = lat_measurement;
			longitude = lng_measurement;
			variance = accuracy * accuracy;
		} else { // else apply Kalman filter methodology
			long TimeInc_milliseconds = timeStampsMilliseconds
					- this.timeStampsMilliseconds;
			if (TimeInc_milliseconds > 0) {
				// Time has moved on, so the uncertainty in the current position increases
				variance += TimeInc_milliseconds * qMetresPerSecond * qMetresPerSecond / 1000;
				this.timeStampsMilliseconds = timeStampsMilliseconds;
				// TO DO: COULD USE VELOCITY INFORMATION HERE TO GET A BETTER ESTIMATE OF CURRENT POSITION
			}
			// Because K is dimensionless, it doesn't matter that variance has different units to lat and lng
			float K = variance / (variance + accuracy * accuracy);
			latitude += K * (lat_measurement - latitude); // apply K
			longitude += K * (lng_measurement - longitude); // apply K
			// new Covarariance matrix is (IdentityMatrix - K) * Covarariance
			variance = (1 - K) * variance;
		}
	}

	/**
	 * setState method assigns new values for the filter.
	 * @param latitude is a new measurement of latitude.
	 * @param longitude is a new measurement of longitude.
	 * @param accuracy is a measurement of 1 standard deviation error in metres.
	 * @param timeStampsMilliseconds is a time of measurement.
	 */
	public void setState(double latitude, double longitude, float accuracy, long timeStampsMilliseconds) {
		this.latitude = latitude;
		this.longitude = longitude;
		variance = accuracy * accuracy;
		this.timeStampsMilliseconds = timeStampsMilliseconds;
	}

	/**
	 * Return the time of measurement.
	 * @return time in milliseconds.
	 */
	public long getTimeStamp() {
		return timeStampsMilliseconds;
	}

	/**
	 * Getter for Latitude.
	 * @return latitude value.
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Getter for Longitude.
	 * @return longitude value.
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Get measurement of 1 standard deviation error in metres.
	 * @return float accuracy in meters.
	 */
	public float getAccuracy() {
		return (float) Math.sqrt(variance);
	}


	/**
	 * Returns the number of rejected locations.
	 * @return in number of rejections.
	 */
	public int getConsecutiveRejectCount() {
		return consecutiveRejectCount;
	}

	/**
	 * Set number of rejected locations for further calculations to be made.
	 * @param consecutiveRejectCount int number of rejections.
	 */
	public void setConsecutiveRejectCount(int consecutiveRejectCount) {
		this.consecutiveRejectCount = consecutiveRejectCount;
	}
	
	
}