package src;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.UnsupportedLookAndFeelException;


public class RushHourGUI extends JFrame {
    // Main components
    private RushHourGame currentGame;
    private List<RushHourGame> currentSolution;
    private List<String> currentActions;
    private SearchAlgorithm currentAlgorithm;

    // GUI Components
    private GamePanel gamePanel;
    private GameAnimation gameAnimation;

    // Control Panel Components
    private JComboBox<String> algorithmComboBox;
    private JComboBox<String> heuristicComboBox;
    private JButton loadButton;
    private JButton solveButton;
    private JButton resetButton;
    private JButton saveButton; // New Save button
    private JLabel statusLabel;

    // Animation Control Components
    private JButton playButton;
    private JButton pauseButton;
    private JButton stepButton;
    private JButton stepBackButton;
    private JSlider speedSlider;
    private JLabel moveLabel;

    // Results Panel Components
    private JTextArea resultsTextArea;
    private JLabel nodesVisitedLabel;
    private JLabel executionTimeLabel;
    private JLabel solutionLengthLabel;

    // Store panels as instance variables
    private JPanel controlPanel;
    private JPanel animationPanel;
    private JPanel resultsPanel;

    // Layout constants
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;
    private static final int GAME_PANEL_SIZE = 600;

    public RushHourGUI() {
        initializeGUI();
        setupEventHandlers();
    }


