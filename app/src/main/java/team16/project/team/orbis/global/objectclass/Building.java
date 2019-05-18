package team16.project.team.orbis.global.objectclass;

/**
 * A class to represent a Building to be navigated around
 */

public final class Building {
    private final String id;
    private final String name;
    private final double longitude;
    private final double latitude;
    private final int floors;
    private final int colour;
    private final int lowestFloorValue;

    /**
     * Instantiate a Building with all fields
     *
     * @param id               The building's ID
     * @param name             The building's name
     * @param longitude        The building's longitude
     * @param latitude         The building's latitude
     * @param floors           The number of floors in the building
     * @param colour           The primary colour of the building
     * @param lowestFloorValue The value of the first floor in the building
     */
    public Building(String id, String name, double longitude, double latitude, int floors, int colour, int lowestFloorValue) {
        this.id = id;
        this.colour = colour;
        this.floors = floors;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.lowestFloorValue = lowestFloorValue;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getFloors() {
        return floors;
    }

    public int getColour() {
        return colour;
    }

    public int getLowestFloorValue() {
        return lowestFloorValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Building building = (Building) o;

        if (Double.compare(building.longitude, longitude) != 0) return false;
        if (Double.compare(building.latitude, latitude) != 0) return false;
        if (floors != building.floors) return false;
        if (colour != building.colour) return false;
        if (lowestFloorValue != building.lowestFloorValue) return false;
        if (id != null ? !id.equals(building.id) : building.id != null) return false;
        return name != null ? name.equals(building.name) : building.name == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + floors;
        result = 31 * result + colour;
        result = 31 * result + lowestFloorValue;
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}


