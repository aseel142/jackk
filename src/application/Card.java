package application;

import javafx.scene.image.ImageView;

public class Card {
    public enum Suit {
        CLUBS, DIAMONDS, HEARTS, SPADES
    }
    
    public enum Value {
        ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, 
        EIGHT, NINE, TEN, JACK, QUEEN, KING
    }
    
    private final Suit suit;
    private final Value value;
    private final ImageView cardImageView;
    private boolean faceUp = false;
    
    // Standard card size constants - adjust these values to change all card sizes
    private static final double CARD_WIDTH = 100;
    private static final double CARD_HEIGHT = 80;
    
    public Card(Suit suit, Value value) {
        this.suit = suit;
        this.value = value;
        
        // Create image view with card back initially
        cardImageView = new ImageView(CardManager.getCardBack());
        
        // Set standard size for all cards
        cardImageView.setFitWidth(CARD_WIDTH);
        cardImageView.setFitHeight(CARD_HEIGHT);
        cardImageView.setPreserveRatio(true);
    }
    
    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
        if (faceUp) {
            // Convert Value enum to the number/name used in the filename
            String valueStr = getValueString();
            // Convert Suit enum to lowercase for the filename
            String suitStr = suit.toString().toLowerCase();
            // Get the card image from CardManager
            String cardName = valueStr + "_of_" + suitStr;
            cardImageView.setImage(CardManager.getCardImage(cardName));
        } else {
            cardImageView.setImage(CardManager.getCardBack());
        }
    }
    
    private String getValueString() {
        switch (value) {
            case ACE: return "ace";
            case TWO: return "2";
            case THREE: return "3";
            case FOUR: return "4";
            case FIVE: return "5";
            case SIX: return "6";
            case SEVEN: return "7";
            case EIGHT: return "8";
            case NINE: return "9";
            case TEN: return "10";
            case JACK: return "jack";
            case QUEEN: return "queen";
            case KING: return "king";
            default: return "";
        }
    }
    
    // Getters
    public Suit getSuit() {
        return suit;
    }
    
    public Value getValue() {
        return value;
    }
    
    public ImageView getCardImageView() {
        return cardImageView;
    }
    
    public boolean isFaceUp() {
        return faceUp;
    }
    
    // Flip the card (toggle face up/down)
    public void flip() {
        setFaceUp(!faceUp);
    }
    
    // Method to make a card draggable
    public void makeDraggable() {
        final double[] dragDelta = new double[2];
        
        cardImageView.setOnMousePressed(event -> {
            // Bring card to front
            cardImageView.toFront();
            // Store initial position
            dragDelta[0] = cardImageView.getX() - event.getSceneX();
            dragDelta[1] = cardImageView.getY() - event.getSceneY();
        });
        
        cardImageView.setOnMouseDragged(event -> {
            cardImageView.setX(event.getSceneX() + dragDelta[0]);
            cardImageView.setY(event.getSceneY() + dragDelta[1]);
        });
    }
    
    @Override
    public String toString() {
        return value + " of " + suit;
    }
}