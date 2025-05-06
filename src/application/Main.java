package application;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.TranslateTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Interpolator;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.Map;

public class Main extends Application {

    private Stage primaryStage;
    private Scene menuScene;
    private Scene phase1Scene;
    private NormalPlayer player1;
    private NormalPlayer player2;
    private NormalPlayer player3;
    private NormalPlayer player4;
    
    // Map to track marble positions
    private Map<Marble, Integer> marblePositions = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        CardManager.preloadCardImages();

        // ---- 1) Main menu with "Phase 1" button ----
        Button phase1Btn = new Button("Phase 1");
        phase1Btn.setOnAction(e -> {
            showPhase1Scene();
            
            // Move the first card of player 1 to the center AFTER clicking the button
            Card firstCardOfPlayer1 = player1.getCards().get(0);
            moveToCenter(firstCardOfPlayer1, 2, 1);
            
            Marble firstMarbleOfPlayer1 = player1.getMarbles().get(0);
            moveMarbleToPosition(firstMarbleOfPlayer1, 65, 1, 0);
            
            Marble secondMarbleOfPlayer1 = player1.getMarbles().get(1);
            moveMarbleToPosition(secondMarbleOfPlayer1, 3, 1, 2);
            
            Card thirdCardOfPlayer2 = player2.getCards().get(2);  // Index 2 is the third card
            moveToCenter(thirdCardOfPlayer2, 2, 3);
            
            Card fourthCardOfPlayer3 = player3.getCards().get(3);  // Index 3 is the fourth card
            moveToCenter(fourthCardOfPlayer3, 2, 5);
        });

        VBox menuRoot = new VBox(20, phase1Btn);
        menuRoot.setAlignment(Pos.CENTER);
        menuScene = new Scene(menuRoot, 800, 600);

        // ---- 2) Phase 1 scene with a full-window board background ----
        Image boardImage = new Image(
            getClass().getResourceAsStream("/images/boardd.png")
        );

        ImageView boardView = new ImageView(boardImage);
        boardView.setPreserveRatio(false);

        // Create players with their cards
        player1 = new NormalPlayer(
            "player1",
            new Card(Card.Suit.DIAMONDS, Card.Value.EIGHT),
            new Card(Card.Suit.HEARTS, Card.Value.SEVEN),
            new Card(Card.Suit.HEARTS, Card.Value.SIX),
            new Card(Card.Suit.HEARTS, Card.Value.TWO)
        );

        player2 = new NormalPlayer(
            "player2",
            new Card(Card.Suit.DIAMONDS, Card.Value.SIX),
            new Card(Card.Suit.CLUBS, Card.Value.SIX),
            new Card(Card.Suit.CLUBS, Card.Value.EIGHT),
            new Card(Card.Suit.CLUBS, Card.Value.SEVEN)
        );

        player3 = new NormalPlayer(
            "player3",
            new Card(Card.Suit.SPADES, Card.Value.ACE),
            new Card(Card.Suit.SPADES, Card.Value.TWO),
            new Card(Card.Suit.SPADES, Card.Value.THREE),
            new Card(Card.Suit.SPADES, Card.Value.FOUR)
        );

        player4 = new NormalPlayer(
            "player4",
            new Card(Card.Suit.SPADES, Card.Value.FIVE),
            new Card(Card.Suit.SPADES, Card.Value.SIX),
            new Card(Card.Suit.SPADES, Card.Value.SEVEN),
            new Card(Card.Suit.SPADES, Card.Value.EIGHT)
        );

        // Create pane with board background
        Pane phase1Root = new Pane(boardView);

        // Add all players to the pane
        player1.addToPane(phase1Root);
        player2.addToPane(phase1Root);
        player3.addToPane(phase1Root);
        player4.addToPane(phase1Root);
        
        // Bind the background image to always fill the pane
        boardView.fitWidthProperty().bind(phase1Root.widthProperty());
        boardView.fitHeightProperty().bind(phase1Root.heightProperty());

