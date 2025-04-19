package application;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class Board {
	 // → -1 means “HOME”; 0..TRACK_END are on‐track; >TRACK_END means in BASE/SAFE
    private static final int HOME_POS = -1;
    private static final int TRACK_END = 40;  // adjust to your board’s track length

    private final List<Player> players;
    private final Map<Marble, Integer> marblePositions = new HashMap<>();

    /**
     * Initialize the board with each player's marbles in HOME.
     */
    public Board(List<Player> players) {
        this.players = players;
        for (Player p : players) {
            for (Marble m : p.getMarbles()) {
                marblePositions.put(m, HOME_POS);
                m.setZone(ZoneType.HOME);
                m.setPosition(HOME_POS);
            }
        }
    }

    /**
     * Is there any marble already sitting on this track index?
     */
    public boolean isBlocked(int position) {
        return marblePositions.containsValue(position);
    }

    /**
     * Can `marble` move `steps` spaces without hopping over
     * another marble?
     */
    public boolean validateMove(Marble marble, int steps) {
        int start = marblePositions.get(marble);
        int dir   = Integer.signum(steps);
        for (int i = 1; i <= Math.abs(steps); i++) {
            int pos = start + dir * i;
            if (isBlocked(pos)) return false;
        }
        return true;
    }

    /**
     * Perform the move (assumes validateMove(...) was called).
     * Updates both this board’s map and the Marble object itself.
     */
    public void updatePosition(Marble marble, int steps) {
        if (!validateMove(marble, steps)) return;

        int oldPos = marblePositions.get(marble);
        int newPos = oldPos == HOME_POS
                   ? 0                   // exiting HOME onto track
                   : oldPos + steps;     // stepping along track

        // Zone transitions
        if (oldPos == HOME_POS && steps > 0) {
            marble.setZone(ZoneType.TRACK);
        } else if (newPos > TRACK_END) {
            marble.setZone(ZoneType.BASE);
        } else {
            marble.setZone(ZoneType.TRACK);
        }

        // Commit the move
        marblePositions.put(marble, newPos);
        marble.setPosition(newPos);
    }
}
	
}
