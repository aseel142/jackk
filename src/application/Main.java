package application;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
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
    private Scene gameScene;
    private Scene upgradeScene;
    private Pane gameRoot;
    private Player player1;
    private Player player2;
    private Player player3;
    private Player player4;
    private Board gameBoard;
    
    // Game states for tracking progression
    private int currentGameNumber = 0;
    private boolean waitingForNextGame = false;
    
    // Map to track marble positions
    private Map<Marble, Integer> marblePositions = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        CardManager.preloadCardImages();

        // ---- 1) Main menu with Phase 0 and Phase 1 buttons ----
        Button phase0Btn = new Button("Phase 1 - Start Game");
        phase0Btn.setPrefWidth(200);
        phase0Btn.setOnAction(e -> startPhase0());
        
        Button phase1Btn = new Button("Phase 2 - Learning Game");
        phase1Btn.setPrefWidth(200);
        phase1Btn.setOnAction(e -> startLearningGame());

        Label titleLabel = new Label("Jackaroo Game");
        titleLabel.setFont(new Font("Arial", 24));
        
        VBox menuRoot = new VBox(30, titleLabel, phase0Btn, phase1Btn);
        menuRoot.setAlignment(Pos.CENTER);
        menuScene = new Scene(menuRoot, 800, 600);

        // ---- 2) Game scene with a full-window board background ----
        Image boardImage = new Image(
            getClass().getResourceAsStream("/images/boardd.png")
        );

        ImageView boardView = new ImageView(boardImage);
        boardView.setPreserveRatio(false);

        // Create pane with board background
        gameRoot = new Pane(boardView);
        
        // Bind the background image to always fill the pane
        boardView.fitWidthProperty().bind(gameRoot.widthProperty());
        boardView.fitHeightProperty().bind(gameRoot.heightProperty());

        gameScene = new Scene(gameRoot, 800, 600);
        
        // ---- 3) Upgrade scene for transitions between games ----
        setupUpgradeScene();

        // ---- 4) Show the menu ----
        primaryStage.setTitle("Jackaroo Simulator");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }
    
    /**
     * Sets up the upgrade scene for transitions between games
     */
    private void setupUpgradeScene() {
        Label upgradeTitle = new Label("Player Has Improved!");
        upgradeTitle.setFont(new Font("Arial", 24));
        
        Label upgradeDesc = new Label("Player 2 (Red) has learned from the game and evolved to a higher skill level!");
        upgradeDesc.setFont(new Font("Arial", 16));
        upgradeDesc.setWrapText(true);
        upgradeDesc.setTextAlignment(TextAlignment.CENTER);
        upgradeDesc.setMaxWidth(600);
        
        Button continueBtn = new Button("Continue to Next Game");
        continueBtn.setPrefWidth(200);
        continueBtn.setOnAction(e -> startNextLearningGame());
        
        Button menuBtn = new Button("Return to Menu");
        menuBtn.setPrefWidth(200);
        menuBtn.setOnAction(e -> primaryStage.setScene(menuScene));
        
        VBox upgradeRoot = new VBox(30, upgradeTitle, upgradeDesc, continueBtn, menuBtn);
        upgradeRoot.setAlignment(Pos.CENTER);
        upgradeScene = new Scene(upgradeRoot, 800, 600);
    }
    
    /**
     * Start Phase 0 - creates a game with existing player setup
     */
    private void startPhase0() {
        // Clear any existing content in the game pane
        gameRoot.getChildren().clear();
        
        // Add the board background
        Image boardImage = new Image(
            getClass().getResourceAsStream("/images/boardd.png")
        );
        ImageView boardView = new ImageView(boardImage);
        boardView.setPreserveRatio(false);
        boardView.fitWidthProperty().bind(gameRoot.widthProperty());
        boardView.fitHeightProperty().bind(gameRoot.heightProperty());
        gameRoot.getChildren().add(boardView);
        
        // Create players - 3 Normals and 1 Pro
        player1 = new NormalPlayer("player1");
        player2 = new ProPlayer("player2");
        player3 = new NormalPlayer("player3");
        player4 = new NormalPlayer("player4"); 
        
        // Create and initialize the game board
        gameBoard = new Board(gameRoot, player1, player2, player3, player4) {
            protected void checkForWin() {
                super.checkForWin();
                // Additional hook to handle game completion
                // NOTE: This does nothing special in Phase 0 mode
            }
        };
        gameBoard.initializeGame();
        
        // Show the game scene
        showGameScene();
        
        System.out.println("Game started with standard player setup");
    }
    
    /**
     * Start the learning progression game with a Beginner player
     */
    private void startLearningGame() {
        // Reset game tracking
        currentGameNumber = 1;
        waitingForNextGame = false;
        
        // Clear any existing content in the game pane
        gameRoot.getChildren().clear();
        
        // Add the board background
        Image boardImage = new Image(
            getClass().getResourceAsStream("/images/boardd.png")
        );
        ImageView boardView = new ImageView(boardImage);
        boardView.setPreserveRatio(false);
        boardView.fitWidthProperty().bind(gameRoot.widthProperty());
        boardView.fitHeightProperty().bind(gameRoot.heightProperty());
        gameRoot.getChildren().add(boardView);
        
        // Create players - 3 Normal and 1 Beginner (as player2 - red)
        player1 = new NormalPlayer("player1");    // Black
        player2 = new BeginnerPlayer("player2");  // Red - Beginner (will learn)
        player3 = new NormalPlayer("player3");    // Blue
        player4 = new NormalPlayer("player4");    // Green
        
        // Create label to show current player level
        Label skillLabel = new Label("Player 2 (Red): BEGINNER Level");
        skillLabel.setStyle("-fx-background-color: rgba(255,255,255,0.7); -fx-padding: 5px;");
        skillLabel.relocate(20, 20);
        gameRoot.getChildren().add(skillLabel);
        
        // Create and initialize the game board with win detection override
        gameBoard = new Board(gameRoot, player1, player2, player3, player4) {
            protected void checkForWin() {
                // First call the original method
                super.checkForWin();
                
                // Then check if the game is over to handle learning progression
                if (this.gameOver && !waitingForNextGame) {
                    waitingForNextGame = true;
                    
                    // Only handle progression if we're in learning mode (game 1 or 2)
                    if (currentGameNumber < 3) {
                        System.out.println("\n===== GAME " + currentGameNumber + " FINISHED =====");
                        
                        // Schedule transition to upgrade scene (give time for win dialog to be dismissed)
                        javafx.application.Platform.runLater(() -> {
                            // Wait 2 seconds then show upgrade scene
                            new java.util.Timer().schedule(
                                new java.util.TimerTask() {
                                    @Override
                                    public void run() {
                                        javafx.application.Platform.runLater(() -> {
                                            // Update the upgrade scene text based on current level
                                            Label desc = (Label)((VBox)upgradeScene.getRoot()).getChildren().get(1);
                                            
                                            if (currentGameNumber == 1) {
                                                desc.setText("Player 2 (Red) has learned from playing as a BEGINNER and will now play as an INTERMEDIATE level player!");
                                            } else if (currentGameNumber == 2) {
                                                desc.setText("Player 2 (Red) has mastered the game as an INTERMEDIATE player and will now play as a PRO level player!");
                                            }
                                            
                                            primaryStage.setScene(upgradeScene);
                                        });
                                    }
                                },
                                2000
                            );
                        });
                    }
                }
            }
        };
        gameBoard.initializeGame();
        
        // Show the game scene
        showGameScene();
        
        System.out.println("\n===== STARTING LEARNING GAME 1 =====");
        System.out.println("Player 2 (Red) is starting as a BEGINNER");
    }
    
    /**
     * Start the next game in the learning progression
     */
    private void startNextLearningGame() {
        // Clear any existing content
        gameRoot.getChildren().clear();
        waitingForNextGame = false;
        
        // Add the board background
        Image boardImage = new Image(
            getClass().getResourceAsStream("/images/boardd.png")
        );
        ImageView boardView = new ImageView(boardImage);
        boardView.setPreserveRatio(false);
        boardView.fitWidthProperty().bind(gameRoot.widthProperty());
        boardView.fitHeightProperty().bind(gameRoot.heightProperty());
        gameRoot.getChildren().add(boardView);
        
        // Improve the player based on current game number
        if (currentGameNumber == 1) {
            // Beginner -> Intermediate
            currentGameNumber = 2;
            Player improvedPlayer = ((Teachable)player2).improve();
            player2 = improvedPlayer;  // Replace with improved player
            
            // Create label to show current player level
            Label skillLabel = new Label("Player 2 (Red): INTERMEDIATE Level");
            skillLabel.setStyle("-fx-background-color: rgba(255,255,255,0.7); -fx-padding: 5px;");
            skillLabel.relocate(20, 20);
            gameRoot.getChildren().add(skillLabel);
            
            System.out.println("\n===== STARTING LEARNING GAME 2 =====");
            System.out.println("Player 2 (Red) is now playing as an INTERMEDIATE player");
            
        } else if (currentGameNumber == 2) {
            // Intermediate -> Pro
            currentGameNumber = 3;
            Player improvedPlayer = ((Teachable)player2).improve();
            player2 = improvedPlayer;  // Replace with improved player
            
            // Create label to show current player level
            Label skillLabel = new Label("Player 2 (Red): PRO Level");
            skillLabel.setStyle("-fx-background-color: rgba(255,255,255,0.7); -fx-padding: 5px;");
            skillLabel.relocate(20, 20);
            gameRoot.getChildren().add(skillLabel);
            
            System.out.println("\n===== STARTING LEARNING GAME 3 (FINAL) =====");
            System.out.println("Player 2 (Red) is now playing as a PRO player");
        }
        
        // Create and initialize a new game board with the same win detection override
        gameBoard = new Board(gameRoot, player1, player2, player3, player4) {
            @Override
            protected void checkForWin() {
                // First call the original method
                super.checkForWin();
                
                // Then check if the game is over to handle learning progression
                if (this.gameOver && !waitingForNextGame) {
                    waitingForNextGame = true;
                    
                    // Only handle progression if we're in learning mode (game 1 or 2)
                    if (currentGameNumber < 3) {
                        System.out.println("\n===== GAME " + currentGameNumber + " FINISHED =====");
                        
                        // Schedule transition to upgrade scene
                        javafx.application.Platform.runLater(() -> {
                            // Wait 2 seconds then show upgrade scene
                            new java.util.Timer().schedule(
                                new java.util.TimerTask() {
                                    @Override
                                    public void run() {
                                        javafx.application.Platform.runLater(() -> {
                                            // Update the upgrade scene text based on current level
                                            Label desc = (Label)((VBox)upgradeScene.getRoot()).getChildren().get(1);
                                            
                                            if (currentGameNumber == 1) {
                                                desc.setText("Player 2 (Red) has learned from playing as a BEGINNER and will now play as an INTERMEDIATE level player!");
                                            } else if (currentGameNumber == 2) {
                                                desc.setText("Player 2 (Red) has mastered the game as an INTERMEDIATE player and will now play as a PRO level player!");
                                            }
                                            
                                            primaryStage.setScene(upgradeScene);
                                        });
                                    }
                                },
                                2000
                            );
                        });
                    } else {
                        System.out.println("\n===== LEARNING PROGRESSION COMPLETE =====");
                        System.out.println("Player 2 (Red) has completed all learning stages!");
                    }
                }
            }
        };
        gameBoard.initializeGame();
        
        // Show the game scene
        showGameScene();
    }
    
    /**
     * Shows the game scene
     */
    private void showGameScene() {
        primaryStage.setScene(gameScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}