        phase1Scene = new Scene(phase1Root, 800, 600);

        // ---- 3) Show the menu ----
        primaryStage.setTitle("Jackaroo Simulator");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    /**
     * Moves a marble to a specified board position with animation
     * @param marble The marble to move
     * @param position The target position number (1-67)
     * @param durationSeconds Animation duration in seconds
     * @param delaySeconds Delay before animation starts in seconds
     */
    private void moveMarbleToPosition(Marble marble, int position, double durationSeconds, double delaySeconds) {
        // Get coordinates from BoardPositions
        double targetX = BoardPositions.getX(position);
        double targetY = BoardPositions.getY(position);
        
        // Create animation
        Timeline timeline = new Timeline();
        
        // Define the keyframes for smooth diagonal movement
        KeyFrame start = new KeyFrame(Duration.ZERO, 
            new KeyValue(marble.centerXProperty(), marble.getCenterX()),
            new KeyValue(marble.centerYProperty(), marble.getCenterY()));
        
        KeyFrame end = new KeyFrame(Duration.seconds(durationSeconds),
            new KeyValue(marble.centerXProperty(), targetX, Interpolator.EASE_BOTH),
            new KeyValue(marble.centerYProperty(), targetY, Interpolator.EASE_BOTH));
        
        timeline.getKeyFrames().addAll(start, end);
        timeline.setDelay(Duration.seconds(delaySeconds));
        
        // Update the marble's position in our tracking map when animation completes
        timeline.setOnFinished(event -> {
            setMarblePosition(marble, position);
        });
        
        // Play the animation
        timeline.play();
    }
    
    /**
     * Moves a marble the specified number of steps along its designated path
     * @param player The player number (1-4)
     * @param marbleIndex The index of the marble (0-3)
     * @param steps The number of steps to move
     * @param durationSeconds Animation duration in seconds
     * @param delaySeconds Delay before animation starts in seconds
     */
    private void moveMarbleSteps(int player, int marbleIndex, int steps, double durationSeconds, double delaySeconds) {
        NormalPlayer currentPlayer;
        
        // Get the player object
        switch (player) {
            case 1: currentPlayer = player1; break;
            case 2: currentPlayer = player2; break;
            case 3: currentPlayer = player3; break;
            case 4: currentPlayer = player4; break;
            default: return; // Invalid player number
        }
        
        // Get the marble to move
        Marble marble = currentPlayer.getMarbles().get(marbleIndex);
        
        // Calculate current position and target position
        int currentPosition = getCurrentMarblePosition(marble);
        int targetPosition = calculateTargetPosition(player, currentPosition, steps);
        
        System.out.println("Moving player " + player + "'s marble from position " + 
                           currentPosition + " to position " + targetPosition);
        
        // Move the marble to the target position
        moveMarbleToPosition(marble, targetPosition, durationSeconds, delaySeconds);
    }

    /**
     * Calculates the target position after moving a specified number of steps
     */
    private int calculateTargetPosition(int player, int currentPosition, int steps) {
        // If already in safe zone, continue moving in safe zone until steps are exhausted
        if (isInSafeZone(player, currentPosition)) {
            int position = currentPosition;
            for (int i = 0; i < steps; i++) {
                position++;
                // If reached end of safe zone, stop moving
                if ((player == 1 && position > 49) ||
                    (player == 2 && position > 66) ||
                    (player == 3 && position > 16) ||
                    (player == 4 && position > 33)) {
                    position--; // Move back to last valid position
                    break;
                }
            }
            return position;
        }
        
        int position = currentPosition;
        int stepsRemaining = steps;
        
        while (stepsRemaining > 0) {
            // Move one position forward
            position++;
            
            // Handle wrapping around the board
            if (position > 67) {
                position = 1;
            }
            
            // Check if we're entering a safe zone
            if (isInSafeZone(player, position)) {
                stepsRemaining--;
                
                // Continue moving within own safe zone
                while (stepsRemaining > 0) {
                    position++;
                    stepsRemaining--;
                    
                    // If reached end of safe zone, stop moving
                    if ((player == 1 && position > 49) ||
                        (player == 2 && position > 66) ||
                        (player == 3 && position > 16) ||
                        (player == 4 && position > 33)) {
                        position--; // Move back to last valid position
                        break;
                    }
                }
                
                break; // Exit loop as we're done moving
            }
            
            // Check if we need to skip other players' safe zones
            if (isOtherPlayerSafeZone(player, position)) {
                // Skip to the position after the safe zone
                if (position >= 13 && position <= 16) { // Player 3's safe zone
                    position = 17; // Skip to position after safe zone
                } else if (position >= 30 && position <= 33) { // Player 4's safe zone
                    position = 34; // Skip to position after safe zone
                } else if (position >= 46 && position <= 49) { // Player 1's safe zone
                    position = 50; // Skip to position after safe zone
                } else if (position >= 63 && position <= 66) { // Player 2's safe zone
                    position = 67; // Skip to position after safe zone
                }
                
                // Count skipping a safe zone as one step
                stepsRemaining--;
            } else {
                // Regular movement
                stepsRemaining--;
            }
        }
        
        return position;
    }

