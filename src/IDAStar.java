package src;

import java.util.*;

/**
 * Iterative Deepening A* (IDA*) algorithm implementation for Rush Hour puzzle
 * IDA* combines the space efficiency of iterative deepening with the guidance of A*
 */
public class IDAStar extends SearchAlgorithm {
    private int heuristicType;
    private int threshold;
    private int nextThreshold;

    // Heuristic types
    public static final int HEURISTIC_MANHATTAN = 1;
    public static final int HEURISTIC_BLOCKING_PIECES = 2;

    /**
     * Constructor for IDA*
     * @param heuristicType Type of heuristic to use (1 or 2)
     */
    public IDAStar(int heuristicType) {
        this.heuristicType = heuristicType;
    }

    @Override
    public List<RushHourGame> solve(RushHourGame initial) {
        resetCounters();
        long startTime = System.currentTimeMillis();

        // Initialize threshold with initial heuristic value
        threshold = calculateHeuristic(initial);

        while (threshold != Integer.MAX_VALUE) {
            nextThreshold = Integer.MAX_VALUE;
            SearchNode result = depthLimitedSearch(initial, 0, new ArrayList<>());

            if (result != null) {
                // Solution found
                executionTime = System.currentTimeMillis() - startTime;
                solution = buildSolution(result);
                return solution;
            }

            threshold = nextThreshold;
        }

        executionTime = System.currentTimeMillis() - startTime;
        return null; // No solution found
    }

    /**
     * Depth-limited search with f-value threshold
     * @param state Current game state
     * @param g Current path cost
     * @param path Current path (for cycle detection)
     * @return SearchNode if solution found, null otherwise
     */
    private SearchNode depthLimitedSearch(RushHourGame state, int g, List<RushHourGame> path) {
        nodesVisited++;

        int h = calculateHeuristic(state);
        int f = g + h;

        // If f exceeds threshold, update next threshold and prune
        if (f > threshold) {
            nextThreshold = Math.min(nextThreshold, f);
            return null;
        }

        // Check if goal reached
        if (state.isGoalState()) {
            // Create solution node
            SearchNode solutionNode = new SearchNode(state, null, g, h, "Goal");

            // Build parent nodes for solution path
            SearchNode current = solutionNode;
            for (int i = path.size() - 1; i >= 0; i--) {
                RushHourGame prevState = path.get(i);
                String action = i > 0 ? getAction(path.get(i - 1), prevState) : "Initial";
                SearchNode parent = new SearchNode(prevState, null, i, calculateHeuristic(prevState), action);
                current.parent = parent;
                current = parent;
            }

            return solutionNode;
        }

        // Expand current node
        List<RushHourGame> nextStates = state.getNextStates();

        // Sort by f-value for better pruning (optional optimization)
        nextStates.sort((s1, s2) -> {
            int f1 = (g + 1) + calculateHeuristic(s1);
            int f2 = (g + 1) + calculateHeuristic(s2);
            return Integer.compare(f1, f2);
        });

        for (RushHourGame nextState : nextStates) {
            // Check for cycles in path
            if (!path.contains(nextState)) {
                path.add(state); // Add current state to path
                SearchNode result = depthLimitedSearch(nextState, g + 1, path);
                path.remove(path.size() - 1); // Remove state from path

                if (result != null) {
                    // Update parent link
                    if (result.parent == null && result.state.equals(nextState)) {
                        String action = getAction(state, nextState);
                        result.parent = new SearchNode(state, null, g, h, action);
                    }
                    return result;
                }
            }
        }

        return null; // No solution found at this threshold
    }

    /**
     * Calculate heuristic value based on selected heuristic type
     * @param state Game state to evaluate
     * @return Heuristic value
     */
    private int calculateHeuristic(RushHourGame state) {
        switch (heuristicType) {
            case HEURISTIC_MANHATTAN:
                return state.getHeuristic1();
            case HEURISTIC_BLOCKING_PIECES:
                return state.getHeuristic2();
            default:
                return state.getHeuristic1();
        }
    }

    @Override
    public String getAlgorithmName() {
        return "Iterative Deepening A* (IDA*)";
    }

