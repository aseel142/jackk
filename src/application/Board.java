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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * The Board class manages the game state and enforces the rules of Jackaroo
 */
public class Board {
    // The game board pane
    private Pane gamePane;
    private boolean gameOver = false;
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
        startingPlayerIndex = 0;
        currentPlayerIndex  = 0;
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
    private void checkForCaptures(Marble captureMarble, int position) {
        Player owner = findMarbleOwner(captureMarble);
        if (owner == null) return;

        // collect to-capture so we don't mutate mid-loop
        List<Marble> toCapture = new ArrayList<>();
        for (Map.Entry<Marble,Integer> e : marblePositions.entrySet()) {
            Marble m = e.getKey();
            int     pos = e.getValue();

            if (m == captureMarble)                          continue; // skip itself
            if (marblesInHome.getOrDefault(m, false))         continue; // skip home
            Player targetOwner = findMarbleOwner(m);
            if (targetOwner == owner)                        continue; // **skip same player**
            if (targetOwner != null && isInSafeZone(targetOwner, pos)) continue; // skip safe-zone

            if (pos == position) {
                toCapture.add(m);
            }
        }

        // now actually send them home
        for (Marble victim : toCapture) {
            marblesInHome.put(victim, true);
            marblePositions.remove(victim);

            Player victOwner = findMarbleOwner(victim);
            int idx = victOwner.getMarbleIndex(victim);
            double[] home = victOwner.getHomePosition(idx);
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
        // Team 1 = players 1 & 3, Team 2 = players 2 & 4
        boolean team1Wins = allMarblesInSafeZone(player1) && allMarblesInSafeZone(player3);
        boolean team2Wins = allMarblesInSafeZone(player2) && allMarblesInSafeZone(player4);

        if (team1Wins || team2Wins) {
            gameOver = true;

            // Show an information dialog on the JavaFX thread:
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Game Over");
                alert.setHeaderText("We have a winner!");
                String winnerText = team1Wins
                    ? "Team 1 (Players 1 & 3) wins!"
                    : "Team 2 (Players 2 & 4) wins!";
                alert.setContentText(winnerText);
                alert.showAndWait();
            });
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
        if (player == player4) return position >= 30 && position <= 33;;
        return false;
    }
    
    /**
     * Check if a position is in another player's safe zone
     * Changed to public so Player classes can access it
     */
    public boolean isOtherPlayerSafeZone(Player player, int position) {
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
    /**
     * Calculates the target position after moving a specified number of steps
     */
    public int calculateTargetPosition(Player player, int currentPosition, int steps) {
        // Always enable debugging for all players
        boolean debug = true;
        String playerName = player.getName();
        
        if (debug) {
            System.out.println("\n==== " + playerName + " MOVEMENT CALCULATION ====");
            System.out.println("Starting position: " + currentPosition);
            System.out.println("Steps: " + steps);
        }
        
        // Skip calculation if no movement
        if (steps == 0) {
            return currentPosition;
        }
        
        // Handle backwards movement specially
        if (steps < 0) {
            if (debug) System.out.println("Backward movement detected");
            
            // Check if already in safe zone - if so, prevent backward movement
            if (isInSafeZone(player, currentPosition)) {
                if (debug) System.out.println("Cannot move backward from safe zone");
                return currentPosition; // No movement allowed from safe zone
            }
            
            return calculateBackwardMove(player, currentPosition, steps);
        }
        
        // Get base and safe zone info for this player
        int basePos = getPlayerBase(player);
        boolean startingFromBase = (currentPosition == basePos);
        
        // Define safe zone coordinates for each player
        int safeZoneStart = -1;
        int safeZoneEnd = -1;
        int safePosCount = 0;
        
        if (player == player1) {  // Black
            safeZoneStart = 46;
            safeZoneEnd = 49;
            safePosCount = 4;
        } else if (player == player2) {  // Red
            safeZoneStart = 63;
            safeZoneEnd = 66;
            safePosCount = 4;
        } else if (player == player3) {  // Blue
            safeZoneStart = 13;
            safeZoneEnd = 16;
            safePosCount = 4;
        } else if (player == player4) {  // Green
            safeZoneStart = 30;
            safeZoneEnd = 33;
            safePosCount = 4;
        }
        
        if (debug) {
            System.out.println("Player base: " + basePos);
            System.out.println("Safe zone: " + safeZoneStart + " to " + safeZoneEnd);
            System.out.println("Starting from base? " + startingFromBase);
        }
        
        // Check for occupied positions (by own marbles)
        Set<Integer> occupiedPositions = new HashSet<>();
        for (Marble m : player.getMarbles()) {
            if (!isMarbleInHome(m)) {
                int pos = getMarblePosition(m);
                if (pos != currentPosition) { // Exclude the marble we're moving
                    occupiedPositions.add(pos);
                    if (debug) System.out.println("Found occupied position: " + pos);
                }
            }
        }
        
        // Check if we're approaching our safe zone
        int distanceToSafeZone = -1;
        boolean approachingSafeZone = false;
        
        // Check for Red (player2)
        if (player == player2) {
            if (currentPosition == 62) {
                approachingSafeZone = true;
                distanceToSafeZone = 1;
                if (debug) System.out.println("RED is approaching safe zone (pos 62 -> 63)");
            } else if (currentPosition == 61) {
                approachingSafeZone = true;
                distanceToSafeZone = 2;
                if (debug) System.out.println("RED is approaching safe zone (pos 61 -> 63)");
            } else if (currentPosition == 60) {
                approachingSafeZone = true;
                distanceToSafeZone = 3;
                if (debug) System.out.println("RED is approaching safe zone (pos 60 -> 63)");
            }
        }
        // Check for Blue (player3)
        else if (player == player3) {
            if (currentPosition == 12) {
                approachingSafeZone = true;
                distanceToSafeZone = 1;
                if (debug) System.out.println("BLUE is approaching safe zone (pos 12 -> 13)");
            } else if (currentPosition == 11) {
                approachingSafeZone = true;
                distanceToSafeZone = 2;
                if (debug) System.out.println("BLUE is approaching safe zone (pos 11 -> 13)");
            } else if (currentPosition == 10) {
                approachingSafeZone = true;
                distanceToSafeZone = 3;
                if (debug) System.out.println("BLUE is approaching safe zone (pos 10 -> 13)");
            }
        }
        // Check for Black (player1)
        else if (player == player1) {
            if (currentPosition == 45) {
                approachingSafeZone = true;
                distanceToSafeZone = 1;
                if (debug) System.out.println("BLACK is approaching safe zone (pos 45 -> 46)");
            } else if (currentPosition == 44) {
                approachingSafeZone = true;
                distanceToSafeZone = 2;
                if (debug) System.out.println("BLACK is approaching safe zone (pos 44 -> 46)");
            } else if (currentPosition == 43) {
                approachingSafeZone = true;
                distanceToSafeZone = 3;
                if (debug) System.out.println("BLACK is approaching safe zone (pos 43 -> 46)");
            }
        }
        // Check for Green (player4)
        else if (player == player4) {
            if (currentPosition == 29) {
                approachingSafeZone = true;
                distanceToSafeZone = 1;
                if (debug) System.out.println("GREEN is approaching safe zone (pos 29 -> 30)");
            } else if (currentPosition == 28) {
                approachingSafeZone = true;
                distanceToSafeZone = 2;
                if (debug) System.out.println("GREEN is approaching safe zone (pos 28 -> 30)");
            } else if (currentPosition == 27) {
                approachingSafeZone = true;
                distanceToSafeZone = 3;
                if (debug) System.out.println("GREEN is approaching safe zone (pos 27 -> 30)");
            }
        }
        
        // SPECIAL HANDLING: Check if steps would cause us to skip our safe zone
        if (approachingSafeZone) {
            boolean wouldSkipSafeZone = false;
            
            // Check if we'd enter safe zone exactly
            if (steps == distanceToSafeZone) {
                if (debug) System.out.println("Exact steps to enter safe zone!");
                // Perfect! This is what we want
            }
            // If steps > distance and divisible by distance, we'd land on the entry
            else if (steps > distanceToSafeZone && steps % distanceToSafeZone == 0) {
                if (debug) System.out.println("Steps divisible by distance to safe zone, would land on entry");
                // Also good, we'd land exactly on the entry
            }
            // If steps > distance but we'd skip the entry point
            else if (steps > distanceToSafeZone) {
                // Here's the key: Would we skip our safe zone?
                wouldSkipSafeZone = true;
                if (debug) System.out.println("WARNING: Steps " + steps + " would cause skipping safe zone from position " + currentPosition);
            }
            
            // If we would skip the safe zone, check if we should enter it
            if (wouldSkipSafeZone) {
                // SPECIAL RULE: When we're approaching our safe zone and would skip it,
                // move into the safe zone instead
                if (debug) System.out.println("SPECIAL RULE: Redirecting movement to enter safe zone");
                
                // Calculate position in safe zone
                int stepsIntoSafeZone = steps - distanceToSafeZone;
                int safeZoneTarget = safeZoneStart + Math.min(stepsIntoSafeZone, safePosCount - 1);
                
                // Check if any of our marbles is already at the target position
                if (!occupiedPositions.contains(safeZoneTarget)) {
                    if (debug) System.out.println("Moving into safe zone at position " + safeZoneTarget);
                    return safeZoneTarget;
                } else {
                    if (debug) System.out.println("Cannot enter safe zone, position " + safeZoneTarget + " is occupied");
                    // Fall back to standard movement (may end up skipping, but we can't enter)
                }
            }
        }
        
        // Check if we're already in safe zone
        boolean startingInSafeZone = false;
        if (currentPosition >= safeZoneStart && currentPosition <= safeZoneEnd) {
            startingInSafeZone = true;
            if (debug) System.out.println("Already in safe zone at position " + currentPosition);
        }
        
        // If already in safe zone, move only within it
        if (startingInSafeZone) {
            int newPosition = currentPosition + steps;
            if (newPosition > safeZoneEnd) {
                newPosition = safeZoneEnd; // Can't go beyond end of safe zone
                if (debug) System.out.println("Cannot go beyond end of safe zone: " + safeZoneEnd);
            }
            
            // Check for collisions
            if (occupiedPositions.contains(newPosition)) {
                if (debug) System.out.println("Cannot move: position " + newPosition + " is occupied");
                return currentPosition; // Can't move if target is occupied
            }
            
            if (debug) System.out.println("Moving within safe zone to " + newPosition);
            return newPosition;
        }
        
        // Normal step-by-step movement for non-safe zone positions
        int position = currentPosition;
        int remaining = steps;
        
        while (remaining > 0) {
            int next = position + 1;
            
            // Wrap around main track
            if (next > 67) {
                next = 1;
                if (debug) System.out.println("Wrapping from 67 to 1");
            }
            
            if (debug) System.out.println("Step: position " + position + " -> " + next);
            
            // CRITICAL: Check if we should enter our safe zone
            boolean shouldEnterSafeZone = false;
            if ((player == player2 && next == 63) || // Red entering safe zone
                (player == player3 && next == 13) || // Blue entering safe zone
                (player == player1 && next == 46) || // Black entering safe zone
                (player == player4 && next == 30)) { // Green entering safe zone
                
                if (debug) System.out.println("Checking safe zone entry at " + next);
                
                // Previous position should be the one before safe zone
                if ((player == player2 && position == 62) || // Red just before safe zone
                    (player == player3 && position == 12) || // Blue just before safe zone
                    (player == player1 && position == 45) || // Black just before safe zone
                    (player == player4 && position == 29)) { // Green just before safe zone
                    
                    if (debug) System.out.println("Valid safe zone entry point detected");
                    shouldEnterSafeZone = true;
                }
            }
            
            if (shouldEnterSafeZone) {
                // Ensure we're not skipping our own safe zone
                if (!occupiedPositions.contains(next)) {
                    position = next; // Move into safe zone
                    remaining--;
                    if (debug) System.out.println("Entered safe zone at " + position);
                } else {
                    if (debug) System.out.println("Safe zone entry " + next + " is occupied");
                    break; // Can't enter if occupied
                }
                continue;
            }
            
            // Check if we're in our own safe zone
            boolean inOwnSafeZone = (next >= safeZoneStart && next <= safeZoneEnd);
            if (inOwnSafeZone) {
                if (debug) System.out.println("In own safe zone at " + next);
                
                // Check if position is already occupied
                if (occupiedPositions.contains(next)) {
                    if (debug) System.out.println("Safe zone position " + next + " is occupied");
                    break; // Can't move to occupied position
                }
                
                // Check if we're trying to go beyond the end
                if (next > safeZoneEnd) {
                    if (debug) System.out.println("Cannot go beyond safe zone end: " + safeZoneEnd);
                    position = safeZoneEnd;
                    break;
                }
                
                // Move to next position in safe zone
                position = next;
                remaining--;
                continue;
            }
            
            // Check if we're approaching another player's safe zone
            if (isOtherPlayerSafeZone(player, next)) {
                if (debug) System.out.println("Found another player's safe zone at " + next);
                
                // Jump past it
                int skipTo = -1;
                if (next >= 13 && next <= 16)      skipTo = 17; // Skip blue's safe zone
                else if (next >= 30 && next <= 33) skipTo = 34; // Skip green's safe zone
                else if (next >= 46 && next <= 49) skipTo = 50; // Skip black's safe zone
                else if (next >= 63 && next <= 66) skipTo = 67; // Skip red's safe zone
                
                if (debug) System.out.println("Skipping from " + next + " to " + skipTo);
                position = skipTo;
                remaining--;
                continue;
            }
            
            // Check for collision with own marble
            if (occupiedPositions.contains(next)) {
                if (debug) System.out.println("Position " + next + " is occupied by own marble");
                break; // Stop movement
            }
            
            // Regular move
            position = next;
            remaining--;
        }
        
        // Final collision check
        if (occupiedPositions.contains(position)) {
            if (debug) System.out.println("ERROR: Final position " + position + " occupied by own marble");
            return currentPosition; // Invalid move
        }
        
        // Final safe zone check
        if (isOtherPlayerSafeZone(player, position)) {
            if (debug) System.out.println("ERROR: Final position " + position + " in another player's safe zone");
            return currentPosition; // Invalid move
        }
        
        if (debug) {
            System.out.println("Final position: " + position);
            if (position == currentPosition) {
                System.out.println("No movement occurred!");
            }
        }
        
        return position;
    }

    /**
     * Calculate position for backward movement (negative steps)
     */

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
        if (gameOver) {
            // no further turns once the game is over
            return;
        }
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
    
    /**
     * Get the current position of a marble
     */
    public int getMarblePosition(Marble marble) {
        return marblePositions.getOrDefault(
          marble,
          getPlayerBase(findMarbleOwner(marble))
        );
    }

    /**
     * Check if a marble is still in home
     */
    public boolean isMarbleInHome(Marble marble) {
        return marblesInHome.getOrDefault(marble, false);
    }
    
    /**
     * Swap two marbles' positions (used by Jack's special move).
     * This method is kept for compatibility, but will not be used 
     * since JACK now moves 11 steps.
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

        // animate them to each other's spots
        TranslateTransition ttA = createMoveAnimation(a, 
            BoardPositions.getX(posB),
            BoardPositions.getY(posB),
            1.0, 0.0);
        
        TranslateTransition ttB = createMoveAnimation(b,
            BoardPositions.getX(posA),
            BoardPositions.getY(posA),
            1.0, 0.0);
        
        // Start the animations
        ttA.play();
        ttB.play();
    }
    
    /**
     * Calculate position for backward movement (negative steps)
     */
    private int calculateBackwardMove(Player player, int currentPosition, int steps) {
        // Make sure we're dealing with backward movement
        if (steps >= 0) {
            return calculateTargetPosition(player, currentPosition, steps);
        }

        boolean debug = (player == player2 || player == player3);
        String playerName = player.getName();
        
        if (debug) {
            System.out.println("\n==== " + playerName + " BACKWARD MOVEMENT CALCULATION ====");
            System.out.println("Starting position: " + currentPosition);
            System.out.println("Steps: " + steps);
        }
        
        // NEW CHECK: If the marble is in a safe zone, don't allow backward movement
        if (isInSafeZone(player, currentPosition)) {
            if (debug) System.out.println("Cannot move backward from safe zone position " + currentPosition);
            return currentPosition; // Return the current position (no movement)
        }
        
        // Convert to positive for easier calculation
        int absSteps = Math.abs(steps);
        int position = currentPosition;
        
        // Moving backward step by step
        for (int i = 0; i < absSteps; i++) {
            position--;
            
            // Wrap around main track
            if (position < 1) {
                position = 67;
                if (debug) System.out.println("Backward wrap from 1 to 67");
            }
            
            if (debug) System.out.println("Backward step: position " + (position+1) + " -> " + position);
            
            // Check if we're entering another player's safe zone when moving backward
            if (isOtherPlayerSafeZone(player, position)) {
                if (debug) System.out.println("Encountered other player's safe zone at " + position);
                
                // Skip past it by finding the last position before that safe zone
                int skipTo = -1;
                
                if (position >= 63 && position <= 66) skipTo = 62;      // Skip red's safe zone
                else if (position >= 46 && position <= 49) skipTo = 45; // Skip black's safe zone
                else if (position >= 30 && position <= 33) skipTo = 29; // Skip green's safe zone
                else if (position >= 13 && position <= 16) skipTo = 12; // Skip blue's safe zone
                
                if (debug) System.out.println("Skipping from " + position + " to " + skipTo);
                position = skipTo;
            }
            
            // Check for collision with own marble
            boolean occupied = false;
            for (Marble m : player.getMarbles()) {
                if (!isMarbleInHome(m) && getMarblePosition(m) != currentPosition) {
                    if (getMarblePosition(m) == position) {
                        occupied = true;
                        if (debug) System.out.println("Position " + position + " is occupied by own marble");
                        break;
                    }
                }
            }
            
            if (occupied) {
                // If collision, stop movement
                position = position + 1; // Move back to previous position
                if (position > 67) position = 1; // Handle wrap around
                break;
            }
        }
        
        if (debug) {
            System.out.println("Final backward position: " + position);
            if (position == currentPosition) {
                System.out.println("No backward movement occurred!");
            }
        }
        
        return position;
    }

    /**
     * Helper method to create a marble movement animation
     */
    private TranslateTransition createMoveAnimation(Marble marble, double targetX, double targetY, 
                                              double duration, double delay) {
        TranslateTransition tt = new TranslateTransition(
            Duration.seconds(duration), marble);
        
        tt.setDelay(Duration.seconds(delay));
        tt.setByX(targetX - marble.getCenterX());
        tt.setByY(targetY - marble.getCenterY());
        
        tt.setOnFinished(event -> {
            // Update visual position
            marble.setCenterX(targetX);
            marble.setCenterY(targetY);
            marble.setTranslateX(0);
            marble.setTranslateY(0);
        });
        
        return tt;
    }
}