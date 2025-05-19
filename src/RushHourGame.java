package src;

import java.util.*;
import java.io.*;

public class RushHourGame {
    private int rows, cols;
    private char[][] board;
    private char primaryPiece = 'P';
    private char exit = 'K';
    protected int exitRow, exitCol;
    protected int primaryRow, primaryCol;
    private Map<Character, List<int[]>> pieces;

    public RushHourGame(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.board = new char[rows][cols];
        this.pieces = new HashMap<>();
    }

    // Copy constructor for creating new states
    public RushHourGame(RushHourGame other) {
        this.rows = other.rows;
        this.cols = other.cols;
        this.board = new char[rows][cols];
        this.pieces = new HashMap<>();

        // Deep copy board
        for (int i = 0; i < rows; i++) {
            System.arraycopy(other.board[i], 0, this.board[i], 0, cols);
        }

        // Deep copy pieces
        for (Map.Entry<Character, List<int[]>> entry : other.pieces.entrySet()) {
            List<int[]> newPieces = new ArrayList<>();
            for (int[] piece : entry.getValue()) {
                newPieces.add(piece.clone());
            }
            this.pieces.put(entry.getKey(), newPieces);
        }

        this.exitRow = other.exitRow;
        this.exitCol = other.exitCol;
        this.primaryRow = other.primaryRow;
        this.primaryCol = other.primaryCol;
    }