    private void initializeGUI() {
        setTitle("Rush Hour Puzzle Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);

        // Set layout first
        setLayout(new BorderLayout());

        // Create main panels
        createGamePanel();
        createControlPanel();
        createAnimationControlPanel();
        createResultsPanel();

        // Setup main layout - add all panels
        setupMainLayout();

        // Force validation
        validate();

        // Initialize with disabled state
        updateGUIState(false);
    }


    private void createGamePanel() {
        gamePanel = new GamePanel();
        gamePanel.setPreferredSize(new Dimension(GAME_PANEL_SIZE, GAME_PANEL_SIZE));
        gamePanel.setBorder(BorderFactory.createTitledBorder("Rush Hour Board"));

        gameAnimation = new GameAnimation(gamePanel);
    }

    private void createControlPanel() {
        controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Algorithm selection
        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Algorithm:"), gbc);

        gbc.gridx = 1;
        algorithmComboBox = new JComboBox<>(new String[]{
                "UCS (Uniform Cost Search)",
                "GBFS (Greedy Best First Search)",
                "A* (A-Star)",
                "IDA* (Iterative Deepening A*)"
        });
        controlPanel.add(algorithmComboBox, gbc);

        // Heuristic selection
        gbc.gridx = 0; gbc.gridy = 1;
        controlPanel.add(new JLabel("Heuristic:"), gbc);

        gbc.gridx = 1;
        heuristicComboBox = new JComboBox<>(new String[]{
                "H1 - Manhattan Distance",
                "H2 - Blocking Pieces + Distance"
        });
        heuristicComboBox.setEnabled(false);
        controlPanel.add(heuristicComboBox, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 2;
        loadButton = new JButton("Load Puzzle");
        System.out.println("Load button created");
        controlPanel.add(loadButton, gbc);

        gbc.gridx = 1;
        solveButton = new JButton("Solve Puzzle");
        solveButton.setEnabled(false);
        controlPanel.add(solveButton, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        resetButton = new JButton("Reset");
        controlPanel.add(resetButton, gbc);
        
        // Add the new Save Solution button
        gbc.gridx = 1; gbc.gridy = 3;
        saveButton = new JButton("Save Solution");
        saveButton.setEnabled(false); // Initially disabled
        controlPanel.add(saveButton, gbc);

        // Status label
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        statusLabel = new JLabel("Ready - Load a puzzle to begin");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        controlPanel.add(statusLabel, gbc);
    }

    private void createAnimationControlPanel() {
        animationPanel = new JPanel(new FlowLayout());
        animationPanel.setBorder(BorderFactory.createTitledBorder("Animation Controls"));

        // Control buttons
        playButton = new JButton("▶ Play");
        pauseButton = new JButton("⏸ Pause");
        stepBackButton = new JButton("⏮ Step Back");
        stepButton = new JButton("⏭ Step Forward");

        // Speed control
        speedSlider = new JSlider(1, 10, 5);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setMajorTickSpacing(5);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);

        // Move information
        moveLabel = new JLabel("Move: 0/0");

        // Add components
        animationPanel.add(stepBackButton);
        animationPanel.add(playButton);
        animationPanel.add(pauseButton);
        animationPanel.add(stepButton);
        animationPanel.add(new JLabel("Speed:"));
        animationPanel.add(speedSlider);
        animationPanel.add(moveLabel);

        // Initially disable all animation controls
        setAnimationControlsEnabled(false);
    }

    private void createResultsPanel() {
        resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Results"));

        // Statistics labels
        JPanel statsPanel = new JPanel(new GridLayout(3, 1));
        nodesVisitedLabel = new JLabel("Nodes Visited: -");
        executionTimeLabel = new JLabel("Execution Time: -");
        solutionLengthLabel = new JLabel("Solution Length: -");

        statsPanel.add(nodesVisitedLabel);
        statsPanel.add(executionTimeLabel);
        statsPanel.add(solutionLengthLabel);

        // Results text area
        resultsTextArea = new JTextArea(5, 20);
        resultsTextArea.setEditable(false);
        resultsTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultsTextArea);

        resultsPanel.add(statsPanel, BorderLayout.NORTH);
        resultsPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void setupMainLayout() {
        // Create center panel for game display
        JPanel centerPanel = new JPanel(new FlowLayout());
        centerPanel.add(gamePanel);

        // Add all panels to the main frame
        add(centerPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.WEST);
        add(animationPanel, BorderLayout.SOUTH);
        add(resultsPanel, BorderLayout.EAST);

        System.out.println("Layout setup complete");
    }

    private void setupEventHandlers() {
        // Algorithm selection handler
        algorithmComboBox.addActionListener(e -> {
            int selected = algorithmComboBox.getSelectedIndex();
            // UCS doesn't use heuristics, others do
            heuristicComboBox.setEnabled(selected != 0);
        });

        // Load button handler
        loadButton.addActionListener(e -> loadPuzzle());

        // Solve button handler
        solveButton.addActionListener(e -> solvePuzzle());

        // Reset button handler
        resetButton.addActionListener(e -> resetGame());
        
        // Save button handler
        saveButton.addActionListener(e -> saveSolution());

        // Animation control handlers
        playButton.addActionListener(e -> gameAnimation.play());
        pauseButton.addActionListener(e -> gameAnimation.pause());
        stepButton.addActionListener(e -> gameAnimation.stepForward());
        stepBackButton.addActionListener(e -> gameAnimation.stepBackward());

        // Speed slider handler
        speedSlider.addChangeListener(e -> {
            int speed = speedSlider.getValue();
            gameAnimation.setSpeed(speed);
        });

        // Updated animation event handler with better handling of the last step
        gameAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onMoveChanged(int currentMove, int totalMoves) {
                moveLabel.setText("Move: " + currentMove + "/" + totalMoves);

                // Update results text area with current move
                if (currentActions != null && currentMove > 0 && currentMove <= currentActions.size()) {
                    String action = currentActions.get(currentMove - 1);
                    resultsTextArea.append("Move " + currentMove + ": " + action + "\n");
                    resultsTextArea.setCaretPosition(resultsTextArea.getDocument().getLength());
                }

                // Special handling for the final move
                if (currentMove == totalMoves) {
                    statusLabel.setText("Solution complete! P has reached the exit.");
                } else if (currentMove == 0) {
                    statusLabel.setText("Initial state");
                } else {
                    statusLabel.setText("Playing animation... Step " + currentMove + "/" + totalMoves);
                }
            }

            @Override
            public void onAnimationComplete() {
                setAnimationControlsEnabled(true);
                // Give user time to see the final state
                statusLabel.setText("Animation complete - Solution shown. Use Reset to return to start.");
            }

            @Override
            public void onAnimationStarted() {
                statusLabel.setText("Playing animation...");
            }
        });
    }

