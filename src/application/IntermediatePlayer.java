package application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class IntermediatePlayer extends Player {
    private Random random = new Random();

    public IntermediatePlayer(String name) {
        super(name);
    }

    @Override
    public void takeTurn(Board board) {
        System.out.println(name + ".takeTurn(); hand=" + cards);
        
        // 1) If no cards left, skip immediately
        if (cards.isEmpty()) {
            board.nextTurn();
            return;
        }

        // ENHANCEMENT: Find the highest value card to play
        Card cardToPlay = findBestCardToPlay(board);
        
        System.out.println(name + " is playing card: " + cardToPlay);
        board.playCard(this, cardToPlay);
    }
    
    /**
     * Find the best card to play based on the current board state
     */
    private Card findBestCardToPlay(Board board) {
        // If we have no marbles on board, prioritize ACE or KING to get a marble out
        if (!hasMarbleOnBoard(board) && hasMarbleInHome(board)) {
            for (Card card : cards) {
                if (card.getValue() == Card.Value.ACE || card.getValue() == Card.Value.KING) {
                    return card;
                }
            }
        }
        
        // Check if any of our marbles are approaching the safe zone entry point
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int pos = board.getMarblePosition(m);
                int safeZoneEntry = getSafeZoneEntryPoint();
                
                // If marble is at the position just before safe zone entry
                if (isApproachingSafeZone(pos)) {
                    System.out.println(name + " detected marble approaching safe zone at position " + pos);
                    
                    // Find a card that can move this marble exactly into the safe zone
                    int stepsNeeded = getStepsToSafeZone(pos);
                    for (Card card : cards) {
                        if (getStepsForCard(card) == stepsNeeded) {
                            System.out.println(name + " choosing card " + card + " to enter safe zone");
                            return card;
                        }
                    }
                }
            }
        }
        
        // Otherwise, find a card that can capture an opponent's marble
        for (Card card : cards) {
            if (canCaptureWithCard(board, card)) {
                return card;
            }
        }
        
        // If no capture is possible, find the best strategic card
        Card bestCard = null;
        int highestScore = Integer.MIN_VALUE;
        
        for (Card card : cards) {
            int score = evaluateCardForMove(board, card);
            if (score > highestScore) {
                highestScore = score;
                bestCard = card;
            }
        }
        
        return bestCard != null ? bestCard : cards.get(0); // Fallback to first card
    }
    
    /**
     * Evaluate how good a card is for moving (not capturing)
     */
    private int evaluateCardForMove(Board board, Card card) {
        int steps = getStepsForCard(card);
        int score = steps; // Base score is the number of steps
        
        // Check if this card would move any marble into a better position
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int currPos = board.getMarblePosition(m);
                int targetPos = board.calculateTargetPosition(this, currPos, steps);
                
                // Bonus if moving closer to safe zone
                if (isCloserToSafeZone(targetPos, currPos)) {
                    score += 10;
                }
                
                // Penalty if skipping safe zone entry
                if (wouldSkipSafeZone(currPos, steps)) {
                    score -= 50;
                }
                
                // Penalty if would land on another of our marbles (wasted move)
                if (isOwnMarbleAtPosition(board, targetPos)) {
                    score -= 20;
                }
            }
        }
        
        return score;
    }
    
    /**
     * Check if this player has any marbles on the board
     */
    private boolean hasMarbleOnBoard(Board board) {
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if we can capture an opponent's marble with this card
     */
    private boolean canCaptureWithCard(Board board, Card card) {
        int steps = getStepsForCard(card);
        
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int currPos = board.getMarblePosition(m);
                int targetPos = board.calculateTargetPosition(this, currPos, steps);
                
                // Check if any opponent's marble is at the target position
                if (isOpponentMarbleAtPosition(board, targetPos)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check if there's an opponent's marble at the specified position
     */
    private boolean isOpponentMarbleAtPosition(Board board, int position) {
        // Check each player's marbles
        for (int i = 0; i < 4; i++) {
            Player player = board.getPlayerByIndex(i);
            
            // Skip our own marbles
            if (player == this) {
                continue;
            }
            
            // Skip partner's marbles (assuming team play: player 0 & 2, player 1 & 3)
            // Adjust this logic if team structure is different
            if ((this == board.getPlayerByIndex(0) && player == board.getPlayerByIndex(2)) ||
                (this == board.getPlayerByIndex(2) && player == board.getPlayerByIndex(0)) ||
                (this == board.getPlayerByIndex(1) && player == board.getPlayerByIndex(3)) ||
                (this == board.getPlayerByIndex(3) && player == board.getPlayerByIndex(1))) {
                continue;
            }
            
            // Check if any of the player's marbles are at this position
            for (Marble m : player.getMarbles()) {
                if (!board.isMarbleInHome(m) && board.getMarblePosition(m) == position &&
                    !board.isInSafeZone(player, position)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check if one of our own marbles is at the specified position
     */
    private boolean isOwnMarbleAtPosition(Board board, int position) {
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m) && board.getMarblePosition(m) == position) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get the position just before the safe zone entry
     */
    private int getSafeZoneEntryPoint() {
        switch (name.toLowerCase()) {
            case "player1": return 45; // Position before safe zone (46-49)
            case "player2": return 62; // Position before safe zone (63-66)
            case "player3": return 12; // Position before safe zone (13-16)
            case "player4": return 29; // Position before safe zone (30-33)
            default: return 1;
        }
    }
    
    /**
     * Check if a marble is at a position approaching safe zone entry
     */
    private boolean isApproachingSafeZone(int position) {
        int entryPoint = getSafeZoneEntryPoint();
        return position == entryPoint;
    }
    
    /**
     * Get the number of steps needed to enter the safe zone from the current position
     */
    private int getStepsToSafeZone(int position) {
        if (position == getSafeZoneEntryPoint()) {
            return 1; // If at the entry point, need 1 step to enter
        }
        return -1; // Default value if not applicable
    }
    
    /**
     * Check if target position is closer to safe zone than current position
     */
    private boolean isCloserToSafeZone(int targetPos, int currentPos) {
        int safeStart = getSafeZoneStart();
        
        // If already in safe zone, check if moving further in
        if (isSafeZonePosition(currentPos)) {
            return targetPos > currentPos;
        }
        
        // Otherwise check if closer to safe zone entry point
        int currentDist = calculateDistanceToSafeZone(currentPos, safeStart);
        int targetDist = calculateDistanceToSafeZone(targetPos, safeStart);
        
        return targetDist < currentDist;
    }
    
    /**
     * Check if a position is in our safe zone
     */
    private boolean isSafeZonePosition(int position) {
        int safeStart = getSafeZoneStart();
        int safeEnd = safeStart + 3; // 4 positions in each safe zone
        
        return position >= safeStart && position <= safeEnd;
    }
    
    /**
     * Check if moving the given number of steps from the current position
     * would make the marble skip its safe zone entry
     */
    private boolean wouldSkipSafeZone(int currentPos, int steps) {
        int entryPoint = getSafeZoneEntryPoint();
        int safeStart = getSafeZoneStart();
        
        // If the marble is at the entry point, we should never skip
        if (currentPos == entryPoint) {
            return false;
        }
        
        // For player 2 (red), check if moving from position 61 or 62 with steps that would skip entry
        if (name.equalsIgnoreCase("player2")) {
            // Position 62 is just before safe zone entry (63)
            if (currentPos == 61 && steps >= 2) {
                System.out.println("Player2 would skip safe zone by moving " + steps + " steps from position 61");
                return true;
            }
            if (currentPos == 60 && steps >= 3) {
                System.out.println("Player2 would skip safe zone by moving " + steps + " steps from position 60");
                return true;
            }
        }
        
        // For player 1 (black)
        else if (name.equalsIgnoreCase("player1")) {
            // Position 45 is just before safe zone entry (46)
            if (currentPos == 44 && steps >= 2) {
                return true;
            }
            if (currentPos == 43 && steps >= 3) {
                return true;
            }
        }
        
        // For player 3 (blue)
        else if (name.equalsIgnoreCase("player3")) {
            // Position 12 is just before safe zone entry (13)
            if (currentPos == 11 && steps >= 2) {
                return true;
            }
            if (currentPos == 10 && steps >= 3) {
                return true;
            }
        }
        
        // For player 4 (green)
        else if (name.equalsIgnoreCase("player4")) {
            // Position 29 is just before safe zone entry (30)
            if (currentPos == 28 && steps >= 2) {
                return true;
            }
            if (currentPos == 27 && steps >= 3) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public void makeMove(Board board, Card card) {
        System.out.println("\n" + name + ".makeMove() with card: " + card);
        boolean moved = false;
        int steps = getStepsForCard(card);
        
        // PRIORITY 1: If we have ACE/KING and marbles in home, bring one out
        if (hasMarbleInHome(board) &&
           (card.getValue() == Card.Value.ACE || card.getValue() == Card.Value.KING)) {
            
            // Check if base is free
            Marble baseMarble = findMarbleOnBase(board);
            if (baseMarble == null) {
                Marble m = getFirstMarbleInHome(board);
                System.out.println(name + " bringing marble out from home to base position " + getBasePosition());
                board.moveMarbleToPosition(m, getBasePosition(), 1.0, 0.0);
                return;
            }
        }
        
        // PRIORITY 2: Enter safe zone if possible
        Marble safeZoneEntryMarble = findMarbleAtSafeZoneEntry(board);
        if (safeZoneEntryMarble != null && steps == 1) {
            int currPos = board.getMarblePosition(safeZoneEntryMarble);
            int targetPos = board.calculateTargetPosition(this, currPos, steps);
            
            System.out.println(name + " entering safe zone from " + currPos + " to " + targetPos);
            board.moveMarbleToPosition(safeZoneEntryMarble, targetPos, 1.0, 0.0);
            return;
        }
        
        // PRIORITY 3: Try to capture an opponent's marble
        Marble captureMarble = findMarbleForCapture(board, steps);
        if (captureMarble != null) {
            int currPos = board.getMarblePosition(captureMarble);
            int targetPos = board.calculateTargetPosition(this, currPos, steps);
            
            // Don't make captures that would skip our safe zone
            if (!wouldSkipSafeZone(currPos, steps)) {
                System.out.println(name + " CAPTURING marble at position " + targetPos);
                board.moveMarbleToPosition(captureMarble, targetPos, 1.0, 0.0);
                return;
            } else {
                System.out.println(name + " SKIPPING capture that would miss safe zone entry");
            }
        }
        
        // PRIORITY 4: Move marble in safe zone further along
        Marble safeZoneMarble = findMarbleInSafeZone(board);
        if (safeZoneMarble != null) {
            int currPos = board.getMarblePosition(safeZoneMarble);
            int targetPos = board.calculateTargetPosition(this, currPos, steps);
            
            if (targetPos != currPos) {
                System.out.println(name + " advancing marble in safe zone from " + currPos + " to " + targetPos);
                board.moveMarbleToPosition(safeZoneMarble, targetPos, 1.0, 0.0);
                return;
            }
        }
        
        // PRIORITY 5: Make strategic movement with marble closest to safe zone
        Marble bestMarble = findMarbleClosestToSafeZone(board);
        if (bestMarble != null) {
            int currPos = board.getMarblePosition(bestMarble);
            
            // Skip movement if it would cause us to miss our safe zone entry
            if (!wouldSkipSafeZone(currPos, steps)) {
                int targetPos = board.calculateTargetPosition(this, currPos, steps);
                
                if (targetPos != currPos) {
                    System.out.println(name + " moving marble closest to safe zone from " + currPos + " to " + targetPos);
                    board.moveMarbleToPosition(bestMarble, targetPos, 1.0, 0.0);
                    return;
                }
            } else {
                System.out.println(name + " AVOIDING move that would skip safe zone entry");
                
                // If we'd skip the safe zone, try with a different marble instead
                for (Marble m : marbles) {
                    if (!board.isMarbleInHome(m) && m != bestMarble) {
                        int altCurrPos = board.getMarblePosition(m);
                        if (!wouldSkipSafeZone(altCurrPos, steps)) {
                            int targetPos = board.calculateTargetPosition(this, altCurrPos, steps);
                            
                            if (targetPos != altCurrPos) {
                                System.out.println(name + " using alternative marble from " + altCurrPos + " to " + targetPos);
                                board.moveMarbleToPosition(m, targetPos, 1.0, 0.0);
                                return;
                            }
                        }
                    }
                }
            }
        }
        
        // PRIORITY 6: Last resort - move any marble that can move
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int currPos = board.getMarblePosition(m);
                
                // Still try to avoid skipping safe zone
                if (!wouldSkipSafeZone(currPos, steps)) {
                    int targetPos = board.calculateTargetPosition(this, currPos, steps);
                    
                    if (targetPos != currPos) {
                        System.out.println(name + " making last resort move from " + currPos + " to " + targetPos);
                        board.moveMarbleToPosition(m, targetPos, 1.0, 0.0);
                        return;
                    }
                }
            }
        }
        
        // PRIORITY 7: Absolute last resort - move even if skipping safe zone
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int currPos = board.getMarblePosition(m);
                int targetPos = board.calculateTargetPosition(this, currPos, steps);
                
                if (targetPos != currPos) {
                    System.out.println(name + " FORCED to make move that skips safe zone from " + currPos + " to " + targetPos);
                    board.moveMarbleToPosition(m, targetPos, 1.0, 0.0);
                    return;
                }
            }
        }
        
        // If we couldn't make any move, advance the turn
        System.out.println(name + " has NO VALID MOVES, discarding card");
        board.nextTurn();
    }
    
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
     * Find a marble that can capture an opponent's marble
     */
    private Marble findMarbleForCapture(Board board, int steps) {
        List<Marble> candidateMarbles = new ArrayList<>();
        
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int currPos = board.getMarblePosition(m);
                int targetPos = board.calculateTargetPosition(this, currPos, steps);
                
                // Skip if no movement possible
                if (targetPos == currPos) continue;
                
                // Check if target position has an opponent's marble
                if (isOpponentMarbleAtPosition(board, targetPos)) {
                    candidateMarbles.add(m);
                }
            }
        }
        
        // If we found any marbles that can capture, return the one closest to safe zone
        if (!candidateMarbles.isEmpty()) {
            return getBestMarbleFromList(candidateMarbles, board);
        }
        
        return null;
    }
    
    /**
     * From a list of candidate marbles, find the one closest to safe zone
     */
    private Marble getBestMarbleFromList(List<Marble> candidates, Board board) {
        Marble best = null;
        int bestDist = Integer.MAX_VALUE;
        int safeStart = getSafeZoneStart();
        
        for (Marble m : candidates) {
            int pos = board.getMarblePosition(m);
            int dist = calculateDistanceToSafeZone(pos, safeStart);
            
            if (dist < bestDist) {
                bestDist = dist;
                best = m;
            }
        }
        
        return best;
    }
    
    /**
     * Calculate the distance from a position to the safe zone
     */
    private int calculateDistanceToSafeZone(int position, int safeZoneStart) {
        // Simplified distance calculation
        // For more complex boards, this would need to account for the track layout
        return Math.abs(position - safeZoneStart);
    }

    // --- Helper methods ---

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

    private Marble findMarbleClosestToSafeZone(Board board) {
        Marble best = null;
        int bestDist = Integer.MAX_VALUE;
        int safeZoneStart = getSafeZoneStart();
        
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int pos = board.getMarblePosition(m);
                int dist = calculateDistanceToSafeZone(pos, safeZoneStart);
                
                if (dist < bestDist) {
                    bestDist = dist;
                    best = m;
                }
            }
        }
        
        return best;
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
}