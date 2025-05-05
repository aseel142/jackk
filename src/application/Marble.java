package application;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Marble extends Circle {
    
    private String player;
    private int position;
    
    // Separate position arrays for each player
    private static final double[][] PLAYER1_POSITIONS = {
        {542, 155},  // Position 1
        {584, 155},  // Position 2
        {584, 184},  // Position 3
        {542, 184}   // Position 4
    };
    
    private static final double[][] PLAYER2_POSITIONS = {
        {499, 400},  // Position 1
        {540, 400},  // Position 2
        {540, 428},  // Position 3
        {499, 428}   // Position 4
    };
    
    private static final double[][] PLAYER3_POSITIONS = {
        {223, 152},  // Position 1
        {268, 152},  // Position 2
        {223, 181},  // Position 3
        {268, 181}   // Position 4
    };
    
    private static final double[][] PLAYER4_POSITIONS = {
        {199, 347},  // Position 1
        {245, 346},  // Position 2
        {200, 376},  // Position 3
        {245, 376}   // Position 4
    };
    
    // Color mapping for different players
    private static Color getColorForPlayer(String player) {
        switch (player.toLowerCase()) {
            case "player1":
                return Color.BLACK;
            case "player2":
                return Color.RED;
            case "player3":
                return Color.BLUE;
            case "player4":
                return Color.GREEN;
            default:
                return Color.RED; // Default color
        }
    }
    
    public Marble(String player, int position) {
        super();
        this.player = player;
        this.position = position;
        
        // Set the size and appearance
        setRadius(10);
        setFill(getColorForPlayer(player));
        setStroke(Color.BLACK);
        setStrokeWidth(1);
        
        // Get the correct position array based on player
        double[][] positions = getPositionsForPlayer(player);
        
        // Set the position (position parameter is 1-based, array is 0-based)
        if (position >= 1 && position <= positions.length) {
            double[] pos = positions[position - 1];
            setCenterX(pos[0]);
            setCenterY(pos[1]);
        } else {
            throw new IllegalArgumentException("Invalid position: " + position + ". Must be between 1 and " + positions.length);
        }
    }
    
    private static double[][] getPositionsForPlayer(String player) {
        switch (player.toLowerCase()) {
            case "player1":
                return PLAYER1_POSITIONS;
            case "player2":
                return PLAYER2_POSITIONS;
            case "player3":
                return PLAYER3_POSITIONS;
            case "player4":
                return PLAYER4_POSITIONS;
            default:
                return PLAYER2_POSITIONS; // Default to player2 positions
        }
    }
    
    // Getter methods
    public String getPlayer() {
        return player;
    }
    
    public int getPosition() {
        return position;
    }
    
    // Method to move marble to a new position
    public void moveToPosition(int newPosition) {
        double[][] positions = getPositionsForPlayer(player);
        if (newPosition >= 1 && newPosition <= positions.length) {
            this.position = newPosition;
            double[] pos = positions[newPosition - 1];
            setCenterX(pos[0]);
            setCenterY(pos[1]);
        }
    }
}