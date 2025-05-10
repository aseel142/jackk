package application;

import java.util.Random;

public class IntermediatePlayer extends Player {
    private Random random = new Random();

    public IntermediatePlayer(String name) {
        super(name);
    }

    @Override
    public void takeTurn(Board board) {
        // 1) If no cards left, skip immediately
        if (cards.isEmpty()) {
            board.nextTurn();
            return;
        }

        // 2) Otherwise play as before…
        //    (for BeginnerPlayer this is simply playing the first card)
        Card cardToPlay = cards.get(0);
        board.playCard(this, cardToPlay);
    }

    @Override
    public void makeMove(Board board, Card card) {
        boolean moved = false;
        int steps = getStepsForCard(card); // JACK→11

        // 1) ACE/KING: bring one out if you still have home marbles
        if (hasMarbleInHome(board) &&
           (card.getValue()==Card.Value.ACE || card.getValue()==Card.Value.KING)) {
            Marble m = getFirstMarbleInHome(board);
            board.moveMarbleToPosition(m, getBasePosition(), 1.0, 0.0);
            moved = true;
        }

        // 2) All other cards—including JACK—move your chosen marble
        if (!moved) {
            Marble best = findMarbleClosestToSafeZone(board);
            if (best != null) {
                int curr = board.getMarblePosition(best);
                int dest = board.calculateTargetPosition(this, curr, steps);
                board.moveMarbleToPosition(best, dest, 1.0, 0.0);
                moved = true;
            }
        }


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

    private Marble findMostAdvancedMarble(Board board) {
        Marble best = null;
        int furthest = -1;
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int pos = board.getMarblePosition(m);
                if (pos > furthest) {
                    furthest = pos;
                    best = m;
                }
            }
        }
        return best;
    }

    private Marble findBestMarbleToSwapWith(Board board) {
        Marble bestTarget = null;
        int maxPos = -1;
        for (int i = 0; i < 4; i++) {
            Player opponent = getOpponentByIndex(i, board);
            for (Marble m : opponent.getMarbles()) {
                if (!board.isMarbleInHome(m)) {
                    int pos = board.getMarblePosition(m);
                    if (pos > maxPos) {
                        maxPos = pos;
                        bestTarget = m;
                    }
                }
            }
        }
        return bestTarget;
    }

    private Player getOpponentByIndex(int idx, Board board) {
        // 0=player1,1=player2,2=player3,3=player4
        return board.getPlayerByIndex(idx);
    }

    private boolean wouldMoveToSafeZone(Board board, Card card) {
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int pos = board.getMarblePosition(m);
                int next = board.calculateTargetPosition(this, pos, getStepsForCard(card));
                if (board.isInSafeZone(this, next)) return true;
            }
        }
        return false;
    }

    private Marble findMarbleClosestToSafeZone(Board board) {
        Marble best = null;
        int bestDist = Integer.MAX_VALUE;
        for (Marble m : marbles) {
            if (!board.isMarbleInHome(m)) {
                int pos = board.getMarblePosition(m);
                int safeStart = getSafeZoneStart();
                int dist = Math.abs(safeStart - pos);
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