    /**
     * Checks if a position is in a player's own safe zone
     */
    private boolean isInSafeZone(int player, int position) {
        switch (player) {
            case 1: return position >= 46 && position <= 49;
            case 2: return position >= 63 && position <= 66;
            case 3: return position >= 13 && position <= 16;
            case 4: return position >= 30 && position <= 33;
            default: return false;
        }
    }

    /**
     * Checks if a position is in another player's safe zone
     */
    private boolean isOtherPlayerSafeZone(int player, int position) {
        switch (player) {
            case 1: return (position >= 63 && position <= 66) || 
                          (position >= 13 && position <= 16) || 
                          (position >= 30 && position <= 33);
                          
            case 2: return (position >= 46 && position <= 49) || 
                          (position >= 13 && position <= 16) || 
                          (position >= 30 && position <= 33);
                          
            case 3: return (position >= 46 && position <= 49) || 
                          (position >= 63 && position <= 66) || 
                          (position >= 30 && position <= 33);
                          
            case 4: return (position >= 46 && position <= 49) || 
                          (position >= 63 && position <= 66) || 
                          (position >= 13 && position <= 16);
                          
            default: return false;
        }
    }

    /**
     * Get the current position of a marble
     */
    private int getCurrentMarblePosition(Marble marble) {
        Integer position = marblePositions.get(marble);
        return position != null ? position : 1; // Default to position 1 if not set
    }

    /**
     * Update the marble's position in the tracking map
     */
    private void setMarblePosition(Marble marble, int position) {
        marblePositions.put(marble, position);
        System.out.println("Marble moved to position: " + position);
    }

    /**
     * Moves a card to the center of the board with animation
     * @param card The card to move
     * @param durationSeconds The duration of the animation in seconds
     * @param delaySeconds The delay before the animation starts in seconds
     */
    private void moveToCenter(Card card, double durationSeconds, double delaySeconds) {
        ImageView cardView = card.getCardImageView();
        
        // Center coordinates
        double centerX = 360;
        double centerY = 240;
        
        // Create animation
        TranslateTransition moveAnimation = new TranslateTransition(
            Duration.seconds(durationSeconds), cardView);
        
        // Add delay before animation starts
        moveAnimation.setDelay(Duration.seconds(delaySeconds));
        
        // Calculate translation distances
        double startX = cardView.getX();
        double startY = cardView.getY();
        moveAnimation.setByX(centerX - startX);
        moveAnimation.setByY(centerY - startY);
        
        // Update actual position when animation completes
        moveAnimation.setOnFinished(event -> {
            cardView.setX(centerX);
            cardView.setY(centerY);
            cardView.setTranslateX(0);
            cardView.setTranslateY(0);
        });
        
        // Start the animation
        moveAnimation.play();
    }

    private void showPhase1Scene() {
        primaryStage.setScene(phase1Scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}