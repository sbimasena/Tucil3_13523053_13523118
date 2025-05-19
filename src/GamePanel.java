package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;


public class GamePanel extends JPanel {
    private RushHourGame game;
    private int cellSize;
    private int boardMargin;
    private String lastAction;
    private char highlightedPiece;
    
    // Colors for different elements
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color BOARD_COLOR = Color.LIGHT_GRAY;
    private static final Color CELL_COLOR = Color.WHITE;
    private static final Color PRIMARY_PIECE_COLOR = Color.RED;
    private static final Color EXIT_COLOR = Color.GREEN;
    private static final Color MOVED_PIECE_COLOR = Color.YELLOW;
    private static final Color OTHER_PIECE_COLOR = Color.BLUE;
    private static final Color GRID_COLOR = Color.GRAY;
    private static final Color WALL_COLOR = Color.DARK_GRAY;
    
    // Piece type indicators
    private enum PieceType {
        PRIMARY, EXIT, MOVED, OTHER, EMPTY
    }
    
   
    public GamePanel() {
        this.game = null;
        this.boardMargin = 50;
        this.lastAction = null;
        this.highlightedPiece = '.';
        
        // Set preferred size
        setPreferredSize(new Dimension(600, 600));
        setBackground(BACKGROUND_COLOR);
        
        // Add mouse listener for interaction (optional - for debugging/testing)
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (game != null) {
                    Point cell = getCellFromPoint(e.getPoint());
                    if (cell != null) {
                        onCellClicked(cell.x, cell.y);
                    }
                }
            }
        });
    }
    

    public void setGame(RushHourGame game) {
        this.game = game;
        this.lastAction = null;
        this.highlightedPiece = '.';
        repaint();
    }
    

    public void setLastAction(String action) {
        this.lastAction = action;
        this.highlightedPiece = '.';
        
        if (action != null && !action.equals("Initial")) {
            String[] parts = action.split("-");
            if (parts.length > 0) {
                this.highlightedPiece = parts[0].charAt(0);
            }
        }
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (game == null) {
            drawEmptyBoard(g);
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        calculateDimensions();
        
        // Draw board background and borders
        drawBoardBackground(g2d);
        drawBoardWalls(g2d);
        
        // Draw grid and cells
        drawGrid(g2d);
        drawCells(g2d);
        
        // Draw exit indicator
        drawExit(g2d);
        
        // Draw game information
        drawGameInfo(g2d);
        
        g2d.dispose();
    }

    private void drawEmptyBoard(Graphics g) {
        g.setColor(Color.GRAY);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g.getFontMetrics();
        String text = "Load a puzzle to begin";
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = getHeight() / 2;
        g.drawString(text, x, y);
    }

    private void calculateDimensions() {
        if (game == null) return;
        
        int availableWidth = getWidth() - (2 * boardMargin);
        int availableHeight = getHeight() - (2 * boardMargin) - 100; // 100px for info area
        
        int rows = game.getRows();
        int cols = game.getCols();
        
        cellSize = Math.min(availableWidth / cols, availableHeight / rows);
        cellSize = Math.max(cellSize, 20); // Minimum cell size
    }

    private void drawBoardBackground(Graphics2D g2d) {
        int rows = game.getRows();
        int cols = game.getCols();
        
        // Calculate board position
        int boardWidth = cols * cellSize;
        int boardHeight = rows * cellSize;
        int boardX = (getWidth() - boardWidth) / 2;
        int boardY = boardMargin;
        
        // Draw board background
        g2d.setColor(BOARD_COLOR);
        g2d.fillRect(boardX - 5, boardY - 5, boardWidth + 10, boardHeight + 10);
    }

    private void drawBoardWalls(Graphics2D g2d) {
        int rows = game.getRows();
        int cols = game.getCols();
        
        int boardWidth = cols * cellSize;
        int boardHeight = rows * cellSize;
        int boardX = (getWidth() - boardWidth) / 2;
        int boardY = boardMargin;
        
        g2d.setColor(WALL_COLOR);
        g2d.setStroke(new BasicStroke(4));
        
        // Draw walls - but leave gap for exit
        int exitRow = game.getExitRow();
        int exitCol = game.getExitCol();
        
        // Top wall
        g2d.drawLine(boardX, boardY, boardX + boardWidth, boardY);
        
        // Bottom wall
        g2d.drawLine(boardX, boardY + boardHeight, boardX + boardWidth, boardY + boardHeight);
        
        // Left wall
        g2d.drawLine(boardX, boardY, boardX, boardY + boardHeight);
        
        // Right wall - with potential gap for exit
        if (exitCol == cols && exitRow >= 0 && exitRow < rows) {
            // Gap for right exit
            int exitY = boardY + exitRow * cellSize;
            g2d.drawLine(boardX + boardWidth, boardY, boardX + boardWidth, exitY);
            g2d.drawLine(boardX + boardWidth, exitY + cellSize, boardX + boardWidth, boardY + boardHeight);
        } else {
            // Full right wall
            g2d.drawLine(boardX + boardWidth, boardY, boardX + boardWidth, boardY + boardHeight);
        }
        
        // Draw other exit walls if needed (top, bottom, left)
        // This extends the current implementation to support exits on all sides
    }

    private void drawGrid(Graphics2D g2d) {
        int rows = game.getRows();
        int cols = game.getCols();
        
        int boardWidth = cols * cellSize;
        int boardHeight = rows * cellSize;
        int boardX = (getWidth() - boardWidth) / 2;
        int boardY = boardMargin;
        
        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(1));
        
        // Vertical lines
        for (int i = 0; i <= cols; i++) {
            int x = boardX + i * cellSize;
            g2d.drawLine(x, boardY, x, boardY + boardHeight);
        }
        
        // Horizontal lines
        for (int i = 0; i <= rows; i++) {
            int y = boardY + i * cellSize;
            g2d.drawLine(boardX, y, boardX + boardWidth, y);
        }
    }

    private void drawCells(Graphics2D g2d) {
        char[][] board = game.getBoard();
        int rows = game.getRows();
        int cols = game.getCols();
        
        int boardWidth = cols * cellSize;
        int boardX = (getWidth() - boardWidth) / 2;
        int boardY = boardMargin;
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char cell = board[i][j];
                int x = boardX + j * cellSize;
                int y = boardY + i * cellSize;
                
                // Determine piece type
                PieceType type = getPieceType(cell);
                
                // Draw cell background
                g2d.setColor(CELL_COLOR);
                g2d.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                
                // Draw piece
                if (cell != '.') {
                    drawPiece(g2d, x, y, cell, type);
                }
            }
        }
    }

    private PieceType getPieceType(char piece) {
        if (piece == '.') {
            return PieceType.EMPTY;
        } else if (piece == game.getPrimaryPiece()) {
            return PieceType.PRIMARY;
        } else if (piece == game.getExit()) {
            return PieceType.EXIT;
        } else if (piece == highlightedPiece) {
            return PieceType.MOVED;
        } else {
            return PieceType.OTHER;
        }
    }

    private void drawPiece(Graphics2D g2d, int x, int y, char piece, PieceType type) {
        Color pieceColor;
        
        switch (type) {
            case PRIMARY:
                pieceColor = PRIMARY_PIECE_COLOR;
                break;
            case EXIT:
                pieceColor = EXIT_COLOR;
                break;
            case MOVED:
                pieceColor = MOVED_PIECE_COLOR;
                break;
            case OTHER:
                pieceColor = OTHER_PIECE_COLOR;
                break;
            default:
                return; // Don't draw empty cells
        }
        
        // Draw piece with rounded corners
        g2d.setColor(pieceColor);
        g2d.fillRoundRect(x + 3, y + 3, cellSize - 6, cellSize - 6, 8, 8);
        
        // Draw piece outline
        g2d.setColor(pieceColor.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x + 3, y + 3, cellSize - 6, cellSize - 6, 8, 8);
        
        // Draw piece letter
        g2d.setColor(Color.WHITE);
        Font font = new Font("Arial", Font.BOLD, cellSize / 3);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (cellSize - fm.charWidth(piece)) / 2;
        int textY = y + (cellSize + fm.getAscent()) / 2;
        g2d.drawString(String.valueOf(piece), textX, textY);
    }

    private void drawExit(Graphics2D g2d) {
        int exitRow = game.getExitRow();
        int exitCol = game.getExitCol();
        int rows = game.getRows();
        int cols = game.getCols();
        
        int boardWidth = cols * cellSize;
        int boardHeight = rows * cellSize;
        int boardX = (getWidth() - boardWidth) / 2;
        int boardY = boardMargin;
        
        g2d.setColor(EXIT_COLOR);
        g2d.setStroke(new BasicStroke(6));
        
        if (exitCol == cols && exitRow >= 0 && exitRow < rows) {
            // Right exit
            int y = boardY + exitRow * cellSize + cellSize / 2;
            int x = boardX + cols * cellSize;
            
            // Draw arrow pointing right
            g2d.drawLine(x, y, x + 30, y);
            g2d.drawLine(x + 20, y - 10, x + 30, y);
            g2d.drawLine(x + 20, y + 10, x + 30, y);
            
            // Draw EXIT text
            g2d.setColor(EXIT_COLOR);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("EXIT", x + 35, y + 5);
        } else if (exitCol == -1 && exitRow >= 0 && exitRow < rows) {
            // Left exit
            int y = boardY + exitRow * cellSize + cellSize / 2;
            int x = boardX;
            
            // Draw arrow pointing left
            g2d.drawLine(x, y, x - 30, y);
            g2d.drawLine(x - 20, y - 10, x - 30, y);
            g2d.drawLine(x - 20, y + 10, x - 30, y);
            
            // Draw EXIT text
            g2d.setColor(EXIT_COLOR);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("EXIT", x - 65, y + 5);
        } else if (exitRow == -1 && exitCol >= 0 && exitCol < cols) {
            // Top exit
            int x = boardX + exitCol * cellSize + cellSize / 2;
            int y = boardY;
            
            // Draw arrow pointing up
            g2d.drawLine(x, y, x, y - 30);
            g2d.drawLine(x - 10, y - 20, x, y - 30);
            g2d.drawLine(x + 10, y - 20, x, y - 30);
            
            // Draw EXIT text
            g2d.setColor(EXIT_COLOR);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("EXIT", x - 20, y - 35);
        } else if (exitRow == rows && exitCol >= 0 && exitCol < cols) {
            // Bottom exit
            int x = boardX + exitCol * cellSize + cellSize / 2;
            int y = boardY + rows * cellSize;
            
            // Draw arrow pointing down
            g2d.drawLine(x, y, x, y + 30);
            g2d.drawLine(x - 10, y + 20, x, y + 30);
            g2d.drawLine(x + 10, y + 20, x, y + 30);
            
            // Draw EXIT text
            g2d.setColor(EXIT_COLOR);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("EXIT", x - 20, y + 45);
        }
    }

    private void drawGameInfo(Graphics2D g2d) {
        if (game == null) return;
        
        int y = getHeight() - 60;
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Game info
        String info = String.format("Board: %dx%d | Pieces: %d | Exit: (%d,%d)",
            game.getRows(), game.getCols(),
            game.getPieces().size(),
            game.getExitRow(), game.getExitCol());
        g2d.drawString(info, 10, y);
        
        // Last action info
        if (lastAction != null && !lastAction.equals("Initial")) {
            y += 20;
            g2d.drawString("Last Action: " + lastAction, 10, y);
        }
        
        // Legend
        y += 20;
        drawLegend(g2d, 10, y);
    }

    private void drawLegend(Graphics2D g2d, int x, int y) {
        int legendSize = 15;
        int spacing = 100;
        
        // Primary piece
        g2d.setColor(PRIMARY_PIECE_COLOR);
        g2d.fillRect(x, y, legendSize, legendSize);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Primary (P)", x + legendSize + 5, y + 12);
        
        // Other pieces
        x += spacing;
        g2d.setColor(OTHER_PIECE_COLOR);
        g2d.fillRect(x, y, legendSize, legendSize);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Other Pieces", x + legendSize + 5, y + 12);
        
        // Moved piece
        x += spacing;
        g2d.setColor(MOVED_PIECE_COLOR);
        g2d.fillRect(x, y, legendSize, legendSize);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Last Moved", x + legendSize + 5, y + 12);
        
        // Exit
        x += spacing;
        g2d.setColor(EXIT_COLOR);
        g2d.fillRect(x, y, legendSize, legendSize);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Exit", x + legendSize + 5, y + 12);
    }

    private Point getCellFromPoint(Point point) {
        if (game == null) return null;
        
        int rows = game.getRows();
        int cols = game.getCols();
        int boardWidth = cols * cellSize;
        int boardHeight = rows * cellSize;
        int boardX = (getWidth() - boardWidth) / 2;
        int boardY = boardMargin;
        
        int x = point.x - boardX;
        int y = point.y - boardY;
        
        if (x >= 0 && x < boardWidth && y >= 0 && y < boardHeight) {
            int col = x / cellSize;
            int row = y / cellSize;
            return new Point(row, col);
        }
        
        return null;
    }

    private void onCellClicked(int row, int col) {
        if (game == null) return;
        
        char[][] board = game.getBoard();
        if (row >= 0 && row < board.length && col >= 0 && col < board[0].length) {
            char cell = board[row][col];
            System.out.println("Clicked cell (" + row + ", " + col + "): " + cell);
        }
    }

    public int getCellSize() {
        return cellSize;
    }

    public Point getBoardOffset() {
        if (game == null) return new Point(0, 0);
        
        int cols = game.getCols();
        int boardWidth = cols * cellSize;
        int boardX = (getWidth() - boardWidth) / 2;
        int boardY = boardMargin;
        
        return new Point(boardX, boardY);
    }

    public void recalculateDimensions() {
        calculateDimensions();
        repaint();
    }
}