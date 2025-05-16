package src;

import java.io.*;
import java.util.*;

/**
 * RushHourIO handles all input/output operations for the Rush Hour puzzle solver
 * This includes reading test cases, writing solutions, and formatting output
 */
public class RushHourIO {

    /**
     * Load a Rush Hour game from a text file
     * @param filename Path to the test case file
     * @return RushHourGame object representing the initial state
     * @throws IOException if file cannot be read or format is invalid
     */
    public static RushHourGame loadGameFromFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));

        try {
            // Read dimensions
            String[] dimensions = reader.readLine().trim().split("\\s+");
            if (dimensions.length != 2) {
                throw new IOException("Invalid format: First line must contain two integers (rows cols)");
            }

            int rows = Integer.parseInt(dimensions[0]);
            int cols = Integer.parseInt(dimensions[1]);

            if (rows <= 0 || cols <= 0 || rows > 20 || cols > 20) {
                throw new IOException("Invalid dimensions: Must be between 1-20");
            }

            // Read number of pieces (excluding primary piece)
            String numPiecesStr = reader.readLine().trim();
            int numPieces = Integer.parseInt(numPiecesStr);

            if (numPieces < 0 || numPieces > 50) {
                throw new IOException("Invalid number of pieces: Must be between 0-50");
            }

            // Create game instance
            RushHourGame game = new RushHourGame(rows, cols);
            char[][] board = game.getBoard();
            boolean exitFound = false;

            // Read board configuration
            for (int i = 0; i < rows; i++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IOException("Unexpected end of file at row " + (i + 1));
                }
                
                // Handle potential exit K outside the board bounds
                if (line.length() == cols + 1 && line.charAt(cols) == 'K') {
                    // Exit K is outside the board - place it as a boundary marker
                    for (int j = 0; j < cols; j++) {
                        board[i][j] = line.charAt(j);
                    }
                    // Set exit position as edge position (outside the board)
                    game.setExitPosition(i, cols);
                    exitFound = true;
                } else if (line.length() == cols) {
                    // Normal row
                    for (int j = 0; j < cols; j++) {
                        board[i][j] = line.charAt(j);
                    }
                } else {
                    throw new IOException("Row " + (i + 1) + " has incorrect length. Expected " + cols + " or " + (cols + 1) + " (with K), got " + line.length());
                }
            }

            // Check for other exit positions (top, bottom, left - though only right is in example)
            // This is for completeness, though the current example only has right exit
            if (!exitFound) {
                throw new IOException("No exit 'K' found outside the board boundaries");
            }

            // Validate and initialize game state
            validateAndInitializeGame(game);

            return game;

        } catch (NumberFormatException e) {
            throw new IOException("Invalid number format in input file", e);
        } finally {
            reader.close();
        }
    }

    /**
     * Validate game configuration and initialize piece tracking
     * @param game The game to validate and initialize
     * @throws IOException if game configuration is invalid
     */
    private static void validateAndInitializeGame(RushHourGame game) throws IOException {
        char[][] board = game.getBoard();
        int rows = game.getRows();
        int cols = game.getCols();

        boolean foundPrimary = false;
        game.clearPieces();

        // Scan board and categorize pieces
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char c = board[i][j];

                if (c == 'P') {
                    foundPrimary = true;
                    game.addPiecePosition(c, i, j);
                    // Set first occurrence as primary position
                    if (game.getPrimaryRow() == 0 && game.getPrimaryCol() == 0) {
                        game.setPrimaryPosition(i, j);
                    }
                } else if (c == 'K') {
                    // K should never be inside the board with new format
                    throw new IOException("Exit 'K' found inside the board at (" + i + ", " + j + "). Exit must be outside the board.");
                } else if (c != '.' && c != 'K') {
                    // Regular piece
                    game.addPiecePosition(c, i, j);
                }
            }
        }

        // Validate required elements
        if (!foundPrimary) {
            throw new IOException("No primary piece 'P' found in the board");
        }

        // Validate exit position is set
        if (game.getExitRow() == 0 && game.getExitCol() == 0) {
            throw new IOException("No exit position set. Exit 'K' must be found outside the board.");
        }

        // Validate primary piece
        List<int[]> primaryPositions = game.getPieces().get('P');
        if (primaryPositions == null || primaryPositions.size() < 2) {
            throw new IOException("Primary piece must occupy at least 2 cells");
        }

        // Validate piece continuity and orientation for all pieces
        for (Map.Entry<Character, List<int[]>> entry : game.getPieces().entrySet()) {
            char piece = entry.getKey();
            List<int[]> positions = entry.getValue();

            if (positions.size() < 2) {
                throw new IOException("Piece '" + piece + "' must occupy at least 2 cells");
            }

            // Check if piece forms a continuous line
            if (!isValidPieceShape(positions)) {
                throw new IOException("Piece '" + piece + "' must form a continuous horizontal or vertical line");
            }
        }

        // Validate exit position (must be on board edge or outside)
        int exitRow = game.getExitRow();
        int exitCol = game.getExitCol();
        
        boolean validExit = false;
        
        // Check if exit is on border (outside the board)
        if (exitCol == cols && exitRow >= 0 && exitRow < rows) {
            // Right border
            validExit = true;
        } else if (exitRow == rows && exitCol >= 0 && exitCol < cols) {
            // Bottom border
            validExit = true;
        } else if (exitCol == -1 && exitRow >= 0 && exitRow < rows) {
            // Left border
            validExit = true;
        } else if (exitRow == -1 && exitCol >= 0 && exitCol < cols) {
            // Top border
            validExit = true;
        }
        
        if (!validExit) {
            throw new IOException("Exit must be on the board edge (outside the board bounds). Found at (" + exitRow + ", " + exitCol + ")");
        }

        // Validate that primary piece and exit are aligned
        boolean isHorizontal = checkPrimaryOrientation(game);
        if (isHorizontal && exitRow != game.getPrimaryRow()) {
            throw new IOException("Primary piece is horizontal but exit is not on the same row");
        } else if (!isHorizontal && exitCol != game.getPrimaryCol()) {
            throw new IOException("Primary piece is vertical but exit is not on the same column");
        }
    }

    /**
     * Check the orientation of the primary piece
     * @param game The game instance
     * @return true if horizontal, false if vertical
     */
    private static boolean checkPrimaryOrientation(RushHourGame game) {
        List<int[]> primaryPositions = game.getPieces().get('P');
        if (primaryPositions.size() < 2) return true; // Default to horizontal
        
        return primaryPositions.get(0)[0] == primaryPositions.get(1)[0];
    }

    /**
     * Check if a piece forms a valid continuous line (horizontal or vertical)
     * @param positions List of positions occupied by the piece
     * @return true if valid, false otherwise
     */
    private static boolean isValidPieceShape(List<int[]> positions) {
        if (positions.size() < 2) return false;

        // Sort positions
        positions.sort((a, b) -> {
            if (a[0] != b[0]) return Integer.compare(a[0], b[0]);
            return Integer.compare(a[1], b[1]);
        });

        // Check if all positions are in same row (horizontal piece)
        boolean horizontal = true;
        for (int i = 1; i < positions.size(); i++) {
            if (positions.get(i)[0] != positions.get(0)[0] ||
                    positions.get(i)[1] != positions.get(i-1)[1] + 1) {
                horizontal = false;
                break;
            }
        }

        if (horizontal) return true;

        // Check if all positions are in same column (vertical piece)
        boolean vertical = true;
        for (int i = 1; i < positions.size(); i++) {
            if (positions.get(i)[1] != positions.get(0)[1] ||
                    positions.get(i)[0] != positions.get(i-1)[0] + 1) {
                vertical = false;
                break;
            }
        }

        return vertical;
    }

    /**
     * Write solution to console with colored output
     * @param solution List of game states representing the solution
     * @param actions List of actions taken
     * @param algorithm The search algorithm used
     */
    public static void writeSolutionToConsole(List<RushHourGame> solution, List<String> actions, SearchAlgorithm algorithm) {
        if (solution == null || solution.isEmpty()) {
            System.out.println("No solution found!");
            return;
        }

        // Print header
        System.out.println("=== " + algorithm.getAlgorithmName() + " Solution ===");
        System.out.println("Nodes visited: " + algorithm.getNodesVisited());
        System.out.println("Execution time: " + algorithm.getExecutionTime() + " ms");
        System.out.println("Solution length: " + (solution.size() - 1) + " moves");
        System.out.println();

        // Print initial state
        System.out.println("Papan Awal");
        printColoredBoard(solution.get(0), null);
        System.out.println();

        // Print each move
        for (int i = 1; i < solution.size(); i++) {
            System.out.println("Gerakan " + i + ": " + actions.get(i - 1));
            printColoredBoard(solution.get(i), actions.get(i - 1));
            System.out.println();
        }
    }

    /**
     * Write solution to file
     * @param solution List of game states representing the solution
     * @param actions List of actions taken
     * @param algorithm The search algorithm used
     * @param filename Output filename
     * @throws IOException if file cannot be written
     */
    public static void writeSolutionToFile(List<RushHourGame> solution, List<String> actions,
                                           SearchAlgorithm algorithm, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

        try {
            // Write header
            writer.write("=== " + algorithm.getAlgorithmName() + " Solution ===\n");
            writer.write("Nodes visited: " + algorithm.getNodesVisited() + "\n");
            writer.write("Execution time: " + algorithm.getExecutionTime() + " ms\n");
            writer.write("Solution length: " + (solution.size() - 1) + " moves\n");
            writer.write("\n");

            if (solution == null || solution.isEmpty()) {
                writer.write("No solution found!\n");
                return;
            }

            // Write initial state
            writer.write("Papan Awal\n");
            writer.write(solution.get(0).toString());
            writer.write("\n");

            // Write each move
            for (int i = 1; i < solution.size(); i++) {
                writer.write("Gerakan " + i + ": " + actions.get(i - 1) + "\n");
                writer.write(solution.get(i).toString());
                writer.write("\n");
            }
        } finally {
            writer.close();
        }
    }

    /**
     * Print board with colored output for console
     * @param game The game state to print
     * @param action The action that was taken (for highlighting moved piece)
     */
    private static void printColoredBoard(RushHourGame game, String action) {
        char[][] board = game.getBoard();
        int rows = game.getRows();
        int cols = game.getCols();

        // ANSI color codes
        String RESET = "\u001B[0m";
        String RED = "\u001B[31m";      // Primary piece
        String GREEN = "\u001B[32m";    // Exit indicator (not used since K is outside)
        String YELLOW = "\u001B[33m";   // Moved piece
        String BLUE = "\u001B[34m";     // Other pieces

        char movedPiece = '.';
        if (action != null && !action.equals("Initial")) {
            movedPiece = action.charAt(0);
        }

        // Print board with border to show exit
        System.out.print("  ");
        for (int j = 0; j < cols; j++) {
            System.out.print(j % 10);
        }
        System.out.println();

        for (int i = 0; i < rows; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < cols; j++) {
                char c = board[i][j];

                if (c == game.getPrimaryPiece()) {
                    System.out.print(RED + c + RESET);
                } else if (c != '.' && c == movedPiece) {
                    System.out.print(YELLOW + c + RESET);
                } else if (c != '.' && c != game.getExit()) {
                    System.out.print(BLUE + c + RESET);
                } else {
                    System.out.print(c);
                }
            }
            
            // Show exit marker if it's on this row
            if (game.getExitRow() == i && game.getExitCol() == cols) {
                System.out.print(GREEN + " K" + RESET);
            }
            
            System.out.println();
        }
    }

    /**
     * Read user input from console
     * @param prompt Message to display to user
     * @return User input as string
     */
    public static String getUserInput(String prompt) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /**
     * Get algorithm choice from user
     * @return Algorithm choice (1-4)
     */
    public static int getAlgorithmChoice() {
        Scanner scanner = new Scanner(System.in);
        int choice = 0;

        while (choice < 1 || choice > 4) {
            System.out.println("\nSelect algorithm:");
            System.out.println("1. UCS (Uniform Cost Search)");
            System.out.println("2. GBFS (Greedy Best First Search)");
            System.out.println("3. A* (A-Star)");
            System.out.println("4. IDA* (Iterative Deepening A*)");
            System.out.print("Choice (1-4): ");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice < 1 || choice > 4) {
                    System.out.println("Invalid choice. Please enter 1-4.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        return choice;
    }

    /**
     * Get heuristic choice from user (for algorithms that use heuristics)
     * @return Heuristic choice (1-2)
     */
    public static int getHeuristicChoice() {
        Scanner scanner = new Scanner(System.in);
        int choice = 0;

        while (choice < 1 || choice > 2) {
            System.out.println("\nSelect heuristic:");
            System.out.println("1. Manhattan Distance (H1)");
            System.out.println("2. Blocking Pieces + Distance (H2)");
            System.out.print("Choice (1-2): ");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice < 1 || choice > 2) {
                    System.out.println("Invalid choice. Please enter 1-2.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        return choice;
    }

    /**
     * Validate that a file exists and is readable
     * @param filename Path to file
     * @return true if valid, false otherwise
     */
    public static boolean isValidFile(String filename) {
        File file = new File(filename);
        return file.exists() && file.isFile() && file.canRead();
    }

    /**
     * Get output filename with timestamp
     * @param baseName Base name for the file
     * @param algorithm Algorithm used
     * @return Full filename with timestamp
     */
    public static String generateOutputFilename(String baseName, SearchAlgorithm algorithm) {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String algName = algorithm.getAlgorithmName().replaceAll("[^a-zA-Z0-9]", "_");
        return "solution_" + baseName + "_" + algName + "_" + timestamp + ".txt";
    }

    /**
     * Print algorithm comparison table
     * @param results Map of algorithm results
     */
    public static void printComparisonTable(Map<String, Map<String, Object>> results) {
        System.out.println("\n=== Algorithm Comparison ===");
        System.out.println("Algorithm\t\tNodes\tTime(ms)\tSolution Length\tOptimal");
        System.out.println("----------------------------------------");

        for (Map.Entry<String, Map<String, Object>> entry : results.entrySet()) {
            String alg = entry.getKey();
            Map<String, Object> stats = entry.getValue();

            System.out.printf("%-15s\t%d\t%d\t\t%d\t\t%s\n",
                    alg,
                    (Integer) stats.get("nodes_visited"),
                    (Long) stats.get("execution_time"),
                    (Integer) stats.get("solution_length"),
                    (Boolean) stats.get("optimal") ? "Yes" : "No");
        }
    }
}