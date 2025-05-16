package src;

import java.util.*;

/**
 * Uniform Cost Search (UCS) algorithm implementation for Rush Hour puzzle
 * UCS expands nodes in order of their path cost from the start
 */
public class UCS extends SearchAlgorithm {

    @Override
    public List<RushHourGame> solve(RushHourGame initial) {
        resetCounters();
        long startTime = System.currentTimeMillis();

        // Priority queue ordered by g value (path cost)
        PriorityQueue<SearchNode> frontier = new PriorityQueue<>(new Comparator<SearchNode>() {
            @Override
            public int compare(SearchNode n1, SearchNode n2) {
                if (n1.g != n2.g) {
                    return Integer.compare(n1.g, n2.g);
                }
                // Tie-breaking: prefer older nodes (FIFO)
                return 0;
            }
        });

        Set<RushHourGame> explored = new HashSet<>();

        // Initialize with starting state
        frontier.add(new SearchNode(initial, null, 0, 0, "Initial"));

        while (!frontier.isEmpty()) {
            SearchNode node = frontier.poll();
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
                if (!explored.contains(nextState)) {
                    // Calculate new cost (each move has cost 1)
                    int newCost = node.g + 1;

                    // Check if state is already in frontier
                    SearchNode existingNode = findInFrontier(frontier, nextState);

                    if (existingNode == null) {
                        // Add new node to frontier
                        String action = getAction(node.state, nextState);
                        frontier.add(new SearchNode(nextState, node, newCost, 0, action));
                    } else if (newCost < existingNode.g) {
                        // Found better path to existing node
                        frontier.remove(existingNode);
                        String action = getAction(node.state, nextState);
                        frontier.add(new SearchNode(nextState, node, newCost, 0, action));
                    }
                }
            }
        }

        executionTime = System.currentTimeMillis() - startTime;
        return null; // No solution found
    }

    @Override
    public String getAlgorithmName() {
        return "Uniform Cost Search (UCS)";
    }

    @Override
    public String getAlgorithmDescription() {
        return "UCS explores nodes in order of their path cost from the start node. " +
                "It guarantees finding the optimal solution (minimum number of moves) " +
                "because each move has the same cost.";
    }

    /**
     * Override findInFrontier to be more efficient for UCS
     * Since we need to check g values, we implement it specifically
     */
    @Override
    protected SearchNode findInFrontier(PriorityQueue<SearchNode> frontier, RushHourGame state) {
        for (SearchNode n : frontier) {
            if (n.state.equals(state)) {
                return n;
            }
        }
        return null;
    }

    /**
     * Get statistics for this UCS run
     * @return Map containing various statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("algorithm", getAlgorithmName());
        stats.put("nodes_visited", nodesVisited);
        stats.put("execution_time", executionTime);
        stats.put("solution_length", solution != null ? solution.size() - 1 : -1);
        stats.put("optimal", true); // UCS always finds optimal solution if one exists
        return stats;
    }

    /**
     * Print detailed information about the UCS search
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
            return;
        }

        System.out.println("Solution found!");
        System.out.println("Nodes visited: " + nodesVisited);
        System.out.println("Execution time: " + executionTime + " ms");
        System.out.println("Solution length: " + (solution.size() - 1) + " moves");
        System.out.println("Status: OPTIMAL (UCS guarantees optimal solution)");
        System.out.println();

        // Print solution path
        System.out.println("Papan Awal");
        printColoredBoard(solution.get(0), null);
        System.out.println();

        // Print each move
        for (int i = 1; i < solution.size(); i++) {
            System.out.println("Gerakan " + i + ": " + solutionActions.get(i - 1));
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
     * Compare UCS with BFS behavior
     * @return true if UCS behaves identically to BFS for this domain
     */
    public boolean isEquivalentToBFS() {
        // In Rush Hour, all moves have the same cost (1), so UCS will explore
        // nodes in the same order as BFS (breadth-first order)
        return true;
    }
}