package application;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import application.Board;

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

        // ---- 2) Phase 1 scene with a full‑window board background ----
        // Load the image from your src/images folder:
        Image boardImage = new Image(
            getClass().getResourceAsStream("/images/boardd.png")
        );

        // Create an ImageView that DOES NOT preserve aspect ratio
        ImageView boardView = new ImageView(boardImage);
        boardView.setPreserveRatio(false);

        // Create the root pane *first*, then bind the image to its size:
        StackPane phase1Root = new StackPane(boardView);
        boardView.fitWidthProperty().bind(phase1Root.widthProperty());
        boardView.fitHeightProperty().bind(phase1Root.heightProperty());

        // You can add other nodes *on top* of boardView:
        // phase1Root.getChildren().add(yourGameControlsOrMarbles);

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

