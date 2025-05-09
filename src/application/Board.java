package application;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The Board class manages the game state and enforces the rules of Jackaroo
 */
public class Board {
    // The game board pane
    private Pane gamePane;
    
    // Players
    private Player player1;
    private Player player2;
    private Player player3;
    private Player player4;
    
    // Current player and turn tracking
    private Player currentPlayer;
    private int currentPlayerIndex;
    private int startingPlayerIndex;
    private int loopCount;
    private int roundCount;
    
    // Marble position tracking
    private Map<Marble, Integer> marblePositions;
    private Map<Marble, Boolean> marblesInHome;
    
    // Constants for positions
    private static final int PLAYER1_BASE = 51;
    private static final int PLAYER2_BASE = 1;
    private static final int PLAYER3_BASE = 18;
    private static final int PLAYER4_BASE = 35;
    
    // Card deck and discard pile
    private List<Card> deck;
    private List<Card> discardPile;
    
    // Random for shuffling and card dealing
    private Random random;
    
    /**
     * Creates a new board with the specified players
     */
    public Board(Pane gamePane, Player player1, Player player2, Player player3, Player player4) {
        this.gamePane = gamePane;
        this.player1 = player1;
        this.player2 = player2;
        this.player3 = player3;
        this.player4 = player4;
        
        // Initialize game state tracking
        marblePositions = new HashMap<>();
        marblesInHome = new HashMap<>();
        deck = new ArrayList<>();
        discardPile = new ArrayList<>();
        random = new Random();
        
        // Initial turn values
        currentPlayerIndex = 0; // Start with player 1
        startingPlayerIndex = 0;
        loopCount = 0;
        roundCount = 0;
    }

    /**
     * Initialize a new game
     */
    public void initializeGame() {
        // Reset state
        marblePositions.clear();
        marblesInHome.clear();
        deck.clear();
        discardPile.clear();

        // Setup board
        setupPlayers();
        createDeck();
        shuffleDeck();
        dealCards(4);

        currentPlayerIndex = startingPlayerIndex;
        currentPlayer = getPlayerByIndex(currentPlayerIndex);
        currentPlayer.takeTurn(this);
    }

    /**
     * Prepare players and place their marbles
     */
    private void setupPlayers() {
        for (Player p : new Player[]{player1, player2, player3, player4}) {
            p.createMarbles();
            for (Marble m : p.getMarbles()) {
                marblesInHome.put(m, true);
                gamePane.getChildren().add(m);
            }
        }
        placeAllMarblesAtHome();
    }

    private void placeAllMarblesAtHome() {
        placePlayerMarblesAtHome(player1);
        placePlayerMarblesAtHome(player2);
        placePlayerMarblesAtHome(player3);
        placePlayerMarblesAtHome(player4);
    }

    private void placePlayerMarblesAtHome(Player player) {
        List<Marble> marbles = player.getMarbles();
        for (int i = 0; i < marbles.size(); i++) {
            Marble m = marbles.get(i);
            double[] home = player.getHomePosition(i);
            m.setCenterX(home[0]);
            m.setCenterY(home[1]);
        }
    }

    /**
     * Build a standard 52-card deck
     */
    private void createDeck() {
        for (Card.Suit suit : Card.Suit.values()) {
            for (int i = 2; i <= 10; i++) {
                deck.add(new Card(suit, getValueFromInt(i)));
            }
            deck.add(new Card(suit, Card.Value.ACE));
            deck.add(new Card(suit, Card.Value.JACK));
            deck.add(new Card(suit, Card.Value.QUEEN));
            deck.add(new Card(suit, Card.Value.KING));
        }
    }

    private Card.Value getValueFromInt(int v) {
        switch (v) {
            case 2: return Card.Value.TWO;
            case 3: return Card.Value.THREE;
            case 4: return Card.Value.FOUR;
            case 5: return Card.Value.FIVE;
            case 6: return Card.Value.SIX;
            case 7: return Card.Value.SEVEN;
            case 8: return Card.Value.EIGHT;
            case 9: return Card.Value.NINE;
            case 10: return Card.Value.TEN;
            default: return Card.Value.TWO;
        }
    }

    /**
     * Simple Fisher–Yates shuffle
     */
    private void shuffleDeck() {
        for (int i = deck.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Card tmp = deck.get(i);
            deck.set(i, deck.get(j));
            deck.set(j, tmp);
        }
    }

