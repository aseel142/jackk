package application;

import java.util.ArrayList;
import java.util.List;

/**
 * ProPlayer - Focuses exclusively on the shortcut strategy using ACE -> FOUR -> Safe Zone entry
 * With additional safeguards to prevent stacking marbles and skipping safe zones
 */
public class ProPlayer extends Player {
    
    private boolean shortcutActive = false;
    private Marble shortcutMarble = null;
    
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
        // HIGHEST PRIORITY: If we have a marble on base and a FOUR card, always play the FOUR
        Marble baseMarble = findMarbleOnBase(board);
        if (baseMarble != null) {
            for (Card card : cards) {
                if (card.getValue() == Card.Value.FOUR) {
                    System.out.println(name + " playing FOUR to move marble backward from base");
                    shortcutActive = true; 
                    shortcutMarble = baseMarble;
                    return card;
                }
            }
        }
        
        // If we have ACE and no marble on base and at least one marble in home, play ACE
        if (baseMarble == null && hasMarbleInHome(board)) {
            for (Card card : cards) {
                if (card.getValue() == Card.Value.ACE || card.getValue() == Card.Value.KING) {
                    System.out.println(name + " playing ACE/KING to bring marble to base");
                    return card;
                }
            }
        }
        
        // If we have a marble at position after backward movement (e.g., position 60 for player 2)
        // and we have a card that can move it into the safe zone, play it
        if (shortcutActive && shortcutMarble != null) {
            int pos = board.getMarblePosition(shortcutMarble);
            int backwardPos = getShortcutBackwardPosition();
            
            if (pos == backwardPos) {
                // For player 2, from position 60, we need at least 3 steps to reach safe zone
                int stepsNeeded = getStepsToSafeZone(pos);
                
                // Find a card that gets us exactly to or beyond the safe zone entry
                // but not skipping the safe zone entirely
                for (Card card : cards) {
                    int steps = getStepsForCard(card);
                    
                    // We want cards that move at least the needed steps,
                    // but for safety, not too many steps beyond the safe zone end
                    if (steps >= stepsNeeded && !wouldSkipSafeZone(pos, steps)) {
                        System.out.println(name + " playing card to enter safe zone from shortcut position");
                        return card;
                    }
                }
                
                // If no ideal card, find any card that moves us closer to the safe zone
                for (Card card : cards) {
                    int steps = getStepsForCard(card);
                    if (steps > 0 && steps < stepsNeeded) {
                        System.out.println(name + " playing card to move toward safe zone from shortcut position");
                        return card;
                    }
                }
            }
        }
        
        // Look for a card to enter the safe zone if we have a marble at the entry point
        Marble entryMarble = findMarbleAtSafeZoneEntry(board);
        if (entryMarble != null) {
            for (Card card : cards) {
                if (getStepsForCard(card) == 1) {
                    System.out.println(name + " playing card to enter safe zone from entry point");
                    return card;
                }
            }
        }
        
        // Look for opportunities to capture
        Card captureCard = findCaptureCard(board);
        if (captureCard != null) {
            System.out.println(name + " playing card to capture opponent's marble");
            return captureCard;
        }
        
