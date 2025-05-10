package application;

import java.util.Comparator;
import java.util.List;

/**
 * NormalPlayer - same move logic as BeginnerPlayer, but chooses its card
 * by rules:
 *   1) if no on-board marbles and an ACE/KING in hand → play one of those
 *   2) else if base slot empty and an ACE/KING in hand → play one of those
 *   3) otherwise play the highest-move-value card (QUEEN=12, JACK=11, TEN=10, etc.)
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
        List<Marble> onBoard = marbles.stream()
            .filter(m -> !board.isMarbleInHome(m))
            .toList();
        boolean hasOnBoard    = !onBoard.isEmpty();
        boolean baseOccupied  = onBoard.stream()
            .anyMatch(m -> board.getMarblePosition(m) == basePos);

        // find any ACE or KING in hand
        Card aceOrKing = cards.stream()
            .filter(c -> c.getValue() == Card.Value.ACE 
                      || c.getValue() == Card.Value.KING)
            .findFirst()
            .orElse(null);

        Card toPlay;

        // rule 1
        if (!hasOnBoard && aceOrKing != null) {
            toPlay = aceOrKing;
        }
        // rule 2
        else if (!baseOccupied && aceOrKing != null) {
            toPlay = aceOrKing;
        }
        // rule 3: highest move-value
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
        int basePos = getBasePosition();

        // 1) Figure out how many steps this card is worth
        int steps;
        switch(card.getValue()) {
            case ACE:   steps = 1;   break;
            case KING:  steps = 13;  break;
            case JACK:  steps = 11;  break;
            default:    steps = getStepsForCard(card);
        }

        // 2) ACE or KING: try to bring a new marble out if your base is still free
        if ((card.getValue() == Card.Value.ACE || card.getValue() == Card.Value.KING)
             && hasMarbleInHome(board)) {
            boolean baseOccupied = marbles.stream()
                .filter(m -> !board.isMarbleInHome(m))
                .anyMatch(m -> board.getMarblePosition(m) == basePos);
            if (!baseOccupied) {
                // animate new marble onto the track
                Marble newcomer = getFirstMarbleInHome(board);
                board.moveMarbleToPosition(newcomer, basePos, 1.0, 0.0);
                moved = true;
            }
        }

        // 3) GENERIC MOVE (this must run for *every* card type if we still haven't moved):
        if (!moved) {
            // pick the marble that actually advances (and in NormalPlayer the one
            // that lands furthest, but for BeginnerPlayer the first one is fine)
            Marble toMove = marbles.stream()
                .filter(m -> !board.isMarbleInHome(m))
                .filter(m -> {
                    int curr = board.getMarblePosition(m);
                    int dest = board.calculateTargetPosition(this, curr, steps);
                    return dest != curr;
                })
                .findFirst()
                .orElse(null);

            if (toMove != null) {
                int curr = board.getMarblePosition(toMove);
                int dest = board.calculateTargetPosition(this, curr, steps);
                board.moveMarbleToPosition(toMove, dest, 1.0, 0.0);
                moved = true;
            }
        }

        // 4) Fallback: if we still never queued anything, advance the turn now
        if (!moved) {
            board.nextTurn();
        }
    }

    /** @return true if at least one marble is still in home */
    private boolean hasMarbleInHome(Board board) {
        for (Marble m : marbles)
            if (board.isMarbleInHome(m)) return true;
        return false;
    }

    /** @return the first marble still in home, or null if none */
    private Marble getFirstMarbleInHome(Board board) {
        for (Marble m : marbles)
            if (board.isMarbleInHome(m)) return m;
        return null;
    }

    /** Map 2–10, QUEEN=12, FOUR=-4; ACE/JACK/KING handled above */
    private int getStepsForCard(Card card) {
        switch (card.getValue()) {
            case TWO:   return 2;
            case THREE: return 3;
            case FOUR:  return -4;
            case FIVE:  return 5;
            case SIX:   return 6;
            case SEVEN: return 7;
            case EIGHT: return 8;
            case NINE:  return 9;
            case TEN:   return 10;
            case QUEEN: return 12;
            default:    return 0;
        }
    }

    /** Returns the “move value” used for choosing highest card */
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

    /** @return this player’s base/entry position */
    private int getBasePosition() {
        switch (name.toLowerCase()) {
            case "player1": return 51;
            case "player2": return 1;
            case "player3": return 18;
            case "player4": return 35;
            default:        return 1;
        }
    }
}
