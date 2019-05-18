package team16.project.team.orbis.global.objectclass;

/**
 * An implementation of a Runnable which allows a Building to be passed as a parameter, and used in the code
 */
public abstract class BuildingRunnable<T> implements Runnable {
    private Building building;

    /**
     * A constructor which sets the Building
     *
     * @param building The Building to set the building variable to
     */
    public BuildingRunnable(Building building) {
        this.building = building;
    }

    /**
     * An empty constructor for when no variables passed
     */
    public BuildingRunnable() {

    }

    /**
     * Return the Building variable
     *
     * @return The Building
     */
    public Building getBuilding() {
        return building;
    }

    /**
     * Set the Building variable
     *
     * @param building The Building
     */
    public void setBuilding(Building building) {
        this.building = building;
    }

    /**
     * The code to be run, set by the instantiator
     */
    @Override
    public abstract void run();
}
