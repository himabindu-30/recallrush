package com.hima.recallrush.service;

/**
 * Gamification rules (XP and levels) that make studying feel like a game.
 */
public final class Gamification {

    private Gamification() {
    }

    // XP by rating: fail = 2 (for trying), Hard(3) = 10, Good(4) = 15, Easy(5) = 20
    public static int xpFor(int quality) {
        return quality >= 3 ? 10 + (quality - 3) * 5 : 2;
    }

    // Level = 1 + floor( sqrt(xp / 100) )
    // XP 0-99 -> L1, 100-399 -> L2, 400-899 -> L3, 900-1599 -> L4 ...
    // The square-root curve means each level needs progressively more XP.
    public static int levelFor(int xp) {
        return 1 + (int) Math.sqrt(xp / 100.0);
    }

    // XP at which the given level begins: (L-1)^2 * 100
    public static int levelStartXp(int level) {
        return (level - 1) * (level - 1) * 100;
    }

    // XP required to reach the next level: L^2 * 100
    public static int nextLevelXp(int level) {
        return level * level * 100;
    }
}