        // Default to the first card if no strategy applies
        System.out.println(name + " playing default card");
        return cards.get(0);
    }
    
    @Override
    public void makeMove(Board board, Card card) {
        System.out.println("\n" + name + ".makeMove() with card: " + card);
        int steps = getStepsForCard(card);
        
        // CASE 1: ACE/KING to bring marble out from home
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
        
        // CASE 2: FOUR to move backward from base
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
        
        // CASE 3: When we have a marble at shortcut position (e.g., position 60 for player 2)
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
                    if (isSafeZonePosition(targetPos)) {
                        shortcutActive = false;
                        shortcutMarble = null;
                    }
                    return;
                }
            }
        }
        
        // CASE 4: Enter safe zone from entry point
        Marble entryMarble = findMarbleAtSafeZoneEntry(board);
        if (entryMarble != null && steps == 1) {
            int currPos = board.getMarblePosition(entryMarble);
            int targetPos = board.calculateTargetPosition(this, currPos, steps);
            System.out.println(name + " entering safe zone from entry point");
            board.moveMarbleToPosition(entryMarble, targetPos, 1.0, 0.0);
            return;
        }
        
        // CASE 5: Capture an opponent's marble
        Marble captureMarble = findMarbleForCapture(board, steps);
        if (captureMarble != null) {
            int currPos = board.getMarblePosition(captureMarble);
            int targetPos = board.calculateTargetPosition(this, currPos, steps);
            System.out.println(name + " capturing opponent's marble");
            board.moveMarbleToPosition(captureMarble, targetPos, 1.0, 0.0);
            return;
        }
        
        // CASE 6: Move a marble that's in safe zone
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
        
        // CASE 7: Move any marble on the board
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
     * Find the best marble to move based on proximity to safe zone
     */
    private Marble findBestMarbleToMove(Board board, int steps) {
        Marble bestMarble = null;
        int bestScore = Integer.MIN_VALUE;
        
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int currPos = board.getMarblePosition(m);
                int targetPos = board.calculateTargetPosition(this, currPos, steps);
                
                if (targetPos != currPos) { // Can move
                    int score = evaluateMove(currPos, targetPos);
                    if (score > bestScore) {
                        bestScore = score;
                        bestMarble = m;
                    }
                }
            }
        }
        
        return bestMarble;
    }
    
    /**
     * Evaluate a move based on proximity to safe zone
     */
    private int evaluateMove(int currentPos, int targetPos) {
        int score = 0;
        
        // Prefer moves that get closer to safe zone
        int currentDist = calculateDistanceToSafeZone(currentPos);
        int targetDist = calculateDistanceToSafeZone(targetPos);
        
        score += (currentDist - targetDist) * 10;
        
        // Avoid moves that skip the safe zone entry
        if (wouldSkipSafeZone(currentPos, targetPos)) {
            score -= 50;
        }
        
        return score;
    }
    
    /**
     * Check if moving with the given steps would skip the safe zone
     */
    private boolean wouldSkipSafeZone(int currentPos, int steps) {
        // For player 2 (red)
        if (name.equalsIgnoreCase("player2")) {
            if (currentPos == 60 && steps > 6) {
                // From position 60, more than 6 steps would skip safe zone (63-66)
                return true;
            }
            if (currentPos == 61 && steps > 5) {
                return true;
            }
            if (currentPos == 62 && steps > 4) {
                return true;
            }
        }
        
        // For player 1 (black)
        else if (name.equalsIgnoreCase("player1")) {
            if (currentPos == 43 && steps > 6) {
                return true;
            }
            if (currentPos == 44 && steps > 5) {
                return true;
            }
            if (currentPos == 45 && steps > 4) {
                return true;
            }
        }
        
        // For player 3 (blue)
        else if (name.equalsIgnoreCase("player3")) {
            if (currentPos == 10 && steps > 6) {
                return true;
            }
            if (currentPos == 11 && steps > 5) {
                return true;
            }
            if (currentPos == 12 && steps > 4) {
                return true;
            }
        }
        
        // For player 4 (green)
        else if (name.equalsIgnoreCase("player4")) {
            if (currentPos == 27 && steps > 6) {
                return true;
            }
            if (currentPos == 28 && steps > 5) {
                return true;
            }
            if (currentPos == 29 && steps > 4) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get the number of steps needed to reach the safe zone from a position
     */
    private int getStepsToSafeZone(int position) {
        // For player 2 (red)
        if (name.equalsIgnoreCase("player2")) {
            if (position == 60) return 3; // From position 60 to safe zone start (63)
            if (position == 61) return 2;
            if (position == 62) return 1;
        }
        
        // For player 1 (black)
        else if (name.equalsIgnoreCase("player1")) {
            if (position == 43) return 3; // From position 43 to safe zone start (46)
            if (position == 44) return 2;
            if (position == 45) return 1;
        }
        
        // For player 3 (blue)
        else if (name.equalsIgnoreCase("player3")) {
            if (position == 10) return 3; // From position 10 to safe zone start (13)
            if (position == 11) return 2;
            if (position == 12) return 1;
        }
        
        // For player 4 (green)
        else if (name.equalsIgnoreCase("player4")) {
            if (position == 27) return 3; // From position 27 to safe zone start (30)
            if (position == 28) return 2;
            if (position == 29) return 1;
        }
        
        return -1; // Position not near safe zone
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
                if (isSafeZonePosition(pos)) {
                    return m;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Check if a position is in our safe zone
     */
    private boolean isSafeZonePosition(int position) {
        int safeStart = getSafeZoneStart();
        int safeEnd = safeStart + 3; // 4 positions in each safe zone
        
        return position >= safeStart && position <= safeEnd;
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
    
    /**
     * Calculate the distance to safe zone
     */
    private int calculateDistanceToSafeZone(int position) {
        int safeEntry = getSafeZoneEntryPoint();
        
        // Simple distance calculation - can be improved for board wrapping
        if (position > safeEntry) {
            return (67 - position) + safeEntry + 1; // Wrap around the board
        } else {
            return safeEntry - position;
        }
    }
}