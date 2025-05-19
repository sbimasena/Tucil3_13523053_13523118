package src;

import java.io.IOException;
import java.util.*;

public class RushHourSolver {
    private RushHourGame initialGame;
    private Map<String, SearchAlgorithm> algorithms;
    private Map<String, Map<String, Object>> results;

    public RushHourSolver() {
        this.algorithms = new HashMap<>();
        this.results = new HashMap<>();
        initializeAlgorithms();
    }

    private void initializeAlgorithms() {
        algorithms.put("UCS", new UCS());
        algorithms.put("GBFS", new GBFS(1)); // Default to heuristic 1
        algorithms.put("A*", new AStar(1));   // Default to heuristic 1
        algorithms.put("IDA*", new IDAStar(1)); // Default to heuristic 1
    }

    public static void main(String[] args) {
        RushHourSolver solver = new RushHourSolver();

        try {
            // Load test case
            String filename;
            if (args.length > 0) {
                filename = args[0];
            } else {
                filename = RushHourIO.getUserInput("Enter test case filename: ");
            }

            // Validate file exists
            while (!RushHourIO.isValidFile(filename)) {
                System.out.println("File not found or not readable: " + filename);
                filename = RushHourIO.getUserInput("Enter test case filename: ");
            }

            // Load game from file
            System.out.println("Loading test case from: " + filename);
            solver.loadGame(filename);

            // Interactive menu
            solver.runInteractiveMode();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadGame(String filename) throws IOException {
        this.initialGame = RushHourIO.loadGameFromFile(filename);
        System.out.println("Game loaded successfully!");
        System.out.println("Board size: " + initialGame.getRows() + "x" + initialGame.getCols());
        System.out.println("Number of pieces: " + initialGame.getPieces().size());

        // Display initial board
        System.out.println("\nInitial board:");
        System.out.println(initialGame);
    }

    private void runInteractiveMode() {
        Scanner scanner = new Scanner(System.in);
        boolean continueRunning = true;

        while (continueRunning) {
            System.out.println("\n=== Rush Hour Solver Menu ===");
            System.out.println("1. Run single algorithm");
            System.out.println("2. Run all algorithms (comparison)");
            System.out.println("3. Display current board");
            System.out.println("4. Load new test case");
            System.out.println("5. View previous results");
            System.out.println("6. Exit");
            System.out.print("Choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());

                switch (choice) {
                    case 1:
                        runSingleAlgorithm();
                        break;
                    case 2:
                        runAllAlgorithms();
                        break;
                    case 3:
                        displayCurrentBoard();
                        break;
                    case 4:
                        loadNewTestCase();
                        break;
                    case 5:
                        displayPreviousResults();
                        break;
                    case 6:
                        continueRunning = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter 1-6.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        System.out.println("Thank you for using Rush Hour Solver!");
    }

    private void runSingleAlgorithm() {
        if (initialGame == null) {
            System.out.println("No game loaded. Please load a test case first.");
            return;
        }

        // Get algorithm choice
        int algorithmChoice = RushHourIO.getAlgorithmChoice();
        SearchAlgorithm algorithm = selectAlgorithm(algorithmChoice);

        // Get heuristic choice if needed
        if (requiresHeuristic(algorithmChoice)) {
            int heuristicChoice = RushHourIO.getHeuristicChoice();
            setAlgorithmHeuristic(algorithm, heuristicChoice);
        }

        // Run algorithm
        System.out.println("\nRunning " + algorithm.getAlgorithmName() + "...");
        runAlgorithm(algorithm);

        // Display results
        displayResults(algorithm);

        // Save to file option
        saveResultsOption(algorithm);
    }

    private void runAllAlgorithms() {
        if (initialGame == null) {
            System.out.println("No game loaded. Please load a test case first.");
            return;
        }

        System.out.println("Running all algorithms...");

        // Run UCS
        System.out.println("\n1. Running UCS...");
        UCS ucs = new UCS();
        runAlgorithm(ucs);
        results.put("UCS", ucs.getStatistics());

        // Run both heuristics for GBFS
        System.out.println("\n2. Running GBFS with H1...");
        GBFS gbfs1 = new GBFS(1);
        runAlgorithm(gbfs1);
        results.put("GBFS-H1", gbfs1.getStatistics());

        System.out.println("\n3. Running GBFS with H2...");
        GBFS gbfs2 = new GBFS(2);
        runAlgorithm(gbfs2);
        results.put("GBFS-H2", gbfs2.getStatistics());

        // Run both heuristics for A*
        System.out.println("\n4. Running A* with H1...");
        AStar aStar1 = new AStar(1);
        runAlgorithm(aStar1);
        results.put("A*-H1", aStar1.getStatistics());

        System.out.println("\n5. Running A* with H2...");
        AStar aStar2 = new AStar(2);
        runAlgorithm(aStar2);
        results.put("A*-H2", aStar2.getStatistics());

        // Run both heuristics for IDA*
        System.out.println("\n6. Running IDA* with H1...");
        IDAStar idaStar1 = new IDAStar(1);
        runAlgorithm(idaStar1);
        results.put("IDA*-H1", idaStar1.getStatistics());

        System.out.println("\n7. Running IDA* with H2...");
        IDAStar idaStar2 = new IDAStar(2);
        runAlgorithm(idaStar2);
        results.put("IDA*-H2", idaStar2.getStatistics());

        // Display comparison
        RushHourIO.printComparisonTable(results);

        // Save comparison to file
        saveComparisonOption();
    }

    private void runAlgorithm(SearchAlgorithm algorithm) {
        long startTime = System.currentTimeMillis();

        // Create a copy of the initial game for the algorithm
        RushHourGame gameCopy = new RushHourGame(initialGame);

        // Solve the puzzle
        List<RushHourGame> solution = algorithm.solve(gameCopy);

        // Record statistics
        System.out.println("Algorithm: " + algorithm.getAlgorithmName());
        System.out.println("Nodes visited: " + algorithm.getNodesVisited());
        System.out.println("Execution time: " + algorithm.getExecutionTime() + " ms");

        if (solution != null && !solution.isEmpty()) {
            System.out.println("Solution found! Length: " + (solution.size() - 1) + " moves");
        } else {
            System.out.println("No solution found.");
        }
    }

    private void displayResults(SearchAlgorithm algorithm) {
        if (algorithm.getSolution() != null && !algorithm.getSolution().isEmpty()) {
            // Prepare solution and actions with final step
            List<RushHourGame> solutionWithExit = new ArrayList<>(algorithm.getSolution());
            List<String> actionsWithExit = new ArrayList<>(algorithm.getSolutionActions());
            RushHourGame finalState = new RushHourGame(solutionWithExit.get(solutionWithExit.size() - 1));
            finalState.removePrimaryPiece();
            solutionWithExit.add(finalState);
            actionsWithExit.add("EXIT");

            System.out.println("\n=== Detailed Solution ===");
            RushHourIO.writeSolutionToConsole(solutionWithExit, actionsWithExit, algorithm);

            // Additional analysis for specific algorithms
            if (algorithm instanceof AStar) {
                System.out.println("\n" + ((AStar) algorithm).analyzeAlgorithm());
            } else if (algorithm instanceof GBFS) {
                System.out.println("\n" + ((GBFS) algorithm).analyzeHeuristic());
            } else if (algorithm instanceof IDAStar) {
                System.out.println("\n" + ((IDAStar) algorithm).analyzeAlgorithm());
            }
        }
    }

    private void displayCurrentBoard() {
        if (initialGame == null) {
            System.out.println("No game loaded. Please load a test case first.");
            return;
        }

        System.out.println("\nCurrent board:");
        System.out.println(initialGame);

        // Display game information
        System.out.println("Board size: " + initialGame.getRows() + "x" + initialGame.getCols());
        System.out.println("Number of pieces: " + initialGame.getPieces().size());
        System.out.println("Primary piece position: Row " + (initialGame.getPrimaryRow() + 1) +
                ", Col " + (initialGame.getPrimaryCol() + 1));
        System.out.println("Exit position: Row " + (initialGame.getExitRow() + 1) +
                ", Col " + (initialGame.getExitCol() + 1));
    }


    private void loadNewTestCase() {
        try {
            String filename = RushHourIO.getUserInput("Enter new test case filename: ");

            while (!RushHourIO.isValidFile(filename)) {
                System.out.println("File not found or not readable: " + filename);
                filename = RushHourIO.getUserInput("Enter test case filename: ");
            }

            loadGame(filename);

            // Clear previous results
            results.clear();
        } catch (IOException e) {
            System.err.println("Error loading game: " + e.getMessage());
        }
    }

    private void displayPreviousResults() {
        if (results.isEmpty()) {
            System.out.println("No previous results available.");
            return;
        }

        System.out.println("\n=== Previous Results ===");
        RushHourIO.printComparisonTable(results);
    }

    private SearchAlgorithm selectAlgorithm(int choice) {
        switch (choice) {
            case 1:
                return new UCS();
            case 2:
                return new GBFS(1);
            case 3:
                return new AStar(1);
            case 4:
                return new IDAStar(1);
            default:
                return new UCS();
        }
    }

    private boolean requiresHeuristic(int algorithmChoice) {
        // UCS doesn't use heuristic, others do
        return algorithmChoice != 1;
    }

    private void setAlgorithmHeuristic(SearchAlgorithm algorithm, int heuristicChoice) {
        if (algorithm instanceof GBFS) {
            ((GBFS) algorithm).setHeuristicType(heuristicChoice);
        } else if (algorithm instanceof AStar) {
            ((AStar) algorithm).setHeuristicType(heuristicChoice);
        } else if (algorithm instanceof IDAStar) {
            ((IDAStar) algorithm).setHeuristicType(heuristicChoice);
        }
    }

    private void saveResultsOption(SearchAlgorithm algorithm) {
        if (algorithm.getSolution() == null || algorithm.getSolution().isEmpty()) {
            return;
        }

        String input = RushHourIO.getUserInput("Save solution to file? (y/n): ");

        if (input.toLowerCase().startsWith("y")) {
            try {
                String filenameInputted = RushHourIO.getUserInput("Enter filename (without extension): ");
                String filename = "output/" + filenameInputted + ".txt";
                // Prepare solution and actions with final step
                List<RushHourGame> solutionWithExit = new ArrayList<>(algorithm.getSolution());
                List<String> actionsWithExit = new ArrayList<>(algorithm.getSolutionActions());
                RushHourGame finalState = new RushHourGame(solutionWithExit.get(solutionWithExit.size() - 1));
                finalState.removePrimaryPiece();
                solutionWithExit.add(finalState);
                actionsWithExit.add("EXIT");
                RushHourIO.writeSolutionToFile(solutionWithExit, actionsWithExit, algorithm, filename);
                System.out.println("Solution saved to: " + filename);
            } catch (IOException e) {
                System.err.println("Error saving file: " + e.getMessage());
            }
        }
    }

    private void saveComparisonOption() {
        String input = RushHourIO.getUserInput("Save comparison results to file? (y/n): ");

        if (input.toLowerCase().startsWith("y")) {
            // This would require additional implementation to save comparison results
            System.out.println("Comparison save feature will be implemented in future version.");
        }
    }

    public RushHourGame loadGameFromFile(String filename) throws IOException {
        this.initialGame = RushHourIO.loadGameFromFile(filename);
        return this.initialGame;
    }

    public List<RushHourGame> solve(String algorithmType, int heuristicType) {
        if (initialGame == null) {
            throw new IllegalStateException("No game loaded. Call loadGameFromFile first.");
        }

        SearchAlgorithm algorithm;

        switch (algorithmType.toUpperCase()) {
            case "UCS":
                algorithm = new UCS();
                break;
            case "GBFS":
                algorithm = new GBFS(heuristicType);
                break;
            case "A*":
                algorithm = new AStar(heuristicType);
                break;
            case "IDA*":
                algorithm = new IDAStar(heuristicType);
                break;
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithmType);
        }

        RushHourGame gameCopy = new RushHourGame(initialGame);
        return algorithm.solve(gameCopy);
    }

    public Map<String, Map<String, Object>> getResults() {
        return new HashMap<>(results);
    }
}