package src;

import java.util.*;

/**
 * Abstract base class for all search algorithms
 */
public abstract class SearchAlgorithm {
    protected int nodesVisited;
    protected long executionTime;
    protected List<RushHourGame> solution;
    protected List<String> solutionActions;

    /**
     * Solve the Rush Hour puzzle using the specific algorithm
     * @param initial Initial game state
     * @return List of game states representing the solution path, null if no solution
     */
    public abstract List<RushHourGame> solve(RushHourGame initial);

    /**
     * Get the number of nodes visited during search
     * @return Number of nodes visited
     */
    public int getNodesVisited() {
        return nodesVisited;
    }

    /**
     * Get the execution time of the search
     * @return Execution time in milliseconds
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * Get the solution path
     * @return List of game states representing solution, null if no solution
     */
    public List<RushHourGame> getSolution() {
        return solution;
    }

    /**
     * Get the actions taken in the solution
     * @return List of action strings
     */
    public List<String> getSolutionActions() {
        return solutionActions;
    }

    /**
     * Reset counters before starting a new search
     */
    protected void resetCounters() {
        nodesVisited = 0;
        executionTime = 0;
        solution = null;
        solutionActions = null;
    }

    /**
     * Build solution path from goal node
     * @param goalNode The goal node containing the solution
     * @return List of game states representing the solution path
     */
    protected List<RushHourGame> buildSolution(SearchNode goalNode) {
        List<RushHourGame> path = new ArrayList<>();
        List<String> actions = new ArrayList<>();
        SearchNode current = goalNode;

        while (current != null) {
            path.add(0, current.state);
            if (current.action != null && !current.action.equals("Initial")) {
                actions.add(0, current.action);
            }
            current = current.parent;
        }

        solutionActions = actions;
        return path;
    }

    /**
     * Determine what action was taken to get from one state to another
     * @param from Previous game state
     * @param to Current game state
     * @return String describing the action taken
     */
    protected String getAction(RushHourGame from, RushHourGame to) {
    // Find which piece moved
    for (char piece : from.getPieces().keySet()) {
        List<int[]> fromPos = from.getPieces().get(piece);
        List<int[]> toPos = to.getPieces().get(piece);
        
        // Handle case where piece might have exited (toPos could be null or different size)
        if (toPos == null) {
            // Piece has completely exited the board (only primary piece can do this)
            if (piece == 'P') {
                // Determine exit direction based on exit position
                if (to.getExitCol() == to.getCols() && to.getExitRow() == from.getPrimaryRow()) {
                    return piece + "-kanan"; // Exited to the right
                } else if (to.getExitRow() == to.getRows() && to.getExitCol() == from.getPrimaryCol()) {
                    return piece + "-bawah"; // Exited down
                }
                // Add more exit directions if needed
            }
            continue;
        }
        
        if (fromPos.size() != toPos.size()) {
            // Size difference might indicate partial exit or other issue
            if (piece == 'P' && toPos.size() < fromPos.size()) {
                // Primary piece is partially exiting
                // Determine direction based on remaining positions
                if (fromPos.get(0)[0] == toPos.get(0)[0]) {
                    // Horizontal movement
                    return from.getExitCol() > from.getPrimaryCol() ? piece + "-kanan" : piece + "-kiri";
                } else {
                    // Vertical movement  
                    return from.getExitRow() > from.getPrimaryRow() ? piece + "-bawah" : piece + "-atas";
                }
            }
            continue;
        }

        boolean moved = false;
        for (int i = 0; i < fromPos.size(); i++) {
            if (fromPos.get(i)[0] != toPos.get(i)[0] || fromPos.get(i)[1] != toPos.get(i)[1]) {
                moved = true;
                break;
            }
        }

        if (moved) {
            // Determine direction of movement
            int[] oldPos = fromPos.get(0);
            int[] newPos = toPos.get(0);

            if (oldPos[0] != newPos[0]) {
                // Vertical movement
                if (newPos[0] > oldPos[0]) {
                    return piece + "-bawah";
                } else {
                    return piece + "-atas";
                }
            } else {
                // Horizontal movement
                if (newPos[1] > oldPos[1]) {
                    return piece + "-kanan";
                } else {
                    return piece + "-kiri";
                }
            }
        }
    }

    return "Unknown";
}

    /**
     * Check if a state exists in frontier with higher cost
     * @param frontier The priority queue representing frontier
     * @param state The state to check
     * @param newCost The new cost for this state
     * @return The existing node if found and has higher cost, null otherwise
     */
    protected SearchNode findInFrontierWithHigherCost(PriorityQueue<SearchNode> frontier,
                                                      RushHourGame state, int newCost) {
        for (SearchNode n : frontier) {
            if (n.state.equals(state) && n.g > newCost) {
                return n;
            }
        }
        return null;
    }

    /**
     * Check if a state exists in frontier
     * @param frontier The priority queue representing frontier
     * @param state The state to check
     * @return The existing node if found, null otherwise
     */
    protected SearchNode findInFrontier(PriorityQueue<SearchNode> frontier, RushHourGame state) {
        for (SearchNode n : frontier) {
            if (n.state.equals(state)) {
                return n;
            }
        }
        return null;
    }

    /**
     * Print the solution in the required format
     */
    public void printSolution() {
        if (solution == null || solution.isEmpty()) {
            System.out.println("No solution found!");
            return;
        }

        System.out.println("Solution found!");
        System.out.println("Nodes visited: " + nodesVisited);
        System.out.println("Execution time: " + executionTime + " ms");
        System.out.println("Solution length: " + (solution.size() - 1) + " moves");
        System.out.println();

        // Print initial state
        System.out.println("Papan Awal");
        System.out.println(solution.get(0));

        // Print each move
        for (int i = 1; i < solution.size(); i++) {
            System.out.println("Gerakan " + i + ": " + solutionActions.get(i - 1));
            System.out.println(solution.get(i));
        }
    }

    /**
     * Get algorithm name
     * @return Name of the algorithm
     */
    public abstract String getAlgorithmName();

    /**
     * Get algorithm description
     * @return Description of the algorithm
     */
    public abstract String getAlgorithmDescription();
}

/**
 * Node class used in search algorithms
 */
class SearchNode implements Comparable<SearchNode> {
    public RushHourGame state;
    public SearchNode parent;
    public int g; // Cost from start
    public int h; // Heuristic value
    public int f; // Total cost (g + h)
    public String action; // Action taken to reach this state

    /**
     * Constructor for SearchNode
     * @param state Game state
     * @param parent Parent node
     * @param g Cost from start
     * @param h Heuristic value
     * @param action Action taken to reach this state
     */
    public SearchNode(RushHourGame state, SearchNode parent, int g, int h, String action) {
        this.state = state;
        this.parent = parent;
        this.g = g;
        this.h = h;
        this.f = g + h;
        this.action = action;
    }

    /**
     * Compare nodes for priority queue ordering
     * Default comparison is by f value (for A*)
     * Override in specific algorithm implementations if needed
     */
    @Override
    public int compareTo(SearchNode other) {
        if (this.f != other.f) {
            return Integer.compare(this.f, other.f);
        }
        // Tie-breaking: prefer node with lower h value
        return Integer.compare(this.h, other.h);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SearchNode)) return false;
        SearchNode other = (SearchNode) obj;
        return state.equals(other.state);
    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }
}