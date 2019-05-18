package team16.project.team.orbis.global.objectclass;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import team16.project.team.orbis.global.methods.LocalPreferences;
import team16.project.team.orbis.global.objectclass.collection.Pair;

/**
 * This represents a map of a building.
 */

public class BuildingMap {
    private final List<BuildingMapNode> nodes;
    private final List<BuildingMapEdge> edges;
    private final Map<Pair<BuildingMapNode>, List<BuildingMapNode>> directions;
    private final boolean useLift;

    /**
     * The constructor if LocalPreferences is to be used to find the lift setting.
     *
     * @param nodes   The nodes (rooms/stairs/lifts) in the map
     * @param edges   The pathways between the nodes in the map
     * @param context The context for getting the lift setting
     */
    public BuildingMap(List<BuildingMapNode> nodes, List<BuildingMapEdge> edges, Context context) {
        this.nodes = Collections.unmodifiableList(nodes);
        this.edges = Collections.unmodifiableList(edges);
        directions = generateDirections();
        useLift = LocalPreferences.isLiftEnabled(context);
    }

    /**
     * The constructor if the lift setting is to be mocked
     *
     * @param nodes   The nodes (rooms/stairs/lifts) in the map
     * @param edges   The pathways between the nodes in the map
     * @param useLift The use lift setting
     */
    public BuildingMap(List<BuildingMapNode> nodes, List<BuildingMapEdge> edges, boolean useLift) {
        this.nodes = Collections.unmodifiableList(nodes);
        this.edges = Collections.unmodifiableList(edges);
        this.useLift = useLift;
        directions = generateDirections();
    }

    public List<BuildingMapNode> getNodes() {
        return nodes;
    }

    public List<BuildingMapEdge> getEdges() {
        return edges;
    }

    public Map<Pair<BuildingMapNode>, List<BuildingMapNode>> getAllDirections() {
        return directions;
    }

    /**
     * Return the directions between two nodes from the directions map
     *
     * @param from The node to travel from
     * @param to   The node to travel to
     * @return The directions
     */
    public List<BuildingMapNode> getSpecificDirections(BuildingMapNode from, BuildingMapNode to) {
        return directions.get(new Pair<>(from, to));
    }

    /**
     * Generate all the directions for the map between all of the nodes, from the nodes and edges already specified
     *
     * @return The directions
     */
    private Map<Pair<BuildingMapNode>, List<BuildingMapNode>> generateDirections() {
        Map<Pair<BuildingMapNode>, List<BuildingMapNode>> allDirections = new HashMap<>();
        for (BuildingMapNode startNode : nodes) {
            for (BuildingMapNode endNode : nodes) {
                // If the node is not a lift, nor a staircase
                if (startNode.getNodeType() != BuildingMapNodeType.LIFT && startNode.getNodeType() != BuildingMapNodeType.STAIRS) {
                    // If the nodes do not equal each other
                    if (!endNode.equals(startNode) && endNode.getNodeType() != BuildingMapNodeType.LIFT && endNode.getNodeType() != BuildingMapNodeType.STAIRS) {
                        // Create a pair representing the nodes
                        Pair<BuildingMapNode> twoPoints = new Pair<>(startNode, endNode);
                        List<BuildingMapNode> directions = generateDirectionsBetweenNodes(startNode, endNode);
                        // If it is possible to navigate between two nodes
                        if (directions != null) {
                            // Needed as the nodes are put in the list backwards
                            Collections.reverse(directions);
                            // Add the directions to the map
                            allDirections.put(twoPoints, directions);
                        }
                    }
                }
            }
        }
        return allDirections;
    }

    /**
     * Generate the directions between the nodes
     *
     * @param startNode The node to start from
     * @param endNode   The node to navigate to
     * @return The solution in backwards order (if it exists)
     */
    private List<BuildingMapNode> generateDirectionsBetweenNodes(BuildingMapNode startNode, BuildingMapNode endNode) {
        List<BuildingMapNode> visitedStart = new ArrayList<>();
        visitedStart.add(startNode);
        return generateDirectionsBetweenNodes(startNode, endNode, visitedStart, new ArrayList<BuildingMapNode>());
    }

