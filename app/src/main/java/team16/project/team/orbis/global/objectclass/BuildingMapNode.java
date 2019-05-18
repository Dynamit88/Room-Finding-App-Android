package team16.project.team.orbis.global.objectclass;

import java.io.Serializable;

/**
 * This represents a node on the map
 */

public class BuildingMapNode implements Serializable {
    private final String id;
    private final String name;
    private final int floor;
    private final BuildingMapNodeType nodeType;

    /**
     * Create the node given the name of it, the floor of the node, and the type of the node
     *
     * @param name     The name of the node
     * @param floor    The floor of the node
     * @param nodeType The type of the node
     */
    public BuildingMapNode(String id, String name, int floor, BuildingMapNodeType nodeType) {
        // If the name is invalid, throw an exception
        if (name.length() < 1) {
            throw new IllegalArgumentException("The name must have at least one character");
        }

        //todo: if name already in use then throw exception, but is this needed? this should already be alidated when saved to databases
        this.id = id;
        this.name = name;
        this.floor = floor;
        this.nodeType = nodeType;
    }


    public String getName() {
        return name;
    }

    public int getFloor() {
        return floor;
    }

    public BuildingMapNodeType getNodeType() {
        return nodeType;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BuildingMapNode that = (BuildingMapNode) o;

        if (floor != that.floor) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return nodeType == that.nodeType;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + floor;
        result = 31 * result + (nodeType != null ? nodeType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}