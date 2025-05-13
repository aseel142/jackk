package application;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;

/**
 * Abstract base class for all player types
 */
public abstract class Player {
    // Player identity
    protected String name;
    protected List<Marble> marbles;
    protected List<Card> cards;
    
    // Home position coordinates for marbles
    protected double[][] homePositions;
    
    /**
     * Create a new player
     */
    public Player(String name) {
        this.name = name;
        this.marbles = new ArrayList<>();
        this.cards = new ArrayList<>();
        
        // Set home positions based on player name
        setupHomePositions();
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Set up home positions for this player's marbles
     * FIXED: Swapped home positions for player3 and player4 to match Marble.java
     */
    private void setupHomePositions() {
        switch (name.toLowerCase()) {
            case "player1":
                homePositions = new double[][] {
                    {542, 155}, // Position 1 - RIGHT of board
                    {584, 155}, // Position 2
                    {584, 184}, // Position 3
                    {542, 184}  // Position 4
                };
                break;
                
            case "player2":
                homePositions = new double[][] {
                    {499, 400}, // Position 1 - BOTTOM of board
                    {540, 400}, // Position 2
                    {540, 428}, // Position 3
                    {499, 428}  // Position 4
                };
                break;
                
            case "player3":
                homePositions = new double[][] {
                    {199, 347}, // Position 1 - LEFT of board (swapped with player4)
                    {245, 346}, // Position 2
                    {200, 376}, // Position 3
                    {245, 376}  // Position 4
                };
                break;
                
            case "player4":
                homePositions = new double[][] {
                    {223, 152}, // Position 1 - TOP of board (swapped with player3)
                    {268, 152}, // Position 2
                    {223, 181}, // Position 3
                    {268, 181}  // Position 4
                };
                break;
        }
    }
    
    /**
     * Create marbles for this player
     */
    public void createMarbles() {
        marbles.clear();
        // Create 4 marbles with the appropriate color
        for (int i = 0; i < 4; i++) {
            Marble marble = new Marble(name, i + 1);
            marbles.add(marble);
        }
    }
    
    /**
     * Get the home position for a specific marble
     */
    public double[] getHomePosition(int index) {
        if (index >= 0 && index < homePositions.length) {
            return homePositions[index];
        }
        return new double[] {0, 0}; // Default
    }
    
    /**
     * Get the index of a marble in this player's marbles list
     */
    public int getMarbleIndex(Marble marble) {
        return marbles.indexOf(marble);
    }
    
    /**
     * Get player's marbles
     */
    public List<Marble> getMarbles() {
        return marbles;
    }
    
    /**
     * Get player's cards
     */
    public List<Card> getCards() {
        return cards;
    }
    
    /**
     * Add a card to player's hand
     */
    public void addCard(Card card) {
        cards.add(card);
        positionCardInHand(card, cards.size() - 1);
    }
    
    /**
     * Position a card in the player's visual hand
     */
    private void positionCardInHand(Card card, int index) {
        // Position cards in a arc/fan at the bottom of the screen for player 1,
        // at the right for player 2, top for player 3, left for player 4
        double cardX = 0;
        double cardY = 0;
        double rotation = 0;
        
        switch (name.toLowerCase()) {
            case "player2":
                // Cards at bottom
                cardX = 300 + (index * 50);
                cardY = 490;
                rotation = -10 + (index * 5);
                break;
                
            case "player1":
                // Cards at right
                cardX = 680;
                cardY = 200 + (index * 50);
                rotation = 80 + (index * 5);
                break;
                
            case "player4":
                // Cards at top - ADJUSTED: shifted 50 pixels to the right
                cardX = 600 - (index * 50);  // Changed from 500 to 550
                cardY = 30;
                rotation = 170 + (index * 5);
                break;
                
            case "player3":
                // Cards at left
                cardX = 80;
                cardY = 400 - (index * 50);
                rotation = 260 + (index * 5);
                break;
        }
        
        // Set the card's position and rotation
        card.getCardImageView().setX(cardX);
        card.getCardImageView().setY(cardY);
        card.getCardImageView().setRotate(rotation);

        
        // Show the card face-up to the player
        card.setFaceUp(true);
    }
    
    /**
     * Remove a card from player's hand
     */
    public void removeCard(Card card) {
        cards.remove(card);
        // Re-position remaining cards
        for (int i = 0; i < cards.size(); i++) {
            positionCardInHand(cards.get(i), i);
        }
    }
    
    /**
     * Clear all cards from player's hand
     */
    public void clearCards() {
        cards.clear();
    }
    
    /**
     * Abstract method to be implemented by each player type
     * Selects and plays a card based on strategy
     */
    public abstract void takeTurn(Board board);
    
    /**
     * Abstract method to be implemented by each player type
     * Chooses how to use a card that was played
     */
    public abstract void makeMove(Board board, Card card);
}