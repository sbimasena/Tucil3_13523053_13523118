package src;

import java.util.*;

public class AStar extends SearchAlgorithm {
    private int heuristicType;

    // Heuristic types
    public static final int HEURISTIC_MANHATTAN = 1;
    public static final int HEURISTIC_BLOCKING_PIECES = 2;

 
    public AStar(int heuristicType) {
        this.heuristicType = heuristicType;
    }

    @Override
    public List<RushHourGame> solve(RushHourGame initial) {
        resetCounters();
        long startTime = System.currentTimeMillis();

        // Priority queue ordered by f = g + h
        PriorityQueue<SearchNode> frontier = new PriorityQueue<>(new Comparator<SearchNode>() {
            @Override
            public int compare(SearchNode n1, SearchNode n2) {
                if (n1.f != n2.f) {
                    return Integer.compare(n1.f, n2.f);
                }
                // Tie-breaking: prefer node with lower h value
                if (n1.h != n2.h) {
                    return Integer.compare(n1.h, n2.h);
                }
                // Second tie-breaking: prefer newer nodes
                return 0;
            }
        });

        Set<RushHourGame> explored = new HashSet<>();
        Map<RushHourGame, Integer> frontierCosts = new HashMap<>(); // Track best g values in frontier

        // Calculate initial heuristic
        int h = calculateHeuristic(initial);
        SearchNode initialNode = new SearchNode(initial, null, 0, h, "Initial");
        frontier.add(initialNode);
        frontierCosts.put(initial, 0);

        while (!frontier.isEmpty()) {
            SearchNode node = frontier.poll();

            // Remove from frontier costs when popped
            frontierCosts.remove(node.state);
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
                int newG = node.g + 1; // Each move has cost 1

                if (!explored.contains(nextState)) {
                    Integer frontierG = frontierCosts.get(nextState);

                    if (frontierG == null) {
                        // New state not in frontier
                        h = calculateHeuristic(nextState);
                        String action = getAction(node.state, nextState);
                        SearchNode newNode = new SearchNode(nextState, node, newG, h, action);
                        frontier.add(newNode);
                        frontierCosts.put(nextState, newG);
                    } else if (newG < frontierG) {
                        // Found better path to state in frontier
                        // Remove old node (we need to do this manually in Java)
                        removeFromFrontier(frontier, nextState);
                        frontierCosts.remove(nextState);

                        // Add new better node
                        h = calculateHeuristic(nextState);
                        String action = getAction(node.state, nextState);
                        SearchNode newNode = new SearchNode(nextState, node, newG, h, action);
                        frontier.add(newNode);
                        frontierCosts.put(nextState, newG);
                    }
                }
            }
        }

        executionTime = System.currentTimeMillis() - startTime;
        return null; // No solution found
    }

    private void removeFromFrontier(PriorityQueue<SearchNode> frontier, RushHourGame state) {
        Iterator<SearchNode> iterator = frontier.iterator();
        while (iterator.hasNext()) {
            SearchNode node = iterator.next();
            if (node.state.equals(state)) {
                iterator.remove();
                break;
            }
        }
    }

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
        return "A* Search";
    }

    @Override
    public String getAlgorithmDescription() {
        String heuristicName = getHeuristicName();
        return "A* combines path cost (g) and heuristic (h) to find optimal solution efficiently. " +
                "Current heuristic: " + heuristicName + ". " +
                "Guarantees optimal solution when heuristic is admissible.";
    }

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

    public int getHeuristicType() {
        return heuristicType;
    }

    public void setHeuristicType(int heuristicType) {
        this.heuristicType = heuristicType;
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("algorithm", getAlgorithmName());
        stats.put("heuristic", getHeuristicName());
        stats.put("heuristic_type", heuristicType);
        stats.put("nodes_visited", nodesVisited);
        stats.put("execution_time", executionTime);
        stats.put("solution_length", solution != null ? solution.size() - 1 : -1);
        stats.put("optimal", isHeuristicAdmissible()); // Optimal if heuristic is admissible
        return stats;
    }

    public boolean isHeuristicAdmissible() {
        switch (heuristicType) {
            case HEURISTIC_MANHATTAN:
                return true; // Manhattan distance is admissible for Rush Hour
            case HEURISTIC_BLOCKING_PIECES:
                return false; // May overestimate, making it non-admissible
            default:
                return false;
        }
    }

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
        System.out.println();

        // Print solution path with f, g, h values
        System.out.println("Papan Awal");
        printColoredBoard(solution.get(0), null);
        int h0 = calculateHeuristic(solution.get(0));
        System.out.println("(g=0, h=" + h0 + ", f=" + h0 + ")");
        System.out.println();

        // Print each move with f, g, h values
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

    public String analyzeAlgorithm() {
        if (solution == null) {
            return "No solution found - cannot analyze algorithm.";
        }

        StringBuilder analysis = new StringBuilder();
        analysis.append("A* Analysis with ").append(getHeuristicName()).append(":\n");

        // Calculate f, g, h values for each step
        List<Integer> gValues = new ArrayList<>();
        List<Integer> hValues = new ArrayList<>();
        List<Integer> fValues = new ArrayList<>();

        for (int i = 0; i < solution.size(); i++) {
            int g = i;
            int h = calculateHeuristic(solution.get(i));
            int f = g + h;
            gValues.add(g);
            hValues.add(h);
            fValues.add(f);
        }

        // Analyze heuristic behavior
        analysis.append("- Initial f=").append(fValues.get(0)).append(" (g=0, h=").append(hValues.get(0)).append(")\n");
        analysis.append("- Final f=").append(fValues.get(fValues.size() - 1))
                .append(" (g=").append(gValues.get(gValues.size() - 1))
                .append(", h=").append(hValues.get(hValues.size() - 1)).append(")\n");

        // Check if heuristic is consistent
        boolean consistent = true;
        for (int i = 1; i < solution.size(); i++) {
            if (hValues.get(i - 1) > hValues.get(i) + 1) {
                consistent = false;
                break;
            }
        }

        analysis.append("- Heuristic consistent: ").append(consistent ? "Yes" : "No").append("\n");
        analysis.append("- Heuristic admissible: ").append(isHeuristicAdmissible() ? "Yes" : "No").append("\n");
        analysis.append("- Solution optimal: ").append(isHeuristicAdmissible() ? "Yes" : "Possibly No").append("\n");

        // Efficiency analysis
        double avgBranchingFactor = (double) nodesVisited / solution.size();
        analysis.append("- Average effective branching factor: ").append(String.format("%.2f", avgBranchingFactor)).append("\n");

        return analysis.toString();
    }

    public String compareWithUCS() {
        StringBuilder comparison = new StringBuilder();
        comparison.append("A* vs UCS Theoretical Comparison:\n");

        if (isHeuristicAdmissible()) {
            comparison.append("- A* should be more efficient than UCS (fewer nodes expanded)\n");
            comparison.append("- Both find optimal solution\n");
            comparison.append("- A* uses heuristic to guide search toward goal\n");
        } else {
            comparison.append("- A* may be faster but might not find optimal solution\n");
            comparison.append("- UCS guarantees optimal solution\n");
            comparison.append("- Non-admissible heuristic can mislead A*\n");
        }

        comparison.append("- A* memory usage similar to UCS\n");
        comparison.append("- A* has overhead of heuristic calculation\n");

        return comparison.toString();
    }

    public boolean guaranteesOptimalSolution() {
        return isHeuristicAdmissible();
    }
}