    private void loadPuzzle() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
        fileChooser.setCurrentDirectory(new File("."));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                currentGame = RushHourIO.loadGameFromFile(selectedFile.getAbsolutePath());
                gamePanel.setGame(currentGame);
                gamePanel.repaint();

                // Reset solution-related data
                currentSolution = null;
                currentActions = null;
                currentAlgorithm = null;

                updateGUIState(true);
                statusLabel.setText("Puzzle loaded: " + selectedFile.getName());

                // Clear previous results
                clearResults();
                
                // Disable the save button since we don't have a solution yet
                saveButton.setEnabled(false);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading puzzle:\n" + ex.getMessage(),
                        "Load Error",
                        JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Error loading puzzle");
            }
        }
    }

    private void solvePuzzle() {
        if (currentGame == null) {
            JOptionPane.showMessageDialog(this,
                    "Please load a puzzle first!",
                    "No Puzzle",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get selected algorithm and heuristic
        int algorithmIndex = algorithmComboBox.getSelectedIndex();
        int heuristicIndex = heuristicComboBox.getSelectedIndex() + 1;

        // Create algorithm instance
        currentAlgorithm = createAlgorithm(algorithmIndex, heuristicIndex);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                statusLabel.setText("Solving puzzle...");
                solveButton.setEnabled(false);
                saveButton.setEnabled(false); // Disable save button during solving

                // Create a copy of the game for solving
                RushHourGame gameCopy = new RushHourGame(currentGame);
                currentSolution = currentAlgorithm.solve(gameCopy);
                currentActions = currentAlgorithm.getSolutionActions();

                if (currentSolution != null && !currentSolution.isEmpty()) {
                    List<RushHourGame> solutionWithExit = new ArrayList<>(currentSolution);
                    List<String> actionsWithExit = new ArrayList<>(currentActions);
                    RushHourGame finalState = new RushHourGame(solutionWithExit.get(solutionWithExit.size() - 1));
                    finalState.removePrimaryPiece();
                    solutionWithExit.add(finalState);
                    actionsWithExit.add("EXIT");
                    currentSolution = solutionWithExit;
                    currentActions = actionsWithExit;
                }

                return null;
            }

            @Override
            protected void done() {
                solveButton.setEnabled(true);

                if (currentSolution != null && !currentSolution.isEmpty()) {
                    displayResults();
                    setupAnimation();
                    statusLabel.setText("Solution found! Ready to animate.");
                    saveButton.setEnabled(true);
                } else {
                    statusLabel.setText("No solution found.");
                    saveButton.setEnabled(false); 
                    
                    JOptionPane.showMessageDialog(RushHourGUI.this,
                            "No solution found for this puzzle.",
                            "No Solution",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void saveSolution() {
        if (currentSolution == null || currentSolution.isEmpty() || currentAlgorithm == null) {
            JOptionPane.showMessageDialog(this,
                    "No solution available to save!",
                    "No Solution",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
        fileChooser.setCurrentDirectory(new File("."));
        
        // Generate a default filename based on algorithm
        String defaultFilename = RushHourIO.generateOutputFilename("puzzle", currentAlgorithm);
        fileChooser.setSelectedFile(new File(defaultFilename));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Add .txt extension if not present
            String filePath = selectedFile.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".txt")) {
                filePath += ".txt";
                selectedFile = new File(filePath);
            }
            
            // Check if file exists and confirm overwrite
            if (selectedFile.exists()) {
                int response = JOptionPane.showConfirmDialog(this,
                        "File already exists. Overwrite?",
                        "Confirm Overwrite",
                        JOptionPane.YES_NO_OPTION);
                if (response != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            try {
                RushHourIO.writeSolutionToFile(currentSolution, currentActions, currentAlgorithm, filePath);
                statusLabel.setText("Solution saved to: " + selectedFile.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving solution:\n" + ex.getMessage(),
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Error saving solution");
            }
        }
    }

    private SearchAlgorithm createAlgorithm(int algorithmIndex, int heuristicIndex) {
        switch (algorithmIndex) {
            case 0:
                return new UCS();
            case 1:
                return new GBFS(heuristicIndex);
            case 2:
                return new AStar(heuristicIndex);
            case 3:
                return new IDAStar(heuristicIndex);
            default:
                return new UCS();
        }
    }

    private void displayResults() {
        if (currentAlgorithm == null) return;

        // Update statistics labels
        nodesVisitedLabel.setText("Nodes Visited: " + currentAlgorithm.getNodesVisited());
        executionTimeLabel.setText("Execution Time: " + currentAlgorithm.getExecutionTime() + " ms");
        solutionLengthLabel.setText("Solution Length: " + (currentSolution.size() - 1) + " moves");

        // Display algorithm information in text area
        resultsTextArea.setText("");
        resultsTextArea.append("Algorithm: " + currentAlgorithm.getAlgorithmName() + "\n");
        resultsTextArea.append("Description: " + currentAlgorithm.getAlgorithmDescription() + "\n\n");
        resultsTextArea.append("Solution Steps:\n");
    }


    private void setupAnimation() {
        if (currentSolution != null && currentActions != null) {
            gameAnimation.setSolution(currentSolution, currentActions);
            setAnimationControlsEnabled(true);
            moveLabel.setText("Move: 0/" + (currentSolution.size() - 1));
        }
    }

 
    private void resetGame() {
        if (currentGame != null) {
            gamePanel.setGame(currentGame);
            gamePanel.repaint();

            if (gameAnimation != null) {
                gameAnimation.reset();
                moveLabel.setText("Move: 0/" + (currentSolution != null ? currentSolution.size() - 1 : 0));
            }

            // Clear only the solution steps from results, keep the statistics
            if (currentAlgorithm != null) {
                resultsTextArea.setText("");
                resultsTextArea.append("Algorithm: " + currentAlgorithm.getAlgorithmName() + "\n");
                resultsTextArea.append("Description: " + currentAlgorithm.getAlgorithmDescription() + "\n\n");
                resultsTextArea.append("Solution Steps:\n");
            }
            
            statusLabel.setText("Game reset to initial state");
        }
    }

    private void clearResults() {
        nodesVisitedLabel.setText("Nodes Visited: -");
        executionTimeLabel.setText("Execution Time: -");
        solutionLengthLabel.setText("Solution Length: -");
        resultsTextArea.setText("");
        moveLabel.setText("Move: 0/0");
        setAnimationControlsEnabled(false);
    }

    private void updateGUIState(boolean puzzleLoaded) {
        solveButton.setEnabled(puzzleLoaded);
        resetButton.setEnabled(puzzleLoaded);
        // Save button should only be enabled when there's a solution
        saveButton.setEnabled(puzzleLoaded && currentSolution != null && !currentSolution.isEmpty());
    }

    private void setAnimationControlsEnabled(boolean enabled) {
        playButton.setEnabled(enabled);
        pauseButton.setEnabled(enabled);
        stepButton.setEnabled(enabled);
        stepBackButton.setEnabled(enabled);
        speedSlider.setEnabled(enabled);
    }

 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            RushHourGUI gui = new RushHourGUI();
            gui.setVisible(true);

            // Additional debug
            System.out.println("GUI visible");
        });
    }
}

interface AnimationListener {
    void onMoveChanged(int currentMove, int totalMoves);
    void onAnimationComplete();
    void onAnimationStarted();
}