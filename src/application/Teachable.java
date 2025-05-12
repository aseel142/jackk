package application;

/**
 * Interface for players that can learn and improve their gameplay
 * This allows players to evolve from beginner to intermediate to pro levels
 */
public interface Teachable {
    
    /**
     * Improves the player's skill level, returning an upgraded player
     * @return An improved version of the player with better strategy
     */
    Player improve();
    
    /**
     * Gets the current learning level of the player
     * @return The current skill level
     */
    SkillLevel getSkillLevel();
    
    /**
     * Enum to track the player's current skill level
     */
    enum SkillLevel {
        BEGINNER,
        INTERMEDIATE,
        PRO
    }
}