package application;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage primaryStage;
    private Scene menuScene;
    private Scene phase1Scene;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // ---- 1) Main menu with "Phase 1" button ----
        Button phase1Btn = new Button("Phase 1");
        phase1Btn.setOnAction(e -> showPhase1Scene());

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
        NormalPlayer player1 = new NormalPlayer(
            "player1",
            new Card(Card.Suit.DIAMONDS, Card.Value.EIGHT),
            new Card(Card.Suit.HEARTS, Card.Value.SEVEN),
            new Card(Card.Suit.HEARTS, Card.Value.SIX),
            new Card(Card.Suit.HEARTS, Card.Value.TWO)
        );

        NormalPlayer player2 = new NormalPlayer(
            "player2",
            new Card(Card.Suit.DIAMONDS, Card.Value.SIX),
            new Card(Card.Suit.CLUBS, Card.Value.SIX),
            new Card(Card.Suit.CLUBS, Card.Value.EIGHT),
            new Card(Card.Suit.CLUBS, Card.Value.SEVEN)
        );

        NormalPlayer player3 = new NormalPlayer(
            "player3",
            new Card(Card.Suit.SPADES, Card.Value.ACE),
            new Card(Card.Suit.SPADES, Card.Value.TWO),
            new Card(Card.Suit.SPADES, Card.Value.THREE),
            new Card(Card.Suit.SPADES, Card.Value.FOUR)
        );

        NormalPlayer player4 = new NormalPlayer(
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

    private void showPhase1Scene() {
        primaryStage.setScene(phase1Scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}