    /**
     * Deal cards to each player and add them to the pane
     */
    private void dealCards(int perPlayer) {
        // Clear existing hands
        player1.clearCards();
        player2.clearCards();
        player3.clearCards();
        player4.clearCards();

        for (int i = 0; i < perPlayer; i++) {
            refillIfEmpty();
            if (!deck.isEmpty()) {
                Card c = deck.remove(0);
                player1.addCard(c);
                ImageView iv = c.getCardImageView();
                if (iv.getParent() == null) {
                    gamePane.getChildren().add(iv);
                }

            }
            refillIfEmpty();
            if (!deck.isEmpty()) {
                Card c = deck.remove(0);
                player2.addCard(c);
                ImageView iv = c.getCardImageView();
                if (iv.getParent() == null) {
                    gamePane.getChildren().add(iv);
                }

            }
            refillIfEmpty();
            if (!deck.isEmpty()) {
                Card c = deck.remove(0);
                player3.addCard(c);
                ImageView iv = c.getCardImageView();
                if (iv.getParent() == null) {
                    gamePane.getChildren().add(iv);
                }

            }
            refillIfEmpty();
            if (!deck.isEmpty()) {
                Card c = deck.remove(0);
                player4.addCard(c);
                ImageView iv = c.getCardImageView();
                if (iv.getParent() == null) {
                    gamePane.getChildren().add(iv);
                }

            }
        }
    }

    /**
     * Move discard pile back into deck if empty
     */
    private void refillIfEmpty() {
        if (deck.isEmpty()) {
            deck.addAll(discardPile);
            discardPile.clear();
            shuffleDeck();
        }
    }

    /**
     * Lookup player by turn index (0–3)
     */
    public Player getPlayerByIndex(int idx) {
        switch (idx) {
            case 0: return player1;
            case 1: return player2;
            case 2: return player3;
            case 3: return player4;
            default: return player1;
        }
    }
    
    /**
     * Given a Player, return the board space where their marbles leave home.
     */
    private int getPlayerBase(Player player) {
        if (player == player1) return PLAYER1_BASE;
        if (player == player2) return PLAYER2_BASE;
        if (player == player3) return PLAYER3_BASE;
        if (player == player4) return PLAYER4_BASE;
        return PLAYER2_BASE; // or whatever default makes sense
    }

