package application;

import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.List;

public class NormalPlayer {
    private final String playerName;
    private final List<Marble> marbles = new ArrayList<>();
    private final List<Card> cards = new ArrayList<>();
    
    // Card positions for each player
    private static final double[][] PLAYER1_CARD_POSITIONS = {
        {460, 40},  // Card 1
        {525, 40},  // Card 2
        {585, 40},  // Card 3
        {650, 40}   // Card 4
    };
    
    private static final double[][] PLAYER2_CARD_POSITIONS = {
        {590, 230},  // Card 1
        {650, 230},  // Card 2
        {590, 320},  // Card 3
        {650, 320}   // Card 4
    };
    
    private static final double[][] PLAYER3_CARD_POSITIONS = {
        {450, 450},  // Card 1
        {390, 450},  // Card 2
        {330, 450},  // Card 3
        {270, 450}   // Card 4
    };
    
    private static final double[][] PLAYER4_CARD_POSITIONS = {
        {130, 230},  // Card 1
        {70, 230},   // Card 2
        {130, 320},  // Card 3
        {70, 320}    // Card 4
    };
    
   
    public NormalPlayer(String playerName, Card card1, Card card2, Card card3, Card card4) {
        this.playerName = playerName;
        
        // Create marbles for this player
        for (int i = 1; i <= 4; i++) {
            marbles.add(new Marble(playerName, i));
        }
        
        // Set up cards
        cards.add(card1);
        cards.add(card2);
        cards.add(card3);
        cards.add(card4);
        
        // Position cards based on player
        double[][] cardPositions = getCardPositionsForPlayer(playerName);
        for (int i = 0; i < 4; i++) {
            Card card = cards.get(i);
            card.setFaceUp(true);
            card.getCardImageView().setX(cardPositions[i][0]);
            card.getCardImageView().setY(cardPositions[i][1]);
        }
    }
    
    /**
     * Get the correct card positions based on player
     */
    private double[][] getCardPositionsForPlayer(String player) {
        switch(player.toLowerCase()) {
            case "player1": return PLAYER1_CARD_POSITIONS;
            case "player2": return PLAYER2_CARD_POSITIONS;
            case "player3": return PLAYER3_CARD_POSITIONS;
            case "player4": return PLAYER4_CARD_POSITIONS;
            default: return PLAYER1_CARD_POSITIONS;
        }
    }
    
    /**
     * Add all player elements (marbles and cards) to the specified pane
     * @param pane The pane to add elements to
     */
    public void addToPane(Pane pane) {
        // Add all marbles to the pane
        for (Marble marble : marbles) {
            pane.getChildren().add(marble);
        }
        
        // Add all cards to the pane
        for (Card card : cards) {
            pane.getChildren().add(card.getCardImageView());
        }
    }
    
    /**
     * Get all marbles for this player
     */
    public List<Marble> getMarbles() {
        return marbles;
    }
    
    /**
     * Get all cards for this player
     */
    public List<Card> getCards() {
        return cards;
    }
    
    /**
     * Get the player name
     */
    public String getPlayerName() {
        return playerName;
    }
}