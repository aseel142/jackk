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
    private Pane phase1Root;
    private Player player1;
    private Player player2;
    private Player player3;
    private Player player4;
    private Board gameBoard;
    
    // Map to track marble positions
    private Map<Marble, Integer> marblePositions = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        CardManager.preloadCardImages();

        // ---- 1) Main menu with Phase 0 and Phase 1 buttons ----
        Button phase0Btn = new Button("Phase 0 - Start Game");
        phase0Btn.setOnAction(e -> startPhase0());
        
        Button phase1Btn = new Button("Phase 1");
        phase1Btn.setOnAction(e -> showPhase1Scene());

        VBox menuRoot = new VBox(20, phase0Btn, phase1Btn);
        menuRoot.setAlignment(Pos.CENTER);
        menuScene = new Scene(menuRoot, 800, 600);

        // ---- 2) Phase 1 scene with a full-window board background ----
        Image boardImage = new Image(
            getClass().getResourceAsStream("/images/boardd.png")
        );

        ImageView boardView = new ImageView(boardImage);
        boardView.setPreserveRatio(false);

        // Create pane with board background
        phase1Root = new Pane(boardView);
        
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
     * Start Phase 0 - creates a game with 3 Beginner players and 1 Intermediate
     */
    private void startPhase0() {
        // Clear any existing content in the game pane
        phase1Root.getChildren().clear();
        
        // Add the board background
        Image boardImage = new Image(
            getClass().getResourceAsStream("/images/boardd.png")
        );
        ImageView boardView = new ImageView(boardImage);
        boardView.setPreserveRatio(false);
        boardView.fitWidthProperty().bind(phase1Root.widthProperty());
        boardView.fitHeightProperty().bind(phase1Root.heightProperty());
        phase1Root.getChildren().add(boardView);
        
        // Create players - 3 Beginners and 1 Intermediate
        
        player1 = new NormalPlayer("player1");
        player2 = new IntermediatePlayer("player2");
        player3 = new NormalPlayer("player3");
        player4 = new NormalPlayer("player4"); 
        
        // Create and initialize the game board
        gameBoard = new Board(phase1Root, player1, player2, player3, player4);
        gameBoard.initializeGame();
        
        // Show the game scene
        showPhase1Scene();
        
        System.out.println("Game started with 3 Beginner players and 1 Intermediate player!");
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
     * Moves a card to the center of the board with animation
     * @param card The card to move
     * @param durationSeconds The duration of the animation in seconds
     * @param delaySeconds The delay before the animation starts in seconds
     */
    /**
     * Move a card to center, then run onFinished.
     */
    private void moveToCenter(Card card, double duration, double delay, Runnable onFinished) {
        ImageView view = card.getCardImageView();
        double centerX = 360, centerY = 240;
        double startX = view.getX(), startY = view.getY();

        TranslateTransition tt = new TranslateTransition(Duration.seconds(duration), view);
        tt.setDelay(Duration.seconds(delay));
        tt.setByX(centerX - startX);
        tt.setByY(centerY - startY);
        tt.setOnFinished(e -> {
            // finalize position
            view.setX(centerX);
            view.setY(centerY);
            view.setTranslateX(0);
            view.setTranslateY(0);
            // then invoke the next step
            onFinished.run();
        });
        tt.play();
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

    private void showPhase1Scene() {
        primaryStage.setScene(phase1Scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}