    /**
     * Move a marble to a specified board position with animation
     */
    public void moveMarbleToPosition(Marble marble, int position, double durationSeconds, double delaySeconds) {
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
            // Update internal state
            marblePositions.put(marble, position);
            marblesInHome.put(marble, false);
            
            // Check for captures
            checkForCaptures(marble, position);
            
            // Check for win
            checkForWin();
            // Move to next player's turn
            nextTurn();
        });
        
        // Play the animation
        timeline.play();
    }
    
    /**
     * Check if the given marble captures any other marbles at its position
     */
    private void checkForCaptures(Marble justMoved, int newPosition) {
        Player mover = findMarbleOwner(justMoved);
        if (mover == null) return;

        // 1) Gather all enemy marbles sitting at the same spot
        List<Marble> toCapture = new ArrayList<>();
        for (Map.Entry<Marble, Integer> entry : marblePositions.entrySet()) {
            Marble other = entry.getKey();
            int pos = entry.getValue();

            if (other == justMoved) continue;               // skip the one we just moved
            if (marblesInHome.getOrDefault(other, false)) continue;  // skip ones still in home

            Player owner = findMarbleOwner(other);
            if (owner != null && isInSafeZone(owner, pos)) continue; // skip safely home marbles

            if (pos == newPosition) {
                toCapture.add(other);
            }
        }

        // 2) Now actually remove them and animate the “send home”
        for (Marble victim : toCapture) {
            // mark it back in home
            marblesInHome.put(victim, true);
            marblePositions.remove(victim);

            // animate back to its owner’s home slot
            Player owner = findMarbleOwner(victim);
            int idx = owner.getMarbleIndex(victim);
            double[] home   = owner.getHomePosition(idx);
            moveMarbleVisually(victim, home[0], home[1], 1.0, 0.0);
        }
    }
    
    /**
     * Move a marble visually to specific coordinates
     */
    private void moveMarbleVisually(Marble marble, double x, double y, double duration, double delay) {
        Timeline timeline = new Timeline();
        
        KeyFrame start = new KeyFrame(Duration.ZERO, 
            new KeyValue(marble.centerXProperty(), marble.getCenterX()),
            new KeyValue(marble.centerYProperty(), marble.getCenterY()));
        
        KeyFrame end = new KeyFrame(Duration.seconds(duration),
            new KeyValue(marble.centerXProperty(), x, Interpolator.EASE_BOTH),
            new KeyValue(marble.centerYProperty(), y, Interpolator.EASE_BOTH));
        
        timeline.getKeyFrames().addAll(start, end);
        timeline.setDelay(Duration.seconds(delay));
        timeline.play();
    }
    
    /**
     * Find which player owns a marble
     */
    private Player findMarbleOwner(Marble marble) {
        if (player1.getMarbles().contains(marble)) return player1;
        if (player2.getMarbles().contains(marble)) return player2;
        if (player3.getMarbles().contains(marble)) return player3;
        if (player4.getMarbles().contains(marble)) return player4;
        return null;
    }
    
    /**
     * Check if a player has won
     */
    private void checkForWin() {
        // Check if Team 1 (Players 1 and 3) has won
        boolean team1Wins = allMarblesInSafeZone(player1) && allMarblesInSafeZone(player3);
        
        // Check if Team 2 (Players 2 and 4) has won
        boolean team2Wins = allMarblesInSafeZone(player2) && allMarblesInSafeZone(player4);
        
        if (team1Wins) {
            System.out.println("Team 1 (Players 1 & 3) wins!");
            // Handle win (show message, reset game, etc.)
        } else if (team2Wins) {
            System.out.println("Team 2 (Players 2 & 4) wins!");
            // Handle win
        }
    }
    
    /**
     * Check if all of a player's marbles are in their safe zone
     */
    private boolean allMarblesInSafeZone(Player player) {
        for (Marble marble : player.getMarbles()) {
            // Skip marbles still in home
            if (marblesInHome.get(marble)) return false;
            
            // Check if the marble is in a safe zone
            Integer position = marblePositions.get(marble);
            if (position == null || !isInSafeZone(player, position)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if a position is in a player's safe zone
     */
    public boolean isInSafeZone(Player player, int position) {
        if (player == player1) return position >= 46 && position <= 49;
        if (player == player2) return position >= 63 && position <= 66;
        if (player == player3) return position >= 13 && position <= 16;
        if (player == player4) return position >= 30 && position <= 33;
        return false;
    }
    
    /**
     * Check if a position is in another player's safe zone
     */
    private boolean isOtherPlayerSafeZone(Player player, int position) {
        if (player == player1) {
            return (position >= 63 && position <= 66) || // Player 2's safe zone
                   (position >= 13 && position <= 16) || // Player 3's safe zone 
                   (position >= 30 && position <= 33);   // Player 4's safe zone
        } else if (player == player2) {
            return (position >= 46 && position <= 49) || // Player 1's safe zone
                   (position >= 13 && position <= 16) || // Player 3's safe zone 
                   (position >= 30 && position <= 33);   // Player 4's safe zone
        } else if (player == player3) {
            return (position >= 46 && position <= 49) || // Player 1's safe zone
                   (position >= 63 && position <= 66) || // Player 2's safe zone 
                   (position >= 30 && position <= 33);   // Player 4's safe zone
        } else if (player == player4) {
            return (position >= 46 && position <= 49) || // Player 1's safe zone
                   (position >= 63 && position <= 66) || // Player 2's safe zone 
                   (position >= 13 && position <= 16);   // Player 3's safe zone
        }
        return false;
    }
    
    /**
     * Calculates the target position after moving a specified number of steps
     */
    public int calculateTargetPosition(Player player, int currentPosition, int steps) {
        // If already in safe zone, continue moving in safe zone until steps are exhausted
        if (isInSafeZone(player, currentPosition)) {
            int position = currentPosition;
            for (int i = 0; i < steps; i++) {
                position++;
                // If reached end of safe zone, stop moving
                if ((player == player1 && position > 49) ||
                    (player == player2 && position > 66) ||
                    (player == player3 && position > 16) ||
                    (player == player4 && position > 33)) {
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
                    if ((player == player1 && position > 49) ||
                        (player == player2 && position > 66) ||
                        (player == player3 && position > 16) ||
                        (player == player4 && position > 33)) {
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
     * Process a card being played
     */
    public void playCard(Player player, Card card) {
        // 1) flip & discard
        card.setFaceUp(true);
        discardPile.add(card);
        player.removeCard(card);

        // 2) animate card → center, then let the player move the marble
        moveToCenter(card, 1.0, 0.0, () -> {
            // this runs after the card is at the center:
            player.makeMove(this, card);
            // note: your makeMove(...) should queue moveMarbleToPosition(...)
            // whose onFinished already calls nextTurn()
        });
    }
    /**
     * Animate a card moving to center, then invoke onFinished.
     */
    public void moveToCenter(Card card,
                             double durationSeconds,
                             double delaySeconds,
                             Runnable onFinished) {
        ImageView view = card.getCardImageView();
        double centerX = 360;
        double centerY = 240;
        double startX  = view.getX();
        double startY  = view.getY();

        TranslateTransition tt = new TranslateTransition(
            Duration.seconds(durationSeconds),
            view
        );
        tt.setDelay(Duration.seconds(delaySeconds));
        tt.setByX(centerX - startX);
        tt.setByY(centerY - startY);
        tt.setOnFinished(evt -> {
            // snap into place
            view.setX(centerX);
            view.setY(centerY);
            view.setTranslateX(0);
            view.setTranslateY(0);
            // now run the next step
            onFinished.run();
        });
        tt.play();
    }
    /**
     * Move a card to the center of the board
     */
    private void moveCardToCenter(Card card) {
        // Center coordinates
        double centerX = 360;
        double centerY = 240;
        
        // Create animation
        TranslateTransition moveAnimation = new TranslateTransition(
            Duration.seconds(1.0), card.getCardImageView());
        
        // Calculate translation
        double startX = card.getCardImageView().getX();
        double startY = card.getCardImageView().getY();
        moveAnimation.setByX(centerX - startX);
        moveAnimation.setByY(centerY - startY);
        
        // Update position when animation completes
        moveAnimation.setOnFinished(event -> {
            card.getCardImageView().setX(centerX);
            card.getCardImageView().setY(centerY);
            card.getCardImageView().setTranslateX(0);
            card.getCardImageView().setTranslateY(0);
        });
        
        // Start animation
        moveAnimation.play();
    }
    
    /**
     * Move to the next player's turn
     */
    public void nextTurn() {
        // Move to next player
        currentPlayerIndex = (currentPlayerIndex + 1) % 4;
        currentPlayer = getPlayerByIndex(currentPlayerIndex);
        
        // If we've completed a round
        if (currentPlayerIndex == startingPlayerIndex) {
            roundCount++;
            
            // Check if all players have used all their cards
            if (player1.getCards().isEmpty() && 
                player2.getCards().isEmpty() && 
                player3.getCards().isEmpty() && 
                player4.getCards().isEmpty()) {
                
                // Complete a loop
                loopCount++;
                
                // Determine number of cards for next loop
                int cardsPerPlayer = 4; // Default
                if (loopCount >= 2) {
                    cardsPerPlayer = 5; // Increase to 5 cards for third loop onwards
                }
                
                // Deal new cards
                dealCards(cardsPerPlayer);
                
                // Update starting player after every three loops
                if (loopCount % 3 == 0) {
                    startingPlayerIndex = (startingPlayerIndex + 1) % 4;
                    currentPlayerIndex = startingPlayerIndex;
                    currentPlayer = getPlayerByIndex(currentPlayerIndex);
                }
            }
        }
        
        // Let the current player take their turn
        currentPlayer.takeTurn(this);
    }
    public int getMarblePosition(Marble marble) {
        return marblePositions.getOrDefault(
          marble,
          getPlayerBase(findMarbleOwner(marble))
        );
    }

    public boolean isMarbleInHome(Marble marble) {
        return marblesInHome.getOrDefault(marble, false);
    }
    /**
     * Swap two marbles’ positions (used by Jack’s special move).
     */
    public void swapMarbles(Marble a, Marble b) {
        // grab their current board indices
        int posA = getMarblePosition(a);
        int posB = getMarblePosition(b);

        // swap in your data maps
        marblePositions.put(a, posB);
        marblePositions.put(b, posA);
        marblesInHome.put(a, false);
        marblesInHome.put(b, false);

        // animate them to each other’s spots
        moveMarbleVisually(a,
            BoardPositions.getX(posB),
            BoardPositions.getY(posB),
            1.0, 0.0);
        moveMarbleVisually(b,
            BoardPositions.getX(posA),
            BoardPositions.getY(posA),
            1.0, 0.0);
    }


}