    @Override
    public String getAlgorithmDescription() {
        String heuristicName = getHeuristicName();
        return "IDA* combines iterative deepening with A* heuristic guidance. " +
                "Uses minimal memory (space complexity O(d)) while maintaining A* optimality. " +
                "Current heuristic: " + heuristicName + ".";
    }

    /**
     * Get the name of the currently used heuristic
     * @return Heuristic name
     */
    public String getHeuristicName() {
        switch (heuristicType) {
            case HEURISTIC_MANHATTAN:
                return "Manhattan Distance (H1)";
            case HEURISTIC_BLOCKING_PIECES:
                return "Blocking Pieces + Distance (H2)";
            default:
                return "Unknown";
        }
    }

    /**
     * Get the heuristic type being used
     * @return Heuristic type (1 or 2)
     */
    public int getHeuristicType() {
        return heuristicType;
    }

    /**
     * Set the heuristic type
     * @param heuristicType New heuristic type (1 or 2)
     */
    public void setHeuristicType(int heuristicType) {
        this.heuristicType = heuristicType;
    }

    /**
     * Check if the current heuristic is admissible
     * @return true if heuristic is admissible
     */
    public boolean isHeuristicAdmissible() {
        switch (heuristicType) {
            case HEURISTIC_MANHATTAN:
                return true; // Manhattan distance is admissible
            case HEURISTIC_BLOCKING_PIECES:
                return false; // May overestimate
            default:
                return false;
        }
    }

    /**
     * Get statistics for this IDA* run
     * @return Map containing various statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("algorithm", getAlgorithmName());
        stats.put("heuristic", getHeuristicName());
        stats.put("heuristic_type", heuristicType);
        stats.put("nodes_visited", nodesVisited);
        stats.put("execution_time", executionTime);
        stats.put("solution_length", solution != null ? solution.size() - 1 : -1);
        stats.put("optimal", isHeuristicAdmissible());
        stats.put("space_complexity", "O(d)"); // Linear in solution depth
        return stats;
    }

    /**
     * Print detailed information about the IDA* search
     */
    @Override
    public void printSolution() {
        System.out.println("=== " + getAlgorithmName() + " Results ===");
        System.out.println(getAlgorithmDescription());
        System.out.println();

        if (solution == null || solution.isEmpty()) {
            System.out.println("No solution found!");
            System.out.println("Nodes visited: " + nodesVisited);
            System.out.println("Execution time: " + executionTime + " ms");
            System.out.println("Heuristic used: " + getHeuristicName());
            System.out.println("Heuristic admissible: " + isHeuristicAdmissible());
            return;
        }

        System.out.println("Solution found!");
        System.out.println("Nodes visited: " + nodesVisited);
        System.out.println("Execution time: " + executionTime + " ms");
        System.out.println("Solution length: " + (solution.size() - 1) + " moves");
        System.out.println("Heuristic used: " + getHeuristicName());
        System.out.println("Heuristic admissible: " + isHeuristicAdmissible());
        System.out.println("Status: " + (isHeuristicAdmissible() ? "OPTIMAL" : "NON-OPTIMAL"));
        System.out.println("Space Complexity: O(d) - Linear in solution depth");
        System.out.println();

        // Print solution path with threshold information
        System.out.println("Papan Awal");
        printColoredBoard(solution.get(0), null);
        int h0 = calculateHeuristic(solution.get(0));
        System.out.println("(g=0, h=" + h0 + ", f=" + h0 + ")");
        System.out.println();

        // Print each move
        for (int i = 1; i < solution.size(); i++) {
            int g = i;
            int h = calculateHeuristic(solution.get(i));
            int f = g + h;
            System.out.println("Gerakan " + i + ": " + solutionActions.get(i - 1) +
                    " (g=" + g + ", h=" + h + ", f=" + f + ")");
            printColoredBoard(solution.get(i), solutionActions.get(i - 1));
            System.out.println();
        }
    }

    /**
     * Print board with colored output for primary piece, exit, and moved piece
     * @param game The game state to print
     * @param action The action that was taken (for highlighting moved piece)
     */
    private void printColoredBoard(RushHourGame game, String action) {
        char[][] board = game.getBoard();
        int rows = game.getRows();
        int cols = game.getCols();

        // ANSI color codes
        String RESET = "\u001B[0m";
        String RED = "\u001B[31m";      // Primary piece
        String GREEN = "\u001B[32m";    // Exit
        String YELLOW = "\u001B[33m";   // Moved piece
        String BLUE = "\u001B[34m";     // Other pieces

        char movedPiece = '.';
        if (action != null && !action.equals("Initial")) {
            movedPiece = action.charAt(0);
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char c = board[i][j];

                if (c == game.getPrimaryPiece()) {
                    System.out.print(RED + c + RESET);
                } else if (c == game.getExit()) {
                    System.out.print(GREEN + c + RESET);
                } else if (c == movedPiece && c != '.') {
                    System.out.print(YELLOW + c + RESET);
                } else if (c != '.' && c != game.getExit()) {
                    System.out.print(BLUE + c + RESET);
                } else {
                    System.out.print(c);
                }
            }
            System.out.println();
        }
    }

    /**
     * Analyze IDA* performance and characteristics
     * @return Analysis of IDA* performance
     */
    public String analyzeAlgorithm() {
        if (solution == null) {
            return "No solution found - cannot analyze algorithm.";
        }

        StringBuilder analysis = new StringBuilder();
        analysis.append("IDA* Analysis with ").append(getHeuristicName()).append(":\n");

        // Calculate memory usage advantage
        int solutionDepth = solution.size() - 1;
        analysis.append("- Solution depth: ").append(solutionDepth).append("\n");
        analysis.append("- Space complexity: O(").append(solutionDepth).append(") vs O(b^d) for A*\n");

        // Analyze threshold growth
        analysis.append("- Final threshold: ").append(threshold).append("\n");

        // Check if heuristic is helpful
        int initialH = calculateHeuristic(solution.get(0));
        if (initialH > 0) {
            analysis.append("- Heuristic provides guidance (initial h = ").append(initialH).append(")\n");
        } else {
            analysis.append("- Heuristic provides minimal guidance\n");
        }

        // Efficiency comparison
        analysis.append("- Nodes visited: ").append(nodesVisited).append("\n");
        analysis.append("- Average nodes per solution step: ").append(String.format("%.2f", (double) nodesVisited / solutionDepth)).append("\n");

        // Admissibility check
        analysis.append("- Heuristic admissible: ").append(isHeuristicAdmissible() ? "Yes" : "No").append("\n");
        analysis.append("- Solution optimal: ").append(isHeuristicAdmissible() ? "Yes" : "Possibly No").append("\n");

        return analysis.toString();
    }

    /**
     * Compare IDA* with A* and other algorithms
     * @return Comparative analysis
     */
    public String compareWithOtherAlgorithms() {
        StringBuilder comparison = new StringBuilder();
        comparison.append("IDA* vs Other Algorithms:\n");

        comparison.append("\nIDA* vs A*:\n");
        comparison.append("- Space: IDA* uses O(d), A* uses O(b^d)\n");
        comparison.append("- Time: IDA* may revisit nodes, potentially slower\n");
        comparison.append("- Optimality: Both optimal with admissible heuristic\n");

        comparison.append("\nIDA* vs UCS:\n");
        if (isHeuristicAdmissible()) {
            comparison.append("- IDA* should be faster (guided by heuristic)\n");
            comparison.append("- Both find optimal solution\n");
        } else {
            comparison.append("- UCS guarantees optimality, IDA* may not\n");
        }
        comparison.append("- IDA* uses less memory than UCS\n");

        comparison.append("\nIDA* vs GBFS:\n");
        comparison.append("- IDA* guarantees optimality (if heuristic admissible)\n");
        comparison.append("- GBFS is typically faster but non-optimal\n");
        comparison.append("- IDA* uses less memory than GBFS\n");

        comparison.append("\nIDA* Advantages:\n");
        comparison.append("- Minimal memory usage\n");
        comparison.append("- Optimal solution (with admissible heuristic)\n");
        comparison.append("- No need to store explored states\n");

        comparison.append("\nIDA* Disadvantages:\n");
        comparison.append("- May revisit the same states multiple times\n");
        comparison.append("- Can be slower than A* for easier problems\n");
        comparison.append("- Performance depends heavily on heuristic quality\n");

        return comparison.toString();
    }

    /**
     * Guarantees optimal solution?
     * @return true if heuristic is admissible
     */
    public boolean guaranteesOptimalSolution() {
        return isHeuristicAdmissible();
    }
}