    /**
     * Generate the directions between the nodes, given a list of visited nodes and a possible solution
     *
     * @param startNode The node to start from
     * @param endNode   The node to navigate to
     * @param visited   The list of visited nodes
     * @param solution  The possible solution
     * @return The solution in backwards order (if it exists)
     */
    private List<BuildingMapNode> generateDirectionsBetweenNodes(BuildingMapNode startNode, BuildingMapNode endNode, List<BuildingMapNode> visited, List<BuildingMapNode> solution) {
        BuildingMapNode nextNode = null;
        for (BuildingMapEdge edge : edges) {
            // Needed for if the for loop reiterates without calling the method again (reset it to null)
            nextNode = null;
            if (edge.getNodeOne().equals(startNode)) {
                nextNode = edge.getNodeTwo();
            } else if (edge.getNodeTwo().equals(startNode)) {
                nextNode = edge.getNodeOne();
            } else if (solution.contains(endNode)) {
                return solution;
            } else if (nextNode == null) {
                continue;
            }

            if (!visited.contains(nextNode)) {
                visited.add(nextNode);
                // If the destination has been found
                if (nextNode.equals(endNode)) {
                    solution.add(nextNode);
                    solution.add(startNode);
                    return solution;
                } else {
                    // If the node is a lift/staircase and the route is supposed to take use the lift/stairs
                    if ((nextNode.getNodeType().equals(BuildingMapNodeType.LIFT) && useLift) || (nextNode.getNodeType().equals(BuildingMapNodeType.STAIRS) && !useLift)) {
                        // Set the difference between the lift/staircase found and the current floor to be the smallest value
                        int diffOfClosestToCurrentFloor = Integer.MIN_VALUE;
                        BuildingMapNode closestRelatedNode = nextNode;

                        for (BuildingMapEdge floorChangeEdge : edges) {
                            // If the first node of the edge is the current node (nextNode)
                            if (floorChangeEdge.getNodeOne().equals(nextNode)) {
                                // Get the other node as the possible new node
                                BuildingMapNode newFloorNode = floorChangeEdge.getNodeTwo();
                                // If it is the same type as the current node (stair/lift)
                                if (newFloorNode.getNodeType().equals(nextNode.getNodeType())) {
                                    // Calculate the difference between the floors
                                    int floorDiff = endNode.getFloor() - newFloorNode.getFloor();
                                    // If the difference is smaller than the current smallest distance
                                    if (floorDiff >= 0 && floorDiff < diffOfClosestToCurrentFloor) {
                                        diffOfClosestToCurrentFloor = floorDiff;
                                        closestRelatedNode = newFloorNode;
                                    }
                                }
                            }
                            // If the second node of the edge is the current node (nextNode)
                            else if (floorChangeEdge.getNodeTwo().equals(nextNode)) {
                                // Get the other node as the possible new node
                                BuildingMapNode newFloorNode = floorChangeEdge.getNodeTwo();
                                // If it is the same type as the current node (stair/lift)
                                if (newFloorNode.getNodeType().equals(nextNode.getNodeType())) {
                                    // Calculate the difference between the floors
                                    int floorDiff = endNode.getFloor() - newFloorNode.getFloor();
                                    // If the difference is smaller than the current smallest distance
                                    if (floorDiff > 0 && floorDiff > diffOfClosestToCurrentFloor) {
                                        diffOfClosestToCurrentFloor = floorDiff;
                                        closestRelatedNode = newFloorNode;
                                    }
                                }
                            }
                        }

                        // Get the directions from the new node to the destination
                        return getBuildingMapNodesFromNewNode(startNode, endNode, visited, solution, closestRelatedNode);
                    }
                    // If the node types are incompatible go to the next iteration of the for loop
                    else if ((nextNode.getNodeType().equals(BuildingMapNodeType.LIFT) && !useLift) || (nextNode.getNodeType().equals(BuildingMapNodeType.STAIRS) && useLift)) {
                        continue;
                    } else {
                        return getBuildingMapNodesFromNewNode(startNode, endNode, visited, solution, nextNode);

                    }
                }
            }
        }

        if (nextNode == null) {
            return null;
        } else {
            return solution;
        }
    }

    /**
     * Given a new node, get the directions from it to the destination
     *
     * @param startNode The new node
     * @param endNode   The destination node
     * @param visited   The list of visited nodes
     * @param solution  The possible solution
     * @param nextNode
     * @return The solution in backwards order (if it exists)
     */
    private List<BuildingMapNode> getBuildingMapNodesFromNewNode(BuildingMapNode startNode, BuildingMapNode endNode, List<BuildingMapNode> visited, List<BuildingMapNode> solution, BuildingMapNode nextNode) {
        // Get the directions from the new node to the destination
        List<BuildingMapNode> extendedSolution = generateDirectionsBetweenNodes(nextNode, endNode, visited, solution);
        // If the solution exists
        if (extendedSolution != null) {
            extendedSolution.add(startNode);
        }
        return extendedSolution;
    }
}