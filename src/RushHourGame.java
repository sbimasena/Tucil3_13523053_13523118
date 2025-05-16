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

            // Read board configuration
            for (int i = 0; i < rows; i++) {
                String line = reader.readLine();
                if (line.length() == cols) {
                    // Normal row without exit
                    for (int j = 0; j < cols; j++) {
                        char c = line.charAt(j);
                        game.board[i][j] = c;

                        if (c != '.' && c != 'K') {
                            if (!game.pieces.containsKey(c)) {
                                game.pieces.put(c, new ArrayList<>());
                            }
                            game.pieces.get(c).add(new int[]{i, j});

                            if (c == 'P') {
                                game.primaryRow = i;
                                game.primaryCol = j;
                            }
                        } else if (c == 'K') {
                            throw new IOException("Exit 'K' cannot be inside the board");
                        }
                    }
                } else if (line.length() == cols + 1 && line.charAt(cols) == 'K') {
                    // Row with exit K in the border
                    for (int j = 0; j < cols; j++) {
                        char c = line.charAt(j);
                        game.board[i][j] = c;

                        if (c != '.' && c != 'K') {
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
                    // Exit is in the right border at position (i, cols)
                    game.exitRow = i;
                    game.exitCol = cols;
                } else {
                    throw new IOException("Row " + (i + 1) + " has incorrect length. Expected " + cols + " or " + (cols + 1) + " (with K), got " + line.length());
                }
            }

            // Validate that exit was found
            boolean exitFound = false;
            for (int i = 0; i < rows; i++) {
                String line = reader.readLine();
                if (line != null && line.length() == cols + 1 && line.charAt(cols) == 'K') {
                    exitFound = true;
                }
            }
            
            // Reset reader and re-read properly
            reader.close();
            reader = new BufferedReader(new FileReader(filename));
            reader.readLine(); // skip dimensions
            reader.readLine(); // skip piece count
            
            boolean foundExit = false;
            for (int i = 0; i < rows; i++) {
                String line = reader.readLine();
                if (line.length() == cols + 1 && line.charAt(cols) == 'K') {
                    foundExit = true;
                    break;
                }
            }
            
            if (!foundExit) {
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

        int newCol = (direction > 0) ? maxCol + 1 : minCol - 1;

        // Special handling for primary piece exiting through border
        if (piece == 'P' && exitRow == row && exitCol == cols && direction > 0) {
            // Primary piece can exit through right border
            if (maxCol == cols - 1) {
                // Already at the edge, create goal state
                RushHourGame newState = new RushHourGame(this);
                newState.primaryCol = cols; // Indicate piece has exited
                nextStates.add(newState);
                return;
            }
        }

        // Check bounds - primary piece can move out of bounds to exit
        if (piece == 'P' && exitRow == row && exitCol == newCol) {
            // Primary piece is exiting - this is allowed
        } else if (newCol < 0 || newCol >= cols) {
            return; // Out of bounds for non-exit move
        }

        // Check if target position is available
        if (newCol >= 0 && newCol < cols) {
            char target = board[row][newCol];
            if (target != '.') return;
        }

        // Check if there's space for the entire piece to move
        if (direction < 0) {
            // Moving left
            if (newCol < 0) return; // Out of bounds
            if (board[row][newCol] != '.') return;
        } else {
            // Moving right
            if (piece == 'P' && exitRow == row && newCol == cols) {
                // Primary piece exiting - allowed
            } else if (newCol >= cols) {
                return; // Out of bounds
            } else if (board[row][newCol] != '.') {
                return;
            }
        }

        // Create new state
        RushHourGame newState = new RushHourGame(this);

        // Update board
        if (direction > 0) {
            // Moving right: clear leftmost, add rightmost
            newState.board[row][minCol] = '.';
            if (newCol < cols) {
                newState.board[row][newCol] = piece;
            }
        } else {
            // Moving left: clear rightmost, add leftmost
            newState.board[row][maxCol] = '.';
            if (newCol >= 0) {
                newState.board[row][newCol] = piece;
            }
        }

        // Update piece positions
        List<int[]> newPositions = new ArrayList<>();
        for (int[] pos : positions) {
            newPositions.add(new int[]{pos[0], pos[1] + direction});
        }
        newState.pieces.put(piece, newPositions);

        // Update primary piece position if necessary
        if (piece == 'P') {
            newState.primaryRow = row;
            newState.primaryCol = positions.get(0)[1] + direction;
        }

        nextStates.add(newState);
    }

    private void tryMoveVertical(List<RushHourGame> nextStates, char piece, int direction) {
        List<int[]> positions = pieces.get(piece);
        int col = positions.get(0)[1];

        int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
        for (int[] pos : positions) {
            minRow = Math.min(minRow, pos[0]);
            maxRow = Math.max(maxRow, pos[0]);
        }

        int newRow = (direction > 0) ? maxRow + 1 : minRow - 1;

        // Special handling for primary piece exiting through border
        if (piece == 'P' && exitCol == col && exitRow == rows && direction > 0) {
            // Primary piece can exit through bottom border
            if (maxRow == rows - 1) {
                // Already at the edge, create goal state
                RushHourGame newState = new RushHourGame(this);
                newState.primaryRow = rows; // Indicate piece has exited
                nextStates.add(newState);
                return;
            }
        }

        // Check bounds - primary piece can move out of bounds to exit
        if (piece == 'P' && exitCol == col && exitRow == newRow) {
            // Primary piece is exiting - this is allowed
        } else if (newRow < 0 || newRow >= rows) {
            return; // Out of bounds for non-exit move
        }

        // Check if target position is available
        if (newRow >= 0 && newRow < rows) {
            char target = board[newRow][col];
            if (target != '.') return;
        }

        // Check if there's space for the entire piece to move
        if (direction < 0) {
            // Moving up
            if (newRow < 0) return; // Out of bounds
            if (board[newRow][col] != '.') return;
        } else {
            // Moving down
            if (piece == 'P' && exitCol == col && newRow == rows) {
                // Primary piece exiting - allowed
            } else if (newRow >= rows) {
                return; // Out of bounds
            } else if (board[newRow][col] != '.') {
                return;
            }
        }

        // Create new state
        RushHourGame newState = new RushHourGame(this);

        // Update board
        if (direction > 0) {
            // Moving down: clear topmost, add bottommost
            newState.board[minRow][col] = '.';
            if (newRow < rows) {
                newState.board[newRow][col] = piece;
            }
        } else {
            // Moving up: clear bottommost, add topmost
            newState.board[maxRow][col] = '.';
            if (newRow >= 0) {
                newState.board[newRow][col] = piece;
            }
        }

        // Update piece positions
        List<int[]> newPositions = new ArrayList<>();
        for (int[] pos : positions) {
            newPositions.add(new int[]{pos[0] + direction, pos[1]});
        }
        newState.pieces.put(piece, newPositions);

        // Update primary piece position if necessary
        if (piece == 'P') {
            newState.primaryRow = positions.get(0)[0] + direction;
            newState.primaryCol = col;
        }

        nextStates.add(newState);
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