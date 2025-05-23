package src;

import java.io.*;
import java.util.*;

public class RushHourIO {
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

            // Detect top exit before reading the board
            String peekLine = reader.readLine();
            if (peekLine != null && peekLine.trim().length() == 1 && peekLine.trim().charAt(0) == 'K') {
                int topExitCol = peekLine.indexOf('K');
                game.setExitPosition(-1, topExitCol);
                exitFound = true;
                peekLine = reader.readLine();
            }

            // Read all lines after header and possible top exit
            List<String> allLines = new ArrayList<>();
            if (peekLine != null) allLines.add(peekLine);
            String line;
            while ((line = reader.readLine()) != null) {
                allLines.add(line);
            }

            // Find possible bottom exit line (a line with only 'K' and spaces)
            int bottomExitIdx = -1;
            for (int i = 0; i < allLines.size(); i++) {
                String l = allLines.get(i);
                if (l.trim().length() == 1 && l.trim().charAt(0) == 'K') {
                    bottomExitIdx = i;
                    break;
                }
            }

            int boardEndIdx = (bottomExitIdx == -1) ? allLines.size() : bottomExitIdx;
            List<String> boardLines = allLines.subList(0, boardEndIdx);

            // Validate number of board lines
            if (boardLines.size() != rows) {
                throw new IOException("Number of board lines (" + boardLines.size() + ") does not match specified rows (" + rows + ")");
            }

            // Validate each board line's length
            for (int i = 0; i < boardLines.size(); i++) {
                String bl = boardLines.get(i);
                int expected1 = cols;
                int expected2 = cols + 1; 
                if (bl.length() != expected1 && bl.length() != expected2) {
                    throw new IOException("Row " + (i + 1) + " has incorrect length: expected " + expected1 + " or " + expected2 + ", got " + bl.length());
                }
            }

            // Fill the board as before
            for (int i = 0; i < rows; i++) {
                String bl = boardLines.get(i);
                if (bl.length() == cols + 1 && bl.charAt(0) == 'K') {
                    // Exit on left
                    for (int j = 1; j <= cols; j++) {
                        board[i][j - 1] = bl.charAt(j);
                    }
                    game.setExitPosition(i, -1);
                    exitFound = true;
                } else if (bl.length() == cols + 1 && bl.charAt(cols) == 'K') {
                    // Exit on right
                    for (int j = 0; j < cols; j++) {
                        board[i][j] = bl.charAt(j);
                    }
                    game.setExitPosition(i, cols);
                    exitFound = true;
                } else if (bl.length() == cols + 1 && bl.charAt(0) == ' ') {
                    for (int j = 1; j <= cols; j++) {
                        board[i][j - 1] = bl.charAt(j);
                    }
                } else if (bl.length() == cols) {
                    for (int j = 0; j < cols; j++) {
                        board[i][j] = bl.charAt(j);
                    }
                } else {
                    throw new IOException("Row " + (i + 1) + " has incorrect length or invalid 'K' placement");
                }
            }

            // Handle bottom exit if present
            if (bottomExitIdx != -1) {
                String bottomLine = allLines.get(bottomExitIdx);
                int colK = bottomLine.indexOf('K');
                game.setExitPosition(rows, colK); 
                exitFound = true;
                for (int i = bottomExitIdx + 1; i < allLines.size(); i++) {
                    if (!allLines.get(i).trim().isEmpty()) {
                        throw new IOException("Extra non-empty line found after board and exit: '" + allLines.get(i) + "'");
                    }
                }
            } else {
                for (int i = boardEndIdx; i < allLines.size(); i++) {
                    if (!allLines.get(i).trim().isEmpty()) {
                        throw new IOException("Extra non-empty line found after board: '" + allLines.get(i) + "'");
                    }
                }
            }

            if (!exitFound) {
                throw new IOException("No exit 'K' found outside the board boundaries");
            }

            // Validate and initialize game state
            validateAndInitializeGame(game);

            // Validate number of unique pieces (excluding 'P')
            Set<Character> uniquePieces = new HashSet<>();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    char c = board[i][j];
                    if (c != '.' && c != 'P') {
                        uniquePieces.add(c);
                    }
                }
            }
            if (uniquePieces.size() != numPieces) {
                throw new IOException("Number of unique pieces (excluding 'P') in board (" + uniquePieces.size() + ") does not match specified number (" + numPieces + ")");
            }

            return game;

        } catch (NumberFormatException e) {
            throw new IOException("Invalid number format in input file", e);
        } finally {
            reader.close();
        }
    }

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
                    if (game.getPrimaryRow() == 0 && game.getPrimaryCol() == 0) {
                        game.setPrimaryPosition(i, j);
                    }
                } else if (c == 'K') {
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

    private static boolean checkPrimaryOrientation(RushHourGame game) {
        List<int[]> primaryPositions = game.getPieces().get('P');
        if (primaryPositions.size() < 2) return true; 
        
        return primaryPositions.get(0)[0] == primaryPositions.get(1)[0];
    }

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

    private static void printColoredBoard(RushHourGame game, String action) {
        char[][] board = game.getBoard();
        int rows = game.getRows();
        int cols = game.getCols();

        // ANSI color codes
        String RESET = "\u001B[0m";
        String RED = "\u001B[31m";      // Primary piece
        String GREEN = "\u001B[32m";   
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


    public static String getUserInput(String prompt) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }


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

    public static boolean isValidFile(String filename) {
        File file = new File(filename);
        return file.exists() && file.isFile() && file.canRead();
    }

    public static String generateOutputFilename(String baseName, SearchAlgorithm algorithm) {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String algName = algorithm.getAlgorithmName().replaceAll("[^a-zA-Z0-9]", "_");
        return "solution_" + baseName + "_" + algName + "_" + timestamp + ".txt";
    }

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