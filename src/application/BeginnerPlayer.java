package application;

import java.util.ArrayList;
import java.util.List;



/**
 * BeginnerPlayer - With improved marble detection and detailed debugging
 * Using standard java.util classes for compatibility
 */
public class BeginnerPlayer extends Player {

    public BeginnerPlayer(String name) {
        super(name);
    }

    @Override
    public void takeTurn(Board board) {
        System.out.println("\n" + name + ".takeTurn() with hand: " + cards);
        
        // Simple strategy: play the first card in hand
        if (!cards.isEmpty()) {
            Card cardToPlay = cards.get(0);
            System.out.println(name + " is playing card: " + cardToPlay);
            board.playCard(this, cardToPlay);
        }
    }

    @Override
    public void makeMove(Board board, Card card) {
        System.out.println("\n" + name + ".makeMove() with card: " + card);
        boolean moved = false;
        int steps = getStepsForCard(card);
        int basePos = getBasePosition();
        
        System.out.println(name + " has " + steps + " steps to move");
        
        // Get a complete inventory of all marbles and their positions
        System.out.println(name + " marble inventory:");
        int marblesOnBoard = 0;
        int marblesInHome = 0;
        int marblesOnBase = 0;
        
        for (Marble m : marbles) {
            if (board.isMarbleInHome(m)) {
                marblesInHome++;
                System.out.println("- Marble " + marbles.indexOf(m) + " is in HOME");
            } else {
                int pos = board.getMarblePosition(m);
                marblesOnBoard++;
                
                if (pos == basePos) {
                    marblesOnBase++;
                    System.out.println("- Marble " + marbles.indexOf(m) + " is on BASE (position " + pos + ")");
                } else {
                    System.out.println("- Marble " + marbles.indexOf(m) + " is on BOARD at position " + pos);
                }
            }
        }
        
        System.out.println(name + " has " + marblesOnBoard + " marble(s) on board, " + 
                          marblesOnBase + " on base, and " + marblesInHome + " in home");
        
        // NEW CHECK: If the card moves backward and we have marbles in safe zone, don't try to move those
        if (steps < 0) {
            System.out.println("Card has negative movement (" + steps + "), checking for marbles in safe zone");
            List<Marble> movableMarblesNotInSafeZone = new ArrayList<>();
            
            for (Marble m : marbles) {
                if (!board.isMarbleInHome(m)) {
                    int pos = board.getMarblePosition(m);
                    if (!board.isInSafeZone(this, pos)) {
                        movableMarblesNotInSafeZone.add(m);
                        System.out.println("- Marble at position " + pos + " is not in safe zone, can be moved backward");
                    } else {
                        System.out.println("- Marble at position " + pos + " is in safe zone, cannot be moved backward");
                    }
                }
            }
            
            // If we have marbles that can move backward, use the first one
            if (!movableMarblesNotInSafeZone.isEmpty()) {
                Marble marbleToMove = movableMarblesNotInSafeZone.get(0);
                int currPos = board.getMarblePosition(marbleToMove);
                int targetPos = board.calculateTargetPosition(this, currPos, steps);
                
                if (targetPos != currPos) {
                    System.out.println(name + " MOVING marble backward from position " + currPos + " to " + targetPos);
                    board.moveMarbleToPosition(marbleToMove, targetPos, 1.0, 0.0);
                    moved = true;
                    return; // Successfully moved a marble
                }
            }
        }
        
        // PRIORITY 1: If we have a marble on the base position, always move it first
        Marble baseMarble = findMarbleOnBase(board);
        if (baseMarble != null) {
            int currPos = board.getMarblePosition(baseMarble);
            System.out.println("Found marble on base at position " + currPos);
            
            // Double-check currPos matches basePos
            if (currPos != basePos) {
                System.out.println("WARNING: Base position mismatch! currPos=" + currPos + ", basePos=" + basePos);
            }
            
            int targetPos = board.calculateTargetPosition(this, currPos, steps);
            System.out.println("Target position for base marble: " + targetPos);
            
            // Only move if it changes position
            if (targetPos != currPos) {
                System.out.println(name + " MOVING marble from base position " + currPos + " to " + targetPos);
                board.moveMarbleToPosition(baseMarble, targetPos, 1.0, 0.0);
                moved = true;
                return; // Successfully moved a base marble
            } else {
                System.out.println("Cannot move base marble - target position equals current position");
            }
        } else if (marblesOnBase > 0) {
            System.out.println("WARNING: Detected " + marblesOnBase + " marbles on base but findMarbleOnBase returned null!");
        }
        
        // PRIORITY 2: Try ALL marbles on board that can move (not just the furthest)
        System.out.println("Checking ALL marbles on board for possible moves:");
        ArrayList<Marble> movableMarblesInfo = new ArrayList<Marble>();
        
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m) && (baseMarble == null || m != baseMarble)) {
                int currPos = board.getMarblePosition(m);
                
                // Skip marbles in safe zone if trying to move backward
                if (steps < 0 && board.isInSafeZone(this, currPos)) {
                    System.out.println("- Marble at position " + currPos + " is in safe zone, cannot move backward");
                    continue;
                }
                
                int targetPos = board.calculateTargetPosition(this, currPos, steps);
                
                String moveInfo = "Marble at position " + currPos + " -> target " + targetPos;
                if (targetPos != currPos) {
                    moveInfo += " (CAN MOVE)";
                    movableMarblesInfo.add(m);
                } else {
                    moveInfo += " (cannot move)";
                }
                System.out.println("- " + moveInfo);
            }
        }
        
        // If we have any marbles that can move, move the first one
        if (!movableMarblesInfo.isEmpty()) {
            Marble marbleToMove = movableMarblesInfo.get(0);
            int currPos = board.getMarblePosition(marbleToMove);
            int targetPos = board.calculateTargetPosition(this, currPos, steps);
            
            System.out.println(name + " MOVING marble from position " + currPos + " to " + targetPos);
            board.moveMarbleToPosition(marbleToMove, targetPos, 1.0, 0.0);
            moved = true;
            return; // Successfully moved a marble
        } else if (marblesOnBoard > 0) {
            System.out.println("Found " + marblesOnBoard + " marbles on board but NONE can move with " + steps + " steps");
        }
        
        // PRIORITY 3: If we have ACE/KING and marbles in home, bring one out
        if (!moved && hasMarbleInHome(board) && 
           (card.getValue() == Card.Value.ACE || card.getValue() == Card.Value.KING)) {
            
            // Check if base position is free
            boolean baseOccupied = (findMarbleOnBase(board) != null);
            System.out.println("Base position " + basePos + " occupied? " + baseOccupied);
            
            if (!baseOccupied) {
                Marble homeMarble = getFirstMarbleInHome(board);
                if (homeMarble != null) {
                    System.out.println(name + " BRINGING marble out from home to base position " + basePos);
                    board.moveMarbleToPosition(homeMarble, basePos, 1.0, 0.0);
                    moved = true;
                    return; // Successfully brought out a marble
                } else {
                    System.out.println("ERROR: hasMarbleInHome returned true but getFirstMarbleInHome returned null!");
                }
            } else {
                System.out.println("Cannot bring marble from home because base is occupied");
            }
        }
        
        // If we couldn't make any move, advance the turn
        if (!moved) {
            System.out.println(name + " has NO VALID MOVES, discarding card");
            board.nextTurn();
        }
    }
    
    /**
     * Find a marble that is currently on this player's base position
     */
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
}