package team16.project.team.orbis.global.objectclass.collection;

import java.util.HashSet;
import java.util.Set;

/**
 * This provides a way of grouping two Objects together in no order, so that when compared to another one of the same type, it is deemed equal if the objects are the same, not the order
 */

public class Pair<T> {
    // A Set is used as it holds an object in no specific order
    private final Set<T> objectHolder;

    /**
     * Create the Pair given the two Objects to hold
     *
     * @param valueOne An Object
     * @param valueTwo The other Object
     */
    public Pair(T valueOne, T valueTwo) {
        objectHolder = new HashSet<T>();
        objectHolder.add(valueOne);
        objectHolder.add(valueTwo);
    }

    public Set<T> getObjectHolder() {
        return objectHolder;
    }

    @Override
    public boolean equals(Object o) {
        return objectHolder.equals(((Pair) o).getObjectHolder());
    }

    @Override
    public int hashCode() {
        return objectHolder.hashCode();
    }
}