    public static RushHourGame loadFromFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));

        try {
            // Read dimensions
            String[] dimensions = reader.readLine().split(" ");
            int rows = Integer.parseInt(dimensions[0]);
            int cols = Integer.parseInt(dimensions[1]);

            // Read number of pieces (not including primary piece)
            int numPieces = Integer.parseInt(reader.readLine());

            RushHourGame game = new RushHourGame(rows, cols);

            // Read all lines to find the board and exit
            List<String> allLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                allLines.add(line);
            }

            // Check for exit at the top (before the board)
            boolean exitFound = false;
            int startLine = 0;
            
            // Check if first line is just "K" (exit at top)
            if (allLines.size() > 0 && allLines.get(0).trim().equals("K")) {
                game.exitRow = -1;
                game.exitCol = cols / 2; // Place at center of top border
                exitFound = true;
                startLine = 1;
            }

            // Read the board
            for (int i = 0; i < rows; i++) {
                if (startLine + i >= allLines.size()) {
                    throw new IOException("Not enough lines for board");
                }
                
                String boardLine = allLines.get(startLine + i);
                
                // Check for exit on left side
                if (boardLine.startsWith("K")) {
                    game.exitRow = i;
                    game.exitCol = -1;
                    exitFound = true;
                    boardLine = boardLine.substring(1); // Remove K from beginning
                }
                
                // Check for exit on right side
                if (boardLine.endsWith("K")) {
                    game.exitRow = i;
                    game.exitCol = cols;
                    exitFound = true;
                    boardLine = boardLine.substring(0, boardLine.length() - 1); // Remove K from end
                }

                // Process the board line
                if (boardLine.length() != cols) {
                    throw new IOException("Row " + (i + 1) + " has incorrect length. Expected " + cols + ", got " + boardLine.length());
                }

                for (int j = 0; j < cols; j++) {
                    char c = boardLine.charAt(j);
                    game.board[i][j] = c;

                    if (c != '.') {
                        if (!game.pieces.containsKey(c)) {
                            game.pieces.put(c, new ArrayList<>());
                        }
                        game.pieces.get(c).add(new int[]{i, j});

                        if (c == 'P') {
                            game.primaryRow = i;
                            game.primaryCol = j;
                        }
                    }
                }
            }

            // Check for exit at the bottom (after the board)
            if (startLine + rows < allLines.size()) {
                String bottomLine = allLines.get(startLine + rows);
                if (bottomLine.trim().equals("K")) {
                    game.exitRow = rows;
                    game.exitCol = cols / 2; // Place at center of bottom border
                    exitFound = true;
                }
            }

            if (!exitFound) {
                throw new IOException("No exit 'K' found outside the board border");
            }

            return game;
        } finally {
            reader.close();
        }
    }
    public boolean isGoalState() {
        // Check if primary piece can exit
        List<int[]> primaryPositions = pieces.get('P');
        if (primaryPositions == null) return false;

        // Determine orientation of primary piece
        boolean isHorizontal = primaryPositions.size() > 1 &&
                primaryPositions.get(0)[0] == primaryPositions.get(1)[0];

        if (isHorizontal) {
            // Primary piece is horizontal, exit should be on the same row
            if (exitRow != primaryRow) return false;

            // Check if we can slide the primary piece to the exit
            int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;
            for (int[] pos : primaryPositions) {
                minCol = Math.min(minCol, pos[1]);
                maxCol = Math.max(maxCol, pos[1]);
            }

            // Check if exit is reachable
            if (exitCol == cols) {
                // Exit is at right border
                if (maxCol == cols - 1) {
                    // Primary piece is already at the edge, goal reached
                    return true;
                } else {
                    // Check if path to right edge is clear
                    for (int j = maxCol + 1; j < cols; j++) {
                        if (board[exitRow][j] != '.') {
                            return false;
                        }
                    }
                    return true;
                }
            } else if (exitCol > maxCol) {
                // Exit is to the right, check if path is clear
                for (int j = maxCol + 1; j <= exitCol; j++) {
                    if (board[exitRow][j] != '.') {
                        return false;
                    }
                }
                return true;
            } else if (exitCol < minCol) {
                // Exit is to the left, check if path is clear
                for (int j = exitCol; j < minCol; j++) {
                    if (board[exitRow][j] != '.') {
                        return false;
                    }
                }
                return true;
            }
        } else {
            // Primary piece is vertical, exit should be on the same column
            if (exitCol != primaryCol) return false;

            // Check if we can slide the primary piece to the exit
            int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
            for (int[] pos : primaryPositions) {
                minRow = Math.min(minRow, pos[0]);
                maxRow = Math.max(maxRow, pos[0]);
            }

            // Check if exit is reachable
            if (exitRow == rows) {
                // Exit is at bottom border
                if (maxRow == rows - 1) {
                    // Primary piece is already at the edge, goal reached
                    return true;
                } else {
                    // Check if path to bottom edge is clear
                    for (int i = maxRow + 1; i < rows; i++) {
                        if (board[i][exitCol] != '.') {
                            return false;
                        }
                    }
                    return true;
                }
            } else if (exitRow > maxRow) {
                // Exit is below, check if path is clear
                for (int i = maxRow + 1; i <= exitRow; i++) {
                    if (board[i][exitCol] != '.') {
                        return false;
                    }
                }
                return true;
            } else if (exitRow < minRow) {
                // Exit is above, check if path is clear
                for (int i = exitRow; i < minRow; i++) {
                    if (board[i][exitCol] != '.') {
                        return false;
                    }
                }
                return true;
            }
        }

        return false;
    }

    public List<RushHourGame> getNextStates() {
        List<RushHourGame> nextStates = new ArrayList<>();

        for (Map.Entry<Character, List<int[]>> entry : pieces.entrySet()) {
            char piece = entry.getKey();
            List<int[]> positions = entry.getValue();

            if (positions.size() < 2) continue;

            // Determine if piece is horizontal or vertical
            boolean isHorizontal = positions.get(0)[0] == positions.get(1)[0];

            if (isHorizontal) {
                // Try moving left and right
                tryMoveHorizontal(nextStates, piece, -1); // left
                tryMoveHorizontal(nextStates, piece, 1);  // right
            } else {
                // Try moving up and down
                tryMoveVertical(nextStates, piece, -1); // up
                tryMoveVertical(nextStates, piece, 1);  // down
            }
        }

        return nextStates;
    }

    private void tryMoveHorizontal(List<RushHourGame> nextStates, char piece, int direction) {
    List<int[]> positions = pieces.get(piece);
    int row = positions.get(0)[0];

    int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;
    for (int[] pos : positions) {
        minCol = Math.min(minCol, pos[1]);
        maxCol = Math.max(maxCol, pos[1]);
    }

    int step = 1;
    while (true) {
        int newCol = (direction > 0) ? maxCol + step : minCol - step;

        // Allow primary piece to exit if at the border
        if (piece == 'P' && exitRow == row && newCol == exitCol) {
            RushHourGame newState = new RushHourGame(this);
            newState.primaryRow = row;
            newState.primaryCol = exitCol;
            nextStates.add(newState);
            break;
        }

        // Check bounds
        if (newCol < 0 || newCol >= cols) break;

        // Check all positions along the path
        boolean clear = true;
        for (int s = 1; s <= step; s++) {
            int colToCheck = (direction > 0) ? maxCol + s : minCol - s;
            if (colToCheck < 0 || colToCheck >= cols || board[row][colToCheck] != '.') {
                clear = false;
                break;
            }
        }

        if (!clear) break;

        // Create new state
        RushHourGame newState = new RushHourGame(this);

        // Clear old positions
        for (int[] pos : positions) {
            newState.board[pos[0]][pos[1]] = '.';
        }

        // Create updated positions
        List<int[]> newPositions = new ArrayList<>();
        for (int[] pos : positions) {
            int newY = pos[1] + direction * step;
            newState.board[row][newY] = piece;
            newPositions.add(new int[]{row, newY});
        }

        newState.pieces.put(piece, newPositions);
        if (piece == 'P') {
            newState.primaryRow = row;
            newState.primaryCol = newPositions.get(0)[1];
        }

        nextStates.add(newState);
        step++;
    }
}

   private void tryMoveVertical(List<RushHourGame> nextStates, char piece, int direction) {
    List<int[]> positions = pieces.get(piece);
    int col = positions.get(0)[1];

    int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
    for (int[] pos : positions) {
        minRow = Math.min(minRow, pos[0]);
        maxRow = Math.max(maxRow, pos[0]);
    }

    int step = 1;
    while (true) {
        int newRow = (direction > 0) ? maxRow + step : minRow - step;

        // Allow primary piece to exit if at the border
        if (piece == 'P' && exitCol == col && newRow == exitRow) {
            RushHourGame newState = new RushHourGame(this);
            newState.primaryRow = exitRow;
            newState.primaryCol = col;
            nextStates.add(newState);
            break;
        }

        // Check bounds
        if (newRow < 0 || newRow >= rows) break;

        // Check all positions along the path
        boolean clear = true;
        for (int s = 1; s <= step; s++) {
            int rowToCheck = (direction > 0) ? maxRow + s : minRow - s;
            if (rowToCheck < 0 || rowToCheck >= rows || board[rowToCheck][col] != '.') {
                clear = false;
                break;
            }
        }

        if (!clear) break;

        // Create new state
        RushHourGame newState = new RushHourGame(this);

        // Clear old positions
        for (int[] pos : positions) {
            newState.board[pos[0]][pos[1]] = '.';
        }

        // Create updated positions
        List<int[]> newPositions = new ArrayList<>();
        for (int[] pos : positions) {
            int newX = pos[0] + direction * step;
            newState.board[newX][col] = piece;
            newPositions.add(new int[]{newX, col});
        }

        newState.pieces.put(piece, newPositions);
        if (piece == 'P') {
            newState.primaryRow = newPositions.get(0)[0];
            newState.primaryCol = col;
        }

        nextStates.add(newState);
        step++;
    }
}

    // Calculate heuristic 1: Manhattan distance from primary piece to exit
    public int getHeuristic1() {
        if (isGoalState()) return 0;

        List<int[]> primaryPositions = pieces.get('P');
        if (primaryPositions == null) return Integer.MAX_VALUE;

        // Determine if primary piece is horizontal or vertical
        boolean isHorizontal = primaryPositions.size() > 1 &&
                primaryPositions.get(0)[0] == primaryPositions.get(1)[0];

        if (isHorizontal) {
            // Primary piece is horizontal
            int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;
            for (int[] pos : primaryPositions) {
                minCol = Math.min(minCol, pos[1]);
                maxCol = Math.max(maxCol, pos[1]);
            }

            // Calculate distance to exit
            if (exitRow == primaryRow) {
                if (exitCol == cols) {
                    // Exit is at right border
                    return cols - maxCol - 1;
                } else if (exitCol > maxCol) {
                    return exitCol - maxCol;
                } else if (exitCol < minCol) {
                    return minCol - exitCol;
                }
            }
            return Math.abs(exitRow - primaryRow) + Math.min(Math.abs(exitCol - minCol), Math.abs(exitCol - maxCol));
        } else {
            // Primary piece is vertical
            int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
            for (int[] pos : primaryPositions) {
                minRow = Math.min(minRow, pos[0]);
                maxRow = Math.max(maxRow, pos[0]);
            }

            // Calculate distance to exit
            if (exitCol == primaryCol) {
                if (exitRow == rows) {
                    // Exit is at bottom border
                    return rows - maxRow - 1;
                } else if (exitRow > maxRow) {
                    return exitRow - maxRow;
                } else if (exitRow < minRow) {
                    return minRow - exitRow;
                }
            }
            return Math.abs(exitCol - primaryCol) + Math.min(Math.abs(exitRow - minRow), Math.abs(exitRow - maxRow));
        }
    }

    // Calculate heuristic 2: Number of blocking pieces + distance
    public int getHeuristic2() {
        if (isGoalState()) return 0;

        List<int[]> primaryPositions = pieces.get('P');
        if (primaryPositions == null) return Integer.MAX_VALUE;

        // Determine if primary piece is horizontal or vertical
        boolean isHorizontal = primaryPositions.size() > 1 &&
                primaryPositions.get(0)[0] == primaryPositions.get(1)[0];

        int blockingPieces = 0;
        int distance = 0;

        if (isHorizontal) {
            // Primary piece is horizontal
            int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;
            for (int[] pos : primaryPositions) {
                minCol = Math.min(minCol, pos[1]);
                maxCol = Math.max(maxCol, pos[1]);
            }

            // Count blocking pieces and calculate distance
            if (exitRow == primaryRow) {
                if (exitCol == cols) {
                    // Exit is at right border
                    distance = cols - maxCol - 1;
                    for (int j = maxCol + 1; j < cols; j++) {
                        if (board[exitRow][j] != '.') {
                            blockingPieces++;
                        }
                    }
                } else if (exitCol > maxCol) {
                    distance = exitCol - maxCol;
                    for (int j = maxCol + 1; j < exitCol; j++) {
                        if (board[exitRow][j] != '.') {
                            blockingPieces++;
                        }
                    }
                } else if (exitCol < minCol) {
                    distance = minCol - exitCol;
                    for (int j = exitCol + 1; j < minCol; j++) {
                        if (board[exitRow][j] != '.') {
                            blockingPieces++;
                        }
                    }
                }
            } else {
                distance = Math.abs(exitRow - primaryRow);
                blockingPieces = 1; // Primary piece needs to be moved vertically first
            }
        } else {
            // Primary piece is vertical
            int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
            for (int[] pos : primaryPositions) {
                minRow = Math.min(minRow, pos[0]);
                maxRow = Math.max(maxRow, pos[0]);
            }

            // Count blocking pieces and calculate distance
            if (exitCol == primaryCol) {
                if (exitRow == rows) {
                    // Exit is at bottom border
                    distance = rows - maxRow - 1;
                    for (int i = maxRow + 1; i < rows; i++) {
                        if (board[i][exitCol] != '.') {
                            blockingPieces++;
                        }
                    }
                } else if (exitRow > maxRow) {
                    distance = exitRow - maxRow;
                    for (int i = maxRow + 1; i < exitRow; i++) {
                        if (board[i][exitCol] != '.') {
                            blockingPieces++;
                        }
                    }
                } else if (exitRow < minRow) {
                    distance = minRow - exitRow;
                    for (int i = exitRow + 1; i < minRow; i++) {
                        if (board[i][exitCol] != '.') {
                            blockingPieces++;
                        }
                    }
                }
            } else {
                distance = Math.abs(exitCol - primaryCol);
                blockingPieces = 1; // Primary piece needs to be moved horizontally first
            }
        }

        return blockingPieces * 2 + distance;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        RushHourGame other = (RushHourGame) obj;

        if (rows != other.rows || cols != other.cols) return false;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] != other.board[i][j]) return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = rows * 31 + cols;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                hash = hash * 31 + board[i][j];
            }
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sb.append(board[i][j]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    // Getters
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public char[][] getBoard() { return board; }
    public char getPrimaryPiece() { return primaryPiece; }
    public char getExit() { return exit; }
    public int getExitRow() { return exitRow; }
    public int getExitCol() { return exitCol; }
    public int getPrimaryRow() { return primaryRow; }
    public int getPrimaryCol() { return primaryCol; }
    public Map<Character, List<int[]>> getPieces() { return pieces; }

    // Setter methods
    public void setExitPosition(int row, int col) {
        this.exitRow = row;
        this.exitCol = col;
    }

    public void setPrimaryPosition(int row, int col) {
        this.primaryRow = row;
        this.primaryCol = col;
    }

    public void clearPieces() {
        this.pieces.clear();
    }

    public void addPiecePosition(char piece, int row, int col) {
        if (!pieces.containsKey(piece)) {
            pieces.put(piece, new ArrayList<>());
        }
        pieces.get(piece).add(new int[]{row, col});
    }
}