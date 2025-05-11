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
        
        // Otherwise, find a card that can capture an opponent's marble
        for (Card card : cards) {
            if (canCaptureWithCard(board, card)) {
                return card;
            }
        }
        
        // If no capture is possible, find the highest value card
        return cards.stream()
            .max(Comparator.comparingInt(this::getStepsForCard))
            .orElse(cards.get(0)); // Fallback to first card if something goes wrong
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
        
        // PRIORITY 2: Try to capture an opponent's marble
        Marble captureMarble = findMarbleForCapture(board, steps);
        if (captureMarble != null) {
            int currPos = board.getMarblePosition(captureMarble);
            int targetPos = board.calculateTargetPosition(this, currPos, steps);
            
            System.out.println(name + " CAPTURING marble at position " + targetPos);
            board.moveMarbleToPosition(captureMarble, targetPos, 1.0, 0.0);
            return;
        }
        
        // PRIORITY 3: Move the marble closest to safe zone
        Marble bestMarble = findMarbleClosestToSafeZone(board);
        if (bestMarble != null) {
            int currPos = board.getMarblePosition(bestMarble);
            int targetPos = board.calculateTargetPosition(this, currPos, steps);
            
            if (targetPos != currPos) {
                System.out.println(name + " moving marble closest to safe zone from " + currPos + " to " + targetPos);
                board.moveMarbleToPosition(bestMarble, targetPos, 1.0, 0.0);
                return;
            }
        }
        
        // PRIORITY 4: Move any marble that can move
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int currPos = board.getMarblePosition(m);
                int targetPos = board.calculateTargetPosition(this, currPos, steps);
                
                if (targetPos != currPos) {
                    System.out.println(name + " moving marble from " + currPos + " to " + targetPos);
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