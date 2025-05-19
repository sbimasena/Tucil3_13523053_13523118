package src;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * GameAnimation handles the animated playback of Rush Hour puzzle solutions
 * It controls the timing and progression of moves during solution animation
 */
public class GameAnimation {
    private GamePanel gamePanel;
    private List<RushHourGame> solution;
    private List<String> actions;
    private int currentStep;
    private boolean isPlaying;
    private Timer animationTimer;
    private AnimationListener listener;
    
    // Animation settings
    private int animationSpeed; // Speed from 1 (slow) to 10 (fast)
    private static final int BASE_DELAY = 2000; // Base delay in milliseconds (2 seconds)
    private static final int MIN_DELAY = 200;   // Minimum delay (0.2 seconds)
    
    // Animation states
    private enum AnimationState {
        STOPPED, PLAYING, PAUSED, COMPLETED
    }
    
    private AnimationState currentState;
    
    /**
     * Constructor for GameAnimation
     * @param gamePanel The panel that displays the game board
     */
    public GameAnimation(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.currentStep = 0;
        this.isPlaying = false;
        this.animationSpeed = 5; // Default middle speed
        this.currentState = AnimationState.STOPPED;
        
        // Create animation timer
        initializeTimer();
    }
    
    /**
     * Initialize the animation timer
     */
    private void initializeTimer() {
        int delay = calculateDelay();
        animationTimer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentState == AnimationState.PLAYING) {
                    nextStep();
                }
            }
        });
    }
    
    /**
     * Set the solution to animate
     * @param solution List of game states representing the solution
     * @param actions List of actions taken to reach each state
     */
    public void setSolution(List<RushHourGame> solution, List<String> actions) {
        if (solution == null || solution.isEmpty()) {
            throw new IllegalArgumentException("Solution cannot be null or empty");
        }
        
        if (actions == null || actions.size() != solution.size() - 1) {
            throw new IllegalArgumentException("Actions list must have one less element than solution list");
        }
        
        this.solution = solution;
        this.actions = actions;
        reset();
    }
    
    /**
     * Start or resume animation playback
     */
    public void play() {
        if (solution == null || solution.isEmpty()) {
            return;
        }
        
        if (currentState != AnimationState.PLAYING && currentState != AnimationState.COMPLETED) {
            currentState = AnimationState.PLAYING;
            isPlaying = true;
            
            if (listener != null) {
                listener.onAnimationStarted();
            }
            
            // Update timer delay based on current speed
            animationTimer.setDelay(calculateDelay());
            animationTimer.start();
        }
    }
    
    /**
     * Pause animation playback
     */
    public void pause() {
        if (currentState == AnimationState.PLAYING) {
            currentState = AnimationState.PAUSED;
            isPlaying = false;
            animationTimer.stop();
        }
    }
    
    /**
     * Stop animation and reset to beginning
     */
    public void stop() {
        currentState = AnimationState.STOPPED;
        isPlaying = false;
        animationTimer.stop();
        reset();
    }
    
    /**
     * Step forward one move in the animation
     */
    public void stepForward() {
        if (solution == null || solution.isEmpty()) {
            return;
        }
        
        if (currentStep < solution.size() - 1) {
            currentStep++;
            updateDisplay();
            
            if (listener != null) {
                listener.onMoveChanged(currentStep, solution.size() - 1);
            }
            
            // Check if we've reached the end
            if (currentStep >= solution.size() - 1) {
                // Don't reset - just stop the timer and mark as completed
                currentState = AnimationState.COMPLETED;
                isPlaying = false;
                animationTimer.stop();
                
                if (listener != null) {
                    listener.onAnimationComplete();
                }
            }
        }
    }
    
    /**
     * Step backward one move in the animation
     */
    public void stepBackward() {
        if (solution == null || solution.isEmpty()) {
            return;
        }
        
        if (currentStep > 0) {
            currentStep--;
            
            // If we were completed and step back, we're no longer completed
            if (currentState == AnimationState.COMPLETED) {
                currentState = AnimationState.STOPPED;
            }
            
            updateDisplay();
            
            if (listener != null) {
                listener.onMoveChanged(currentStep, solution.size() - 1);
            }
        }
    }
    
    /**
     * Reset animation to the beginning
     */
    public void reset() {
        currentStep = 0;
        currentState = AnimationState.STOPPED;
        isPlaying = false;
        animationTimer.stop();
        
        updateDisplay();
        
        if (listener != null && solution != null) {
            listener.onMoveChanged(currentStep, solution.size() - 1);
        }
    }
    
    /**
     * Set the animation speed
     * @param speed Speed from 1 (slowest) to 10 (fastest)
     */
    public void setSpeed(int speed) {
        this.animationSpeed = Math.max(1, Math.min(10, speed));
        
        // Update timer delay if animation is running
        if (animationTimer != null) {
            animationTimer.setDelay(calculateDelay());
        }
    }
    
    /**
     * Get the current animation speed
     * @return Current speed (1-10)
     */
    public int getSpeed() {
        return animationSpeed;
    }
    
    /**
     * Set the animation listener for callbacks
     * @param listener The listener to receive animation events
     */
    public void setAnimationListener(AnimationListener listener) {
        this.listener = listener;
    }
    
    /**
     * Get the current step in the animation
     * @return Current step number (0-based)
     */
    public int getCurrentStep() {
        return currentStep;
    }
    
    /**
     * Get the total number of steps in the solution
     * @return Total steps, or 0 if no solution loaded
     */
    public int getTotalSteps() {
        return solution != null ? solution.size() - 1 : 0;
    }
    
    /**
     * Check if animation is currently playing
     * @return true if playing, false otherwise
     */
    public boolean isPlaying() {
        return isPlaying;
    }
    
    /**
     * Check if animation is paused
     * @return true if paused, false otherwise
     */
    public boolean isPaused() {
        return currentState == AnimationState.PAUSED;
    }
    
    /**
     * Check if animation is completed
     * @return true if animation has reached the final step, false otherwise
     */
    public boolean isCompleted() {
        return currentState == AnimationState.COMPLETED;
    }
    
    /**
     * Check if there is a next step available
     * @return true if can step forward, false otherwise
     */
    public boolean hasNextStep() {
        return solution != null && currentStep < solution.size() - 1;
    }
    
    /**
     * Check if there is a previous step available
     * @return true if can step backward, false otherwise
     */
    public boolean hasPreviousStep() {
        return solution != null && currentStep > 0;
    }
    
    /**
     * Get the current action being displayed
     * @return Action string or null if none
     */
    public String getCurrentAction() {
        if (actions != null && currentStep > 0 && currentStep <= actions.size()) {
            return actions.get(currentStep - 1);
        }
        return null;
    }
    
    /**
     * Jump to a specific step in the animation
     * @param step Step number to jump to (0-based)
     */
    public void jumpToStep(int step) {
        if (solution == null || solution.isEmpty()) {
            return;
        }
        
        step = Math.max(0, Math.min(step, solution.size() - 1));
        currentStep = step;
        
        // Update state based on the step we jumped to
        if (step >= solution.size() - 1) {
            currentState = AnimationState.COMPLETED;
            isPlaying = false;
            animationTimer.stop();
        } else if (currentState == AnimationState.COMPLETED) {
            currentState = AnimationState.STOPPED;
        }
        
        updateDisplay();
        
        if (listener != null) {
            listener.onMoveChanged(currentStep, solution.size() - 1);
        }
    }
    
    /**
     * Calculate animation delay based on speed setting
     * @return Delay in milliseconds
     */
    private int calculateDelay() {
        // Convert speed (1-10) to delay (2000-200 ms)
        // Speed 1 = 2000ms, Speed 10 = 200ms
        int delay = BASE_DELAY - ((animationSpeed - 1) * (BASE_DELAY - MIN_DELAY) / 9);
        return Math.max(MIN_DELAY, delay);
    }
    
    /**
     * Move to the next step in the animation
     */
    private void nextStep() {
        if (hasNextStep()) {
            stepForward();
        } else {
            // Animation complete - don't reset, just stop and mark as completed
            currentState = AnimationState.COMPLETED;
            isPlaying = false;
            animationTimer.stop();
            
            if (listener != null) {
                listener.onAnimationComplete();
            }
        }
    }
    
    /**
     * Update the game panel display with current state
     */
    private void updateDisplay() {
        if (solution == null || solution.isEmpty()) {
            return;
        }
        
        // Set the current game state
        RushHourGame currentGameState = solution.get(currentStep);
        gamePanel.setGame(currentGameState);
        
        // Highlight the piece that moved (if any)
        if (currentStep > 0 && actions != null && currentStep <= actions.size()) {
            String action = actions.get(currentStep - 1);
            gamePanel.setLastAction(action);
        } else {
            gamePanel.setLastAction(null);
        }
        
        // Force repaint
        gamePanel.repaint();
    }
    
    /**
     * Get animation information as a string
     * @return String containing current animation status
     */
    public String getAnimationInfo() {
        if (solution == null) {
            return "No solution loaded";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Step: ").append(currentStep).append("/").append(solution.size() - 1);
        info.append(" | State: ").append(currentState);
        info.append(" | Speed: ").append(animationSpeed).append("/10");
        
        if (currentStep > 0 && actions != null && currentStep <= actions.size()) {
            info.append(" | Last Action: ").append(actions.get(currentStep - 1));
        }
        
        return info.toString();
    }
    
    /**
     * Create a simple text-based progress bar
     * @param width Width of the progress bar in characters
     * @return Progress bar string
     */
    public String getProgressBar(int width) {
        if (solution == null || solution.isEmpty()) {
            return "[" + " ".repeat(width) + "]";
        }
        
        int progress = (int) ((double) currentStep / (solution.size() - 1) * width);
        progress = Math.max(0, Math.min(progress, width));
        
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            if (i < progress) {
                bar.append("=");
            } else if (i == progress) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }
        bar.append("]");
        
        return bar.toString();
    }
    
    /**
     * Get detailed step information
     * @return Step details including board state and action
     */
    public String getStepDetails() {
        if (solution == null || currentStep >= solution.size()) {
            return "No step information available";
        }
        
        StringBuilder details = new StringBuilder();
        details.append("Step ").append(currentStep).append(":\n");
        
        if (currentStep == 0) {
            details.append("Initial state");
        } else {
            details.append("Action: ").append(actions.get(currentStep - 1));
        }
        
        return details.toString();
    }
    
    /**
     * Dispose of resources used by the animation
     */
    public void dispose() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
        currentState = AnimationState.STOPPED;
        isPlaying = false;
    }
}