package team16.project.team.orbis.global.objectclass;

import java.util.List;

/**
 * An implementation of a Runnable which allows a list to be passed as a parameter, and used in the code
 */

public abstract class ListRunnable<T> implements Runnable {
    private List<T> list;

    /**
     * A constructor which sets the list
     *
     * @param list The list to set the list variable to
     */
    public ListRunnable(List<T> list) {
        this.list = list;
    }

    /**
     * An empty constructor for when no variables passed
     */
    public ListRunnable() {
    }

    /**
     * Return the list variable
     *
     * @return The list
     */
    public List<T> getList() {
        return list;
    }

    /**
     * Set the list
     *
     * @param list The list to set the list variable to
     */
    public void setList(List<T> list) {
        this.list = list;
    }

    /**
     * The code to be run, set by the instantiator
     */
    @Override
    public abstract void run();
}
