package application;

import java.util.Random;

/**
 * BeginnerPlayer - Makes very basic moves without complex strategy
 */
public class BeginnerPlayer extends Player {
    private Random random = new Random();

    public BeginnerPlayer(String name) {
        super(name);
    }

    @Override
    public void takeTurn(Board board) {
        // Simple strategy: play the first card in hand
        if (!cards.isEmpty()) {
            Card cardToPlay = cards.get(0);
            board.playCard(this, cardToPlay);
        }
    }

    @Override
    public void makeMove(Board board, Card card) {
        boolean moved = false;

        // 1. If it's a King or Ace and we have marbles in home, move one out
        if (hasMarbleInHome(board) &&
            (card.getValue() == Card.Value.KING || card.getValue() == Card.Value.ACE)) {
            Marble marbleToMove = getFirstMarbleInHome(board);
            if (marbleToMove != null) {
                int basePosition = getBasePosition();
                board.moveMarbleToPosition(marbleToMove, basePosition, 1.0, 0.0);
                moved = true;
            }
        }
        // 2. If it's a Jack, ignore its special behavior (no marble animation)
        else if (card.getValue() == Card.Value.JACK) {
            // moved stays false
        }
        // 3. Otherwise move the first marble not in home
        else {
            Marble marbleToMove = getFirstMarbleNotInHome(board);
            if (marbleToMove != null) {
                int currentPosition = board.getMarblePosition(marbleToMove);
                int steps = getStepsForCard(card);
                int targetPosition = board.calculateTargetPosition(this, currentPosition, steps);
                board.moveMarbleToPosition(marbleToMove, targetPosition, 1.0, 0.0);
                moved = true;
            }
        }

        // Fallback: if we never moved a marble, advance the turn immediately
        if (!moved) {
            board.nextTurn();
        }
    }

    // Helper to check if any marble is still in home
    private boolean hasMarbleInHome(Board board) {
        for (Marble marble : marbles) {
            if (board.isMarbleInHome(marble)) {
                return true;
            }
        }
        return false;
    }

    // First marble found in home
    private Marble getFirstMarbleInHome(Board board) {
        for (Marble marble : marbles) {
            if (board.isMarbleInHome(marble)) {
                return marble;
            }
        }
        return null;
    }

    // First marble found not in home
    private Marble getFirstMarbleNotInHome(Board board) {
        for (Marble marble : marbles) {
            if (!board.isMarbleInHome(marble)) {
                return marble;
            }
        }
        return null;
    }

    // Get base position based on player name
    private int getBasePosition() {
        switch (name.toLowerCase()) {
            case "player1": return 51;
            case "player2": return 1;
            case "player3": return 18;
            case "player4": return 35;
            default: return 1;
        }
    }

    // Map card values to move steps
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
            case JACK:  return 0;
            case QUEEN: return 12;
            case KING:  return 13;
            default:    return 0;
        }
    }
}
