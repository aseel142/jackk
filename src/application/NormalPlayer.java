package application;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

/**
 * NormalPlayer - Keeps original card selection strategy but uses improved movement priorities
 */
public class NormalPlayer extends Player {

    public NormalPlayer(String name) {
        super(name);
    }

    @Override
    public void takeTurn(Board board) {
        System.out.println(name + ".takeTurn(); hand=" + cards);

        // 1) skip if empty hand
        if (cards.isEmpty()) {
            board.nextTurn();
            return;
        }

        int basePos = getBasePosition();

        // compute on-board and base occupancy
        List<Marble> onBoard = new ArrayList<>();
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                onBoard.add(m);
            }
        }
        
        boolean hasOnBoard = !onBoard.isEmpty();
        
        // Check if we have a marble on base
        boolean baseOccupied = false;
        for (Marble m : onBoard) {
            if (board.getMarblePosition(m) == basePos) {
                baseOccupied = true;
                break;
            }
        }

        // find any ACE or KING in hand
        Card aceOrKing = null;
        for (Card c : cards) {
            if (c.getValue() == Card.Value.ACE || c.getValue() == Card.Value.KING) {
                aceOrKing = c;
                break;
            }
        }

        Card toPlay;

        // rule 1: if no on-board marbles and an ACE/KING in hand → play one of those
        if (!hasOnBoard && aceOrKing != null) {
            toPlay = aceOrKing;
        }
        // rule 2: if base slot empty and an ACE/KING in hand → play one of those
        else if (!baseOccupied && aceOrKing != null && hasMarbleInHome(board)) {
            toPlay = aceOrKing;
        }
        // rule 3: otherwise play the highest-move-value card
        else {
            toPlay = cards.stream()
                .max(Comparator.comparingInt(this::cardMoveValue))
                .get();  // safe because hand not empty
        }

        board.playCard(this, toPlay);
    }

    @Override
    public void makeMove(Board board, Card card) {
        boolean moved = false;
        int steps = getStepsForCard(card);
        int basePos = getBasePosition();
        
        // Special handling for backward movement (FOUR card)
        if (steps < 0) {
            // First, identify marbles that are NOT in the safe zone
            List<Marble> movableMarblesNotInSafeZone = new ArrayList<>();
            
            for (Marble m : marbles) {
                if (!board.isMarbleInHome(m)) {
                    int pos = board.getMarblePosition(m);
                    if (!board.isInSafeZone(this, pos)) {
                        movableMarblesNotInSafeZone.add(m);
                    }
                }
            }
            
            // If we have marbles that can be moved backward, choose the furthest one
            if (!movableMarblesNotInSafeZone.isEmpty()) {
                Marble marbleToMove = findFurthestMarbleInList(board, movableMarblesNotInSafeZone);
                int currPos = board.getMarblePosition(marbleToMove);
                int targetPos = board.calculateTargetPosition(this, currPos, steps);
                
                if (targetPos != currPos) {
                    System.out.println(name + " moving marble backward from position " + currPos + " to " + targetPos);
                    board.moveMarbleToPosition(marbleToMove, targetPos, 1.0, 0.0);
                    return;
                }
            }
        }
        
        // PRIORITY 1: If we have a marble on the base position, always move it first
        Marble baseMarble = findMarbleOnBase(board);
        if (baseMarble != null) {
            int currPos = board.getMarblePosition(baseMarble);
            int targetPos = board.calculateTargetPosition(this, currPos, steps);
            
            // Only move if it changes position
            if (targetPos != currPos) {
                System.out.println(name + " moving marble from base position");
                board.moveMarbleToPosition(baseMarble, targetPos, 1.0, 0.0);
                moved = true;
                return; // Successfully moved a base marble
            }
        }
        
        // PRIORITY 2: Try to move any marble that's furthest along on the track
        Marble furthestMarble = findFurthestMarble(board);
        if (furthestMarble != null && (baseMarble == null || furthestMarble != baseMarble)) {
            int currPos = board.getMarblePosition(furthestMarble);
            
            // Skip if trying to move backward from safe zone
            if (steps < 0 && board.isInSafeZone(this, currPos)) {
                System.out.println(name + " cannot move marble backward from safe zone");
            } else {
                int targetPos = board.calculateTargetPosition(this, currPos, steps);
                
                // Only move if it changes position
                if (targetPos != currPos) {
                    System.out.println(name + " moving furthest marble from position " + currPos);
                    board.moveMarbleToPosition(furthestMarble, targetPos, 1.0, 0.0);
                    moved = true;
                    return; // Successfully moved the furthest marble
                }
            }
        }
        
        // PRIORITY 3: Try to move ANY marble on the board that can move
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m) && 
                (baseMarble == null || m != baseMarble) && 
                (furthestMarble == null || m != furthestMarble)) {
                
                int currPos = board.getMarblePosition(m);
                
                // Skip if trying to move backward from safe zone
                if (steps < 0 && board.isInSafeZone(this, currPos)) {
                    continue;
                }
                
                int targetPos = board.calculateTargetPosition(this, currPos, steps);
                
                // Only move if it changes position
                if (targetPos != currPos) {
                    System.out.println(name + " moving marble from position " + currPos);
                    board.moveMarbleToPosition(m, targetPos, 1.0, 0.0);
                    moved = true;
                    return; // Successfully moved a marble
                }
            }
        }
        
        // PRIORITY 4: If we have ACE/KING and marbles in home, bring one out
        if (!moved && hasMarbleInHome(board) && 
           (card.getValue() == Card.Value.ACE || card.getValue() == Card.Value.KING)) {
            
            // Check if base position is free
            boolean baseOccupied = (findMarbleOnBase(board) != null);
            
            if (!baseOccupied) {
                Marble homeMarble = getFirstMarbleInHome(board);
                if (homeMarble != null) {
                    System.out.println(name + " bringing marble out from home to base");
                    board.moveMarbleToPosition(homeMarble, basePos, 1.0, 0.0);
                    moved = true;
                    return; // Successfully brought out a marble
                }
            }
        }
        
        // If we couldn't make any move, advance the turn
        if (!moved) {
            System.out.println(name + " has no valid moves, discarding card");
            board.nextTurn();
        }
    }

    /**
     * Find the furthest marble from a list of marbles
     */
    private Marble findFurthestMarbleInList(Board board, List<Marble> marbleList) {
        Marble furthest = null;
        int maxProgress = -1;
        
        for (Marble m : marbleList) {
            int currPos = board.getMarblePosition(m);
            
            // For simplicity, just use position as progress measure
            int progress = currPos;
            
            if (progress > maxProgress) {
                maxProgress = progress;
                furthest = m;
            }
        }
        
        return furthest;
    }
    
    /**
     * Check if player has marbles in home
     */
    private boolean hasMarbleInHome(Board board) {
        for (Marble m : marbles) {
            if (board.isMarbleInHome(m)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Find the first marble in home
     */
    private Marble getFirstMarbleInHome(Board board) {
        for (Marble m : marbles) {
            if (board.isMarbleInHome(m)) {
                return m;
            }
        }
        return null;
    }
    
    /**
     * Get player's base position
     */
    private int getBasePosition() {
        switch (name.toLowerCase()) {
            case "player1": return 51;
            case "player2": return 1;
            case "player3": return 18;
            case "player4": return 35;
            default: return 1;
        }
    }
    
    /**
     * Get steps for card value
     */
    private int getStepsForCard(Card card) {
        switch (card.getValue()) {
            case ACE:   return 1;
            case TWO:   return 2;
            case THREE: return 3;
            case FOUR:  return -4; // backward
            case FIVE:  return 5;
            case SIX:   return 6;
            case SEVEN: return 7;
            case EIGHT: return 8;
            case NINE:  return 9;
            case TEN:   return 10;
            case JACK:  return 11; // Changed: JACK now moves 11 steps forward
            case QUEEN: return 12;
            case KING:  return 13;
            default:    return 0;
        }
    }
    
    /** Returns the "move value" used for choosing highest card */
    private int cardMoveValue(Card card) {
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
    return null; // No marble found on base
}

/**
 * Find the marble that has moved furthest along the track
 */
private Marble findFurthestMarble(Board board) {
    Marble furthest = null;
    int maxProgress = -1;
    
    for (Marble m : marbles) {
        if (!board.isMarbleInHome(m)) {
            int currPos = board.getMarblePosition(m);
            
            // For simplicity, just use position as progress measure
            int progress = currPos;
            
            if (progress > maxProgress) {
                maxProgress = progress;
                furthest = m;
            }
        }
    }
    
    return furthest;
}}

