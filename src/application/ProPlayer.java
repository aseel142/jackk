package application;

import java.util.ArrayList;
import java.util.List;

/**
 * ProPlayer - Focuses exclusively on the shortcut strategy using ACE -> FOUR -> Safe Zone entry
 * With additional focus on prioritizing marbles close to the safe zone
 */
public class ProPlayer extends Player {
    
    private boolean shortcutActive = false;
    private Marble shortcutMarble = null;
    
    // Maximum distance to consider a marble "close" to safe zone
    private static final int CLOSE_TO_SAFEZONE_THRESHOLD = 10;
    
    public ProPlayer(String name) {
        super(name);
    }
    
    @Override
    public void takeTurn(Board board) {
        System.out.println("\n" + name + ".takeTurn() with hand: " + cards);
        
        // Check if we have no cards left
        if (cards.isEmpty()) {
            board.nextTurn();
            return;
        }
        
        // Get the card to play based on our priority strategy
        Card cardToPlay = selectBestCard(board);
        
        System.out.println(name + " is playing card: " + cardToPlay);
        board.playCard(this, cardToPlay);
    }
    
    /**
     * Select the best card to play based on the current board state and our strategy
     */
    private Card selectBestCard(Board board) {
        // PRIORITY 1: If we have a marble on base and a FOUR card, always play the FOUR
        Marble baseMarble = findMarbleOnBase(board);
        if (baseMarble != null) {
            for (Card card : cards) {
                if (card.getValue() == Card.Value.FOUR) {
                    System.out.println(name + " playing FOUR to move marble backward from base - HIGHEST PRIORITY");
                    shortcutActive = true; 
                    shortcutMarble = baseMarble;
                    return card;
                }
            }
        }
        
        // PRIORITY 2: Check for marbles close to safe zone (within 10 steps)
        Marble closeToSafeZoneMarble = findMarbleCloseToSafeZone(board, CLOSE_TO_SAFEZONE_THRESHOLD);
        if (closeToSafeZoneMarble != null) {
            int pos = board.getMarblePosition(closeToSafeZoneMarble);
            int stepsNeeded = distanceToSafeZone(board, pos);
            
            // Find the card that gets us closest to or into the safe zone
            Card bestCard = findBestCardForSafeZoneEntry(board, closeToSafeZoneMarble, stepsNeeded);
            if (bestCard != null) {
                System.out.println(name + " playing " + bestCard + " to move marble close to safe zone");
                return bestCard;
            }
        }
        
        // PRIORITY 3: If we have ACE/KING and no marble on base and at least one marble in home, play ACE/KING
        if (baseMarble == null && hasMarbleInHome(board)) {
            for (Card card : cards) {
                if (card.getValue() == Card.Value.ACE || card.getValue() == Card.Value.KING) {
                    System.out.println(name + " playing ACE/KING to bring marble to base");
                    return card;
                }
            }
        }
        
        // PRIORITY 4: If we have a marble at position after backward movement
        // and we have a card that can move it into the safe zone, play it
        if (shortcutActive && shortcutMarble != null) {
            int pos = board.getMarblePosition(shortcutMarble);
            int backwardPos = getShortcutBackwardPosition();
            
            if (pos == backwardPos) {
                int stepsNeeded = distanceToSafeZone(board, pos);
                
                // Find a card that moves us closer to or into the safe zone
                for (Card card : cards) {
                    int steps = getStepsForCard(card);
                    
                    // Skip backward movement cards 
                    if (steps <= 0) continue;
                    
                    // Try to find a card that gets us closer
                    if (steps > 0) {
                        System.out.println(name + " playing card to move toward safe zone from shortcut position");
                        return card;
                    }
                }
            }
        }
        
        // PRIORITY 5: Look for a card to enter the safe zone if we have a marble at the entry point
        Marble entryMarble = findMarbleAtSafeZoneEntry(board);
        if (entryMarble != null) {
            for (Card card : cards) {
                if (getStepsForCard(card) == 1) {
                    System.out.println(name + " playing card to enter safe zone from entry point");
                    return card;
                }
            }
        }
        
        // PRIORITY 6: Look for opportunities to capture
        Card captureCard = findCaptureCard(board);
        if (captureCard != null) {
            System.out.println(name + " playing card to capture opponent's marble");
            return captureCard;
        }
        
        // PRIORITY 7: Default to the first card if no strategy applies
        System.out.println(name + " playing default card");
        return cards.get(0);
    }
    
    /**
     * Find the best card to move a marble into or closer to the safe zone
     */
    private Card findBestCardForSafeZoneEntry(Board board, Marble marble, int stepsNeeded) {
        Card bestCard = null;
        int bestScore = Integer.MIN_VALUE;
        
        for (Card card : cards) {
            int steps = getStepsForCard(card);
            
            // Skip backward moving cards
            if (steps <= 0) continue;
            
            int currPos = board.getMarblePosition(marble);
            int targetPos = board.calculateTargetPosition(this, currPos, steps);
            
            // Skip if target equals current (no movement possible)
            if (targetPos == currPos) continue;
            
            // Calculate a score based on how good this move is
            int score = evaluateSafeZoneMove(board, currPos, targetPos, steps, stepsNeeded);
            
            if (score > bestScore) {
                bestScore = score;
                bestCard = card;
            }
        }
        
        return bestCard;
    }
    
    /**
     * Evaluate how good a move is for getting to the safe zone
     */
    private int evaluateSafeZoneMove(Board board, int currentPos, int targetPos, int steps, int stepsNeeded) {
        int score = 0;
        
        // Check if this move gets us into the safe zone
        if (board.isInSafeZone(this, targetPos)) {
            score += 1000; // Huge bonus for entering safe zone
        }
        
        // Check if this gets us exactly to the safe zone entry
        if (targetPos == getSafeZoneStart()) {
            score += 500; // Big bonus for landing exactly at entry point
        }
        
        // Calculate how much closer this gets us to the safe zone
        int currentDist = distanceToSafeZone(board, currentPos);
        int targetDist = distanceToSafeZone(board, targetPos);
        int improvement = currentDist - targetDist;
        
        // Bonus for getting closer to safe zone
        score += improvement * 50;
        
        // Prefer cards that use more of their steps effectively
        if (steps <= stepsNeeded) {
            score += 10 * steps; // Bonus proportional to steps used
        } else {
            // Penalty for using a card with more steps than needed
            // but still maintaining proximity bonus
            score += 10 * stepsNeeded - (steps - stepsNeeded) * 5;
        }
        
        return score;
    }
    
    @Override
    public void makeMove(Board board, Card card) {
        System.out.println("\n" + name + ".makeMove() with card: " + card);
        int steps = getStepsForCard(card);
        
        // PRIORITY 1: Marble close to safe zone (within 10 steps)
        if (steps > 0) {
            Marble closeToSafeZoneMarble = findMarbleCloseToSafeZone(board, CLOSE_TO_SAFEZONE_THRESHOLD);
            if (closeToSafeZoneMarble != null) {
                int currPos = board.getMarblePosition(closeToSafeZoneMarble);
                int targetPos = board.calculateTargetPosition(this, currPos, steps);
                
                // Only move if it changes position
                if (targetPos != currPos) {
                    System.out.println(name + " moving marble close to safe zone from " + currPos + " to " + targetPos);
                    board.moveMarbleToPosition(closeToSafeZoneMarble, targetPos, 1.0, 0.0);
                    return;
                }
            }
        }
        
        // PRIORITY 2: ACE/KING to bring marble out from home
        if ((card.getValue() == Card.Value.ACE || card.getValue() == Card.Value.KING) && 
            hasMarbleInHome(board)) {
            
            // First check if base is already occupied
            Marble baseMarble = findMarbleOnBase(board);
            if (baseMarble == null) {
                // Base is free, move a marble from home to base
                Marble homeMarble = getFirstMarbleInHome(board);
                System.out.println(name + " moving marble from home to base");
                board.moveMarbleToPosition(homeMarble, getBasePosition(), 1.0, 0.0);
                return;
            } else {
                System.out.println(name + " BASE IS ALREADY OCCUPIED - cannot place marble there");
            }
        }
        
        // PRIORITY 3: FOUR to move backward from base
        if (card.getValue() == Card.Value.FOUR) {
            Marble baseMarble = findMarbleOnBase(board);
            if (baseMarble != null) {
                // Override board's calculation with our predetermined position
                int targetPos = getShortcutBackwardPosition();
                System.out.println(name + " moving marble backward from base to " + targetPos);
                board.moveMarbleToPosition(baseMarble, targetPos, 1.0, 0.0);
                return;
            }
        }
        
        // PRIORITY 4: When we have a marble at shortcut position
        if (shortcutActive && shortcutMarble != null) {
            int pos = board.getMarblePosition(shortcutMarble);
            int backwardPos = getShortcutBackwardPosition();
            
            if (pos == backwardPos) {
                // If we're at the shortcut position, calculate target position normally
                int targetPos = board.calculateTargetPosition(this, pos, steps);
                
                // Only move if it leads to a new position
                if (targetPos != pos) {
                    System.out.println(name + " moving marble from shortcut position " + pos + " to " + targetPos);
                    board.moveMarbleToPosition(shortcutMarble, targetPos, 1.0, 0.0);
                    
                    // If we reach the safe zone, reset the shortcut tracking
                    if (board.isInSafeZone(this, targetPos)) {
                        shortcutActive = false;
                        shortcutMarble = null;
                    }
                    return;
                }
            }
        }
        
        // PRIORITY 5: Enter safe zone from entry point
        Marble entryMarble = findMarbleAtSafeZoneEntry(board);
        if (entryMarble != null && steps > 0) {
            int currPos = board.getMarblePosition(entryMarble);
            int targetPos = board.calculateTargetPosition(this, currPos, steps);
            
            // Move into or through safe zone
            if (targetPos != currPos) {
                System.out.println(name + " entering or moving through safe zone from entry point");
                board.moveMarbleToPosition(entryMarble, targetPos, 1.0, 0.0);
                return;
            }
        }
        
        // PRIORITY 6: Capture an opponent's marble
        Marble captureMarble = findMarbleForCapture(board, steps);
        if (captureMarble != null) {
            int currPos = board.getMarblePosition(captureMarble);
            int targetPos = board.calculateTargetPosition(this, currPos, steps);
            System.out.println(name + " capturing opponent's marble");
            board.moveMarbleToPosition(captureMarble, targetPos, 1.0, 0.0);
            return;
        }
        
        // PRIORITY 7: Move a marble that's in safe zone
        Marble safeMarble = findMarbleInSafeZone(board);
        if (safeMarble != null && steps > 0) {
            int currPos = board.getMarblePosition(safeMarble);
            int targetPos = board.calculateTargetPosition(this, currPos, steps);
            if (targetPos != currPos) {
                System.out.println(name + " advancing marble in safe zone");
                board.moveMarbleToPosition(safeMarble, targetPos, 1.0, 0.0);
                return;
            }
        }
        
        // PRIORITY 8: Move any marble on the board
        if (hasMarbleOnBoard(board)) {
            // Find the best marble to move
            Marble bestMarble = findBestMarbleToMove(board, steps);
            if (bestMarble != null) {
                int currPos = board.getMarblePosition(bestMarble);
                int targetPos = board.calculateTargetPosition(this, currPos, steps);
                System.out.println(name + " moving best marble from " + currPos + " to " + targetPos);
                board.moveMarbleToPosition(bestMarble, targetPos, 1.0, 0.0);
                return;
            }
        }
        
        // If no valid move, pass turn
        System.out.println(name + " has no valid moves, passing turn");
        board.nextTurn();
    }
    
    /**
     * Find a marble that is within the specified number of steps from the safe zone
     */
    private Marble findMarbleCloseToSafeZone(Board board, int threshold) {
        Marble closestMarble = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int pos = board.getMarblePosition(m);
                
                // Skip if already in safe zone
                if (board.isInSafeZone(this, pos)) continue;
                
                int distance = distanceToSafeZone(board, pos);
                
                // Check if within threshold and closer than any found so far
                if (distance <= threshold && distance < minDistance) {
                    minDistance = distance;
                    closestMarble = m;
                }
            }
        }
        
        if (closestMarble != null) {
            int pos = board.getMarblePosition(closestMarble);
            System.out.println(name + " found marble at position " + pos + 
                              " that is " + minDistance + " steps from safe zone");
        }
        
        return closestMarble;
    }
    
    /**
     * Calculate the distance to reach the player's safe zone from a position
     */
    private int distanceToSafeZone(Board board, int position) {
        // Skip calculation if already in safe zone
        if (board.isInSafeZone(this, position)) {
            return 0;
        }
        
        int safeZoneEntry = getSafeZoneStart();
        
        // For player 2 (red)
        if (name.equalsIgnoreCase("player2")) {
            // Safe zone starts at 63
            if (position < 63) {
                return 63 - position;
            } else {
                // Need to go around the board
                return (67 - position) + 1 + 63;
            }
        }
        // For player 1 (black)
        else if (name.equalsIgnoreCase("player1")) {
            // Safe zone starts at 46
            if (position < 46) {
                return 46 - position;
            } else {
                // Need to go around the board
                return (67 - position) + 1 + 46;
            }
        }
        // For player 3 (blue)
        else if (name.equalsIgnoreCase("player3")) {
            // Safe zone starts at 13
            if (position < 13) {
                return 13 - position;
            } else {
                // Need to go around the board
                return (67 - position) + 1 + 13;
            }
        }
        // For player 4 (green)
        else if (name.equalsIgnoreCase("player4")) {
            // Safe zone starts at 30
            if (position < 30) {
                return 30 - position;
            } else {
                // Need to go around the board
                return (67 - position) + 1 + 30;
            }
        }
        
        return Integer.MAX_VALUE; // Default fallback
    }
    
    /**
     * Find the best marble to move based on proximity to safe zone
     */
    private Marble findBestMarbleToMove(Board board, int steps) {
        // Skip for backward movement
        if (steps <= 0) {
            return findAnyMarbleNotInSafeZone(board);
        }
        
        Marble bestMarble = null;
        int bestScore = Integer.MIN_VALUE;
        
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int currPos = board.getMarblePosition(m);
                int targetPos = board.calculateTargetPosition(this, currPos, steps);
                
                if (targetPos != currPos) { // Can move
                    int score = evaluateMove(board, currPos, targetPos);
                    if (score > bestScore) {
                        bestScore = score;
                        bestMarble = m;
                    }
                }
            }
        }
        
        // If no valid move found, try any marble
        if (bestMarble == null) {
            for (Marble m : marbles) {
                if (!board.isMarbleInHome(m)) {
                    int currPos = board.getMarblePosition(m);
                    int targetPos = board.calculateTargetPosition(this, currPos, steps);
                    
                    if (targetPos != currPos) { // Can move
                        return m; // Just return the first movable marble
                    }
                }
            }
        }
        
        return bestMarble;
    }
    
    /**
     * Find any marble that is not in a safe zone
     */
    private Marble findAnyMarbleNotInSafeZone(Board board) {
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int pos = board.getMarblePosition(m);
                if (!board.isInSafeZone(this, pos)) {
                    return m;
                }
            }
        }
        return null;
    }
    
    /**
     * Evaluate a move based on proximity to safe zone
     */
    private int evaluateMove(Board board, int currentPos, int targetPos) {
        int score = 0;
        
        // Prefer moves that get closer to safe zone
        int currentDist = distanceToSafeZone(board, currentPos);
        int targetDist = distanceToSafeZone(board, targetPos);
        
        score += (currentDist - targetDist) * 10;
        
        // Huge bonus if entering safe zone
        if (!board.isInSafeZone(this, currentPos) && board.isInSafeZone(this, targetPos)) {
            score += 500;
        }
        
        // Bonus if moving further along in safe zone
        if (board.isInSafeZone(this, currentPos) && board.isInSafeZone(this, targetPos) && targetPos > currentPos) {
            score += 300;
        }
        
        // Bonus for landing exactly at safe zone entry point
        if (targetPos == getSafeZoneStart()) {
            score += 200;
        }
        
        return score;
    }
    
    /**
     * Find a card that can capture an opponent's marble
     */
    private Card findCaptureCard(Board board) {
        for (Card card : cards) {
            int steps = getStepsForCard(card);
            if (findMarbleForCapture(board, steps) != null) {
                return card;
            }
        }
        return null;
    }
    
    /**
     * Find a marble that can capture an opponent's marble with the given steps
     */
    private Marble findMarbleForCapture(Board board, int steps) {
        // Skip for backward movement
        if (steps <= 0) return null;
        
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int currPos = board.getMarblePosition(m);
                int targetPos = board.calculateTargetPosition(this, currPos, steps);
                
                if (targetPos != currPos && isOpponentMarbleAtPosition(board, targetPos)) {
                    return m;
                }
            }
        }
        return null;
    }
    
    /**
     * Check if there's an opponent's marble at the specified position
     */
    private boolean isOpponentMarbleAtPosition(Board board, int position) {
        // Check each player
        for (int i = 0; i < 4; i++) {
            Player player = board.getPlayerByIndex(i);
            
            // Skip our own marbles and partner's marbles
            if (player == this) continue;
            if ((this == board.getPlayerByIndex(0) && player == board.getPlayerByIndex(2)) ||
                (this == board.getPlayerByIndex(2) && player == board.getPlayerByIndex(0)) ||
                (this == board.getPlayerByIndex(1) && player == board.getPlayerByIndex(3)) ||
                (this == board.getPlayerByIndex(3) && player == board.getPlayerByIndex(1))) {
                continue;
            }
            
            // Check if any of the player's marbles are at this position and not in safe zone
            for (Marble m : player.getMarbles()) {
                if (!board.isMarbleInHome(m) && 
                    board.getMarblePosition(m) == position &&
                    !board.isInSafeZone(player, position)) {
                    return true;
                }
            }
        }
        return false;
    }

    // -------------------------- HELPER METHODS --------------------------
    
    /**
     * Find a marble that is at the safe zone entry point
     */
    private Marble findMarbleAtSafeZoneEntry(Board board) {
        int entryPoint = getSafeZoneEntryPoint();
        
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int pos = board.getMarblePosition(m);
                if (pos == entryPoint) {
                    return m;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Find a marble that is already in the safe zone
     */
    private Marble findMarbleInSafeZone(Board board) {
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int pos = board.getMarblePosition(m);
                if (board.isInSafeZone(this, pos)) {
                    return m;
                }
            }
        }
        
        return null;
    }
    
    private boolean hasMarbleOnBoard(Board board) {
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasMarbleInHome(Board board) {
        for (Marble m : marbles) {
            if (board.isMarbleInHome(m)) {
                return true;
            }
        }
        return false;
    }
    
    private Marble getFirstMarbleInHome(Board board) {
        for (Marble m : marbles) {
            if (board.isMarbleInHome(m)) {
                return m;
            }
        }
        return null;
    }
    
    private Marble findMarbleOnBase(Board board) {
        int basePos = getBasePosition();
        
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int pos = board.getMarblePosition(m);
                if (pos == basePos) {
                    return m;
                }
            }
        }
        return null;
    }
    
    private int getBasePosition() {
        switch (name.toLowerCase()) {
            case "player1": return 51;
            case "player2": return 1;
            case "player3": return 18;
            case "player4": return 35;
            default: return 1;
        }
    }
    
    private int getSafeZoneStart() {
        switch (name.toLowerCase()) {
            case "player1": return 46;
            case "player2": return 63;
            case "player3": return 13;
            case "player4": return 30;
            default: return 1;
        }
    }
    
    private int getSafeZoneEntryPoint() {
        switch (name.toLowerCase()) {
            case "player1": return 45; // Position before safe zone (46-49)
            case "player2": return 62; // Position before safe zone (63-66)
            case "player3": return 12; // Position before safe zone (13-16)
            case "player4": return 29; // Position before safe zone (30-33)
            default: return 1;
        }
    }
    
    private int getStepsForCard(Card card) {
        switch (card.getValue()) {
            case ACE:   return 1;
            case TWO:   return 2;
            case THREE: return 3;
            case FOUR:  return -4;
            case FIVE:  return 5;
            case SIX:   return 6;
            case SEVEN: return 7;
            case EIGHT: return 8;
            case NINE:  return 9;
            case TEN:   return 10;
            case JACK:  return 11;
            case QUEEN: return 12;
            case KING:  return 13;
            default:    return 0;
        }
    }
    
    /**
     * Get the backward position after playing a FOUR from base
     */
    private int getShortcutBackwardPosition() {
        switch (name.toLowerCase()) {
            case "player1": // Base at 51, moving backward 4 spaces
                return 43; // 51 → 50 → 45 → 44 → 43 (Skip safe zone 46-49)
            case "player2": // Base at 1, moving backward 4 spaces
                return 60; // 1 → 67 → 62 → 61 → 60 (Skip safe zone 63-66)
            case "player3": // Base at 18, moving backward 4 spaces
                return 10; // 18 → 17 → 12 → 11 → 10 (Skip safe zone 13-16)
            case "player4": // Base at 35, moving backward 4 spaces
                return 27; // 35 → 34 → 29 → 28 → 27 (Skip safe zone 30-33)
            default: return 60;
        }
    }
}