package application;

/**
 * BeginnerPlayer - Enhanced with extensive debugging
 */
public class BeginnerPlayer extends Player {

    public BeginnerPlayer(String name) {
        super(name);
    }

    @Override
    public void takeTurn(Board board) {
        System.out.println("\n" + name + ".takeTurn() - hand size: " + cards.size());
        
        // Simple strategy: play the first card in hand
        if (!cards.isEmpty()) {
            Card cardToPlay = cards.get(0);
            System.out.println(name + " is playing card: " + cardToPlay);
            board.playCard(this, cardToPlay);
        } else {
            System.out.println(name + " has no cards to play!");
        }
    }

    @Override
    public void makeMove(Board board, Card card) {
        System.out.println("\n" + name + ".makeMove() with card: " + card);
        boolean moved = false;
        int steps = getStepsForCard(card);
        System.out.println(name + " - Card translates to " + steps + " steps");
        
        // STEP 1: Look for marbles on base position (highest priority)
        Marble baseMarble = findMarbleOnBase(board);
        int basePos = getBasePosition();
        System.out.println(name + " - Base position is: " + basePos);
        
        if (baseMarble != null) {
            System.out.println(name + " has a marble on base position");
            int currPos = board.getMarblePosition(baseMarble);
            System.out.println(name + " - Current position of base marble: " + currPos);
            
            int targetPos = board.calculateTargetPosition(this, currPos, steps);
            System.out.println(name + " - Target position calculated: " + targetPos);
            
            // Is the target position different from current position?
            System.out.println(name + " - Will marble move? " + (targetPos != currPos ? "YES" : "NO"));
            
            if (targetPos != currPos) {
                System.out.println(name + " - MOVING marble from base position " + currPos + " to " + targetPos);
                board.moveMarbleToPosition(baseMarble, targetPos, 1.0, 0.0);
                moved = true;
                return; // Successfully moved a base marble, we're done
            } else {
                System.out.println(name + " - NOT MOVING marble because target position equals current position");
            }
        } else {
            System.out.println(name + " - No marble found on base position " + basePos);
        }
        
        // STEP 2: Check for any other marble on board we can move
        System.out.println(name + " - Checking for other marbles on board to move");
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m) && (baseMarble == null || m != baseMarble)) {
                int currPos = board.getMarblePosition(m);
                System.out.println(name + " - Found marble at position: " + currPos);
                
                int targetPos = board.calculateTargetPosition(this, currPos, steps);
                System.out.println(name + " - Target position calculated: " + targetPos);
                
                // As long as it changes position, move it
                if (targetPos != currPos) {
                    System.out.println(name + " - MOVING this marble from position " + currPos + " to " + targetPos);
                    board.moveMarbleToPosition(m, targetPos, 1.0, 0.0);
                    moved = true;
                    return; // Successfully moved a board marble, we're done
                } else {
                    System.out.println(name + " - NOT MOVING this marble (target equals current)");
                }
            }
        }
        
        // STEP 3: Try to bring out a new marble with ACE/KING
        if (!moved && hasMarbleInHome(board) && 
           (card.getValue() == Card.Value.ACE || card.getValue() == Card.Value.KING)) {
            
            System.out.println(name + " - Trying to bring a marble out from home with ACE/KING");
            
            // Check if base position is free
            boolean baseOccupied = (findMarbleOnBase(board) != null);
            System.out.println(name + " - Is base position occupied? " + baseOccupied);
            
            if (!baseOccupied) {
                Marble homeMarble = getFirstMarbleInHome(board);
                if (homeMarble != null) {
                    System.out.println(name + " - BRINGING marble out from home to base position " + basePos);
                    board.moveMarbleToPosition(homeMarble, basePos, 1.0, 0.0);
                    moved = true;
                    return; // Successfully brought out a marble, we're done
                } else {
                    System.out.println(name + " - No marbles found in home (???)");
                }
            } else {
                System.out.println(name + " - Cannot bring marble out because base is occupied");
            }
        }
        
        // STEP 4: If we couldn't make any move, advance the turn
        if (!moved) {
            System.out.println(name + " - NO VALID MOVES, discarding card");
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
     * JACK is now 11 steps instead of swapping
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