package src;

import java.util.*;

/**
 * Greedy Best First Search (GBFS) algorithm implementation for Rush Hour puzzle
 * GBFS uses heuristic to guide search toward the goal
 */
public class GBFS extends SearchAlgorithm {
    private int heuristicType;

    // Heuristic types
    public static final int HEURISTIC_MANHATTAN = 1;
    public static final int HEURISTIC_BLOCKING_PIECES = 2;

    /**
     * Constructor for GBFS
     * @param heuristicType Type of heuristic to use (1 or 2)
     */
    public GBFS(int heuristicType) {
        this.heuristicType = heuristicType;
    }

    @Override
    public List<RushHourGame> solve(RushHourGame initial) {
        resetCounters();
        long startTime = System.currentTimeMillis();

        // Priority queue ordered by heuristic value (h only)
        PriorityQueue<SearchNode> frontier = new PriorityQueue<>(new Comparator<SearchNode>() {
            @Override
            public int compare(SearchNode n1, SearchNode n2) {
                if (n1.h != n2.h) {
                    return Integer.compare(n1.h, n2.h);
                }
                // Tie-breaking: prefer newer nodes (LIFO for better performance)
                return 0;
            }
        });

        Set<RushHourGame> explored = new HashSet<>();
        Set<RushHourGame> frontierStates = new HashSet<>(); // Track states in frontier

        // Calculate initial heuristic
        int h = calculateHeuristic(initial);
        frontier.add(new SearchNode(initial, null, 0, h, "Initial"));
        frontierStates.add(initial);

        while (!frontier.isEmpty()) {
            SearchNode node = frontier.poll();
            frontierStates.remove(node.state);
            nodesVisited++;

            // Check if goal state reached
            if (node.state.isGoalState()) {
                executionTime = System.currentTimeMillis() - startTime;
                solution = buildSolution(node);
                return solution;
            }

            explored.add(node.state);

            // Expand current node
            for (RushHourGame nextState : node.state.getNextStates()) {
                if (!explored.contains(nextState) && !frontierStates.contains(nextState)) {
                    // Calculate heuristic for new state
                    h = calculateHeuristic(nextState);
                    String action = getAction(node.state, nextState);

                    // GBFS doesn't track g value, but we set it for consistency
                    frontier.add(new SearchNode(nextState, node, node.g + 1, h, action));
                    frontierStates.add(nextState);
                }
            }
        }

        executionTime = System.currentTimeMillis() - startTime;
        return null; // No solution found
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
        return "Greedy Best First Search (GBFS)";
    }

    @Override
    public String getAlgorithmDescription() {
        String heuristicName = getHeuristicName();
        return "GBFS uses only the heuristic function to guide search toward the goal. " +
                "Current heuristic: " + heuristicName + ". " +
                "It's fast but doesn't guarantee optimal solution.";
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
     * Get statistics for this GBFS run
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
        stats.put("optimal", false); // GBFS doesn't guarantee optimal solution
        return stats;
    }

    /**
     * Print detailed information about the GBFS search
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
            return;
        }

        System.out.println("Solution found!");
        System.out.println("Nodes visited: " + nodesVisited);
        System.out.println("Execution time: " + executionTime + " ms");
        System.out.println("Solution length: " + (solution.size() - 1) + " moves");
        System.out.println("Heuristic used: " + getHeuristicName());
        System.out.println("Status: NON-OPTIMAL (GBFS doesn't guarantee optimal solution)");
        System.out.println();

        // Print solution path
        System.out.println("Papan Awal");
        printColoredBoard(solution.get(0), null);
        System.out.println();

        // Print each move with heuristic values
        for (int i = 1; i < solution.size(); i++) {
            int hValue = calculateHeuristic(solution.get(i));
            System.out.println("Gerakan " + i + ": " + solutionActions.get(i - 1) +
                    " (h=" + hValue + ")");
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
     * Analyze heuristic effectiveness
     * @return Analysis of heuristic performance
     */
    public String analyzeHeuristic() {
        if (solution == null) {
            return "No solution found - cannot analyze heuristic.";
        }

        StringBuilder analysis = new StringBuilder();
        analysis.append("Heuristic Analysis for ").append(getHeuristicName()).append(":\n");

        // Calculate average heuristic value throughout solution
        int totalH = 0;
        for (RushHourGame state : solution) {
            totalH += calculateHeuristic(state);
        }
        double avgH = (double) totalH / solution.size();

        analysis.append("- Average heuristic value: ").append(String.format("%.2f", avgH)).append("\n");
        analysis.append("- Initial heuristic: ").append(calculateHeuristic(solution.get(0))).append("\n");
        analysis.append("- Final heuristic: ").append(calculateHeuristic(solution.get(solution.size() - 1))).append("\n");

        // Check if heuristic generally decreases
        int decreases = 0;
        int increases = 0;
        for (int i = 1; i < solution.size(); i++) {
            int prevH = calculateHeuristic(solution.get(i - 1));
            int currH = calculateHeuristic(solution.get(i));
            if (currH < prevH) decreases++;
            else if (currH > prevH) increases++;
        }

        analysis.append("- Heuristic decreases: ").append(decreases).append(" times\n");
        analysis.append("- Heuristic increases: ").append(increases).append(" times\n");

        if (decreases > increases) {
            analysis.append("- The heuristic generally guides toward the goal effectively.\n");
        } else {
            analysis.append("- The heuristic may not be very informative for this problem.\n");
        }

        return analysis.toString();
    }

    /**
     * Check if the heuristic is admissible
     * Note: This is a simplified check and may not be completely accurate
     * @return true if heuristic appears to be admissible
     */
    public boolean isHeuristicAdmissible() {
        // Heuristic 1 (Manhattan distance) is generally admissible for Rush Hour
        // Heuristic 2 might overestimate in some cases, making it non-admissible
        switch (heuristicType) {
            case HEURISTIC_MANHATTAN:
                return true; // Manhattan distance is admissible
            case HEURISTIC_BLOCKING_PIECES:
                return false; // May overestimate due to the blocking pieces factor
            default:
                return false;
        }
    }

    /**
     * Guarantees optimal solution?
     * @return false, as GBFS doesn't guarantee optimal solution
     */
    public boolean guaranteesOptimalSolution() {
        return false;
    }
}