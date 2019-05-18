package team16.project.team.orbis.global.objectclass;

/**
 * This represents a link between two nodes on the map
 */

public class BuildingMapEdge {
    private final BuildingMapNode nodeOne;
    private final BuildingMapNode nodeTwo;

    /**
     * Create the object given the two nodes
     *
     * @param nodeOne The one linking node
     * @param nodeTwo The other linking node
     */
    public BuildingMapEdge(BuildingMapNode nodeOne, BuildingMapNode nodeTwo) {
        //todo: if either of the nodes don't exist then throw exception
        this.nodeOne = nodeOne;
        this.nodeTwo = nodeTwo;
    }

    public BuildingMapNode getNodeOne() {
        return nodeOne;
    }

    public BuildingMapNode getNodeTwo() {
        return nodeTwo;
    }

    @Override
    public String toString() {
        return "From " + nodeOne +
                " to " + nodeTwo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BuildingMapEdge buildingMapEdge = (BuildingMapEdge) o;

        if (nodeOne != null ? !nodeOne.equals(buildingMapEdge.nodeOne) : buildingMapEdge.nodeOne != null)
            return false;
        return nodeTwo != null ? nodeTwo.equals(buildingMapEdge.nodeTwo) : buildingMapEdge.nodeTwo == null;
    }

    @Override
    public int hashCode() {
        int result = nodeOne != null ? nodeOne.hashCode() : 0;
        result = 31 * result + (nodeTwo != null ? nodeTwo.hashCode() : 0);
        return result;
    }
}
