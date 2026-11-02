package com.hima.recallrush.service;

/**
 * GAMIFICATION rules (XP + Levels). Duolingo laaga study ni game laaga marchadam.
 */
public final class Gamification {

    private Gamification() {
    }

    // Rating batti XP: fail = 2 (try chesinanduku), Hard(3)=10, Good(4)=15, Easy(5)=20
    public static int xpFor(int quality) {
        return quality >= 3 ? 10 + (quality - 3) * 5 : 2;
    }

    // Level = 1 + floor( sqrt(xp / 100) )
    // XP 0-99 -> L1, 100-399 -> L2, 400-899 -> L3, 900-1599 -> L4 ...
    // (level perige koddi ekkuva XP kavali: sqrt curve)
    public static int levelFor(int xp) {
        return 1 + (int) Math.sqrt(xp / 100.0);
    }

    // Ee level start ayye XP:  (L-1)^2 * 100
    public static int levelStartXp(int level) {
        return (level - 1) * (level - 1) * 100;
    }

    // Next level ki kavalsina XP:  L^2 * 100
    public static int nextLevelXp(int level) {
        return level * level * 100;
    }
}
