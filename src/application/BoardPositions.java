package application;

import java.util.HashMap;
import java.util.Map;

public class BoardPositions {
    // Map to store position number to coordinates
    private static final Map<Integer, double[]> positionMap = new HashMap<>();
    
    // Initialize the position map with all board positions
    static {
        // Format: positionMap.put(positionNumber, new double[]{x, y});
        positionMap.put(1, new double[]{350, 440});
        positionMap.put(2, new double[]{350, 420});
        positionMap.put(3, new double[]{350, 400});
        positionMap.put(4, new double[]{350, 380});
        positionMap.put(5, new double[]{350, 360});
        positionMap.put(6, new double[]{330, 350});
        positionMap.put(7, new double[]{310, 330});
        positionMap.put(8, new double[]{290, 320});
        positionMap.put(9, new double[]{260, 310});
        positionMap.put(10, new double[]{230, 310});
        positionMap.put(11, new double[]{200, 310});
        positionMap.put(12, new double[]{200, 290});
        positionMap.put(13, new double[]{230, 280});
        positionMap.put(14, new double[]{260, 280});
        positionMap.put(15, new double[]{290, 280});
        positionMap.put(16, new double[]{320, 290});
        positionMap.put(17, new double[]{200, 270});
        positionMap.put(18, new double[]{200, 250});
        positionMap.put(19, new double[]{230, 250});
        positionMap.put(20, new double[]{260, 250});
        positionMap.put(21, new double[]{290, 240});
        positionMap.put(22, new double[]{310, 220});
        positionMap.put(23, new double[]{330, 210});
        positionMap.put(24, new double[]{350, 200});
        positionMap.put(25, new double[]{350, 180});
        positionMap.put(26, new double[]{350, 160});
        positionMap.put(27, new double[]{350, 140});
        positionMap.put(28, new double[]{350, 120});
        positionMap.put(29, new double[]{380, 120});
        positionMap.put(30, new double[]{390, 140});
        positionMap.put(31, new double[]{390, 160});
        positionMap.put(32, new double[]{390, 180});
        positionMap.put(33, new double[]{390, 200});
        positionMap.put(34, new double[]{410, 120});
        positionMap.put(35, new double[]{440, 120});
        positionMap.put(36, new double[]{440, 140});
        positionMap.put(37, new double[]{440, 160});
        positionMap.put(38, new double[]{440, 180});
        positionMap.put(39, new double[]{440, 200});
        positionMap.put(40, new double[]{460, 210});
        positionMap.put(41, new double[]{480, 220});
        positionMap.put(42, new double[]{510, 230});
        positionMap.put(43, new double[]{540, 230});
        positionMap.put(44, new double[]{570, 230});
        positionMap.put(45, new double[]{570, 260});
        positionMap.put(46, new double[]{540, 260});
        positionMap.put(47, new double[]{510, 260});
        positionMap.put(48, new double[]{480, 260});
        positionMap.put(49, new double[]{450, 260});
        positionMap.put(50, new double[]{570, 280});
        positionMap.put(51, new double[]{570, 300});
        positionMap.put(52, new double[]{540, 300});
        positionMap.put(53, new double[]{510, 300});
        positionMap.put(54, new double[]{490, 320});
        positionMap.put(55, new double[]{470, 330});
        positionMap.put(56, new double[]{450, 340});
        positionMap.put(57, new double[]{440, 360});
        positionMap.put(58, new double[]{440, 380});
        positionMap.put(59, new double[]{440, 400});
        positionMap.put(60, new double[]{440, 420});
        positionMap.put(61, new double[]{440, 440});
        positionMap.put(62, new double[]{410, 440});
        positionMap.put(63, new double[]{390, 420});
        positionMap.put(64, new double[]{390, 400});
        positionMap.put(65, new double[]{390, 380});
        positionMap.put(66, new double[]{390, 360});
        positionMap.put(67, new double[]{380, 440});
    }
    
    /**
     * Get the X,Y coordinates for a specified position number
     * @param position The position number (1-67)
     * @return double[] array with [x, y] coordinates
     */
    public static double[] getCoordinates(int position) {
        if (position < 1 || position > 67) {
            throw new IllegalArgumentException("Position must be between 1 and 67");
        }
        return positionMap.get(position);
    }
    
    /**
     * Get the X coordinate for a position
     * @param position The position number
     * @return X coordinate
     */
    public static double getX(int position) {
        return getCoordinates(position)[0];
    }
    
    /**
     * Get the Y coordinate for a position
     * @param position The position number
     * @return Y coordinate
     */
    public static double getY(int position) {
        return getCoordinates(position)[1];
    }
}