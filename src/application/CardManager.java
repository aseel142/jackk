package application;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

public class CardManager {
    private static Map<String, Image> cardImages = new HashMap<>();
    private static Image cardBack;
    
    // Call this method once at the start of your program
    public static void preloadCardImages() {
        try {
            System.out.println("Loading all card images...");
            
            // The base path to your card images - now directly in the images folder
            String basePath = "/images/";
            
            // Load number cards 2-10
            for (int i = 2; i <= 10; i++) {
                String value = String.valueOf(i);
                for (String suit : new String[]{"clubs", "diamonds", "hearts", "spades"}) {
                    String cardName = value + "_of_" + suit;
                    String imagePath = basePath + cardName + ".png";
                    try {
                        Image cardImage = new Image(CardManager.class.getResourceAsStream(imagePath));
                        cardImages.put(cardName, cardImage);
                        System.out.println("Loaded: " + cardName);
                    } catch (Exception e) {
                        System.err.println("Failed to load: " + imagePath);
                    }
                }
            }
            
            // Load face cards and aces
            for (String value : new String[]{"ace", "jack", "queen", "king"}) {
                for (String suit : new String[]{"clubs", "diamonds", "hearts", "spades"}) {
                    String cardName = value + "_of_" + suit;
                    String imagePath = basePath + cardName + ".png";
                    try {
                        Image cardImage = new Image(CardManager.class.getResourceAsStream(imagePath));
                        cardImages.put(cardName, cardImage);
                        System.out.println("Loaded: " + cardName);
                    } catch (Exception e) {
                        System.err.println("Failed to load: " + imagePath);
                    }
                }
            }
            
            // Load special cards like jokers and card backs
            try {
                Image jokerImage = new Image(CardManager.class.getResourceAsStream(basePath + "black_joker.png"));
                cardImages.put("black_joker", jokerImage);
                System.out.println("Loaded: black_joker");
            } catch (Exception e) {
                System.err.println("Failed to load black joker");
            }
            
            // Use a card back if available, otherwise use another card
            try {
                cardBack = new Image(CardManager.class.getResourceAsStream(basePath + "card_back.png"));
            } catch (Exception e) {
                // If no card back, use first available card
                cardBack = cardImages.get("2_of_clubs");
                if (cardBack == null) {
                    // Last resort - use the first card we find
                    for (Image img : cardImages.values()) {
                        if (img != null) {
                            cardBack = img;
                            break;
                        }
                    }
                }
            }
            
            System.out.println("Loaded " + cardImages.size() + " cards");
            
        } catch (Exception e) {
            System.err.println("Error loading card images: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static Image getCardImage(String cardName) {
        Image image = cardImages.get(cardName);
        if (image == null) {
            System.err.println("Could not find card image for: " + cardName);
            // Return card back as a fallback
            return cardBack;
        }
        return image;
    }
    
    public static Image getCardBack() {
        if (cardBack != null) {
            return cardBack;
        } else {
            // Use a transparent image as last resort
            try {
                return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=");
            } catch (Exception e) {
                return null;
            }
        }
    }
    
    // Helper method to get card by suit and value
    public static Image getCard(Card.Suit suit, Card.Value value) {
        String suitStr = suit.toString().toLowerCase();
        String valueStr;
        
        switch (value) {
            case ACE: valueStr = "ace"; break;
            case TWO: valueStr = "2"; break;
            case THREE: valueStr = "3"; break;
            case FOUR: valueStr = "4"; break;
            case FIVE: valueStr = "5"; break;
            case SIX: valueStr = "6"; break;
            case SEVEN: valueStr = "7"; break;
            case EIGHT: valueStr = "8"; break;
            case NINE: valueStr = "9"; break;
            case TEN: valueStr = "10"; break;
            case JACK: valueStr = "jack"; break;
            case QUEEN: valueStr = "queen"; break;
            case KING: valueStr = "king"; break;
            default: valueStr = "2";
        }
        
        return getCardImage(valueStr + "_of_" + suitStr);
    }
}