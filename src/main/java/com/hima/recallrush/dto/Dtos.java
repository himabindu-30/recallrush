package com.hima.recallrush.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * DTOs (Data Transfer Objects) define the shape of API requests and responses.
 *
 * Why not return entities directly:
 *   - The User entity contains the password hash, which must never leak
 *   - Responses need computed fields (cardCount, dueCount) that entities do not have
 *
 * A Java "record" is a compact immutable class: fields, constructor and accessors are generated.
 * Example: new AuthResponse("abc", "hima") -> response.token(), response.username()
 */
public class Dtos {

    private Dtos() {
    } // utility holder; not meant to be instantiated

    // ---------- REQUESTS (frontend -> backend) ----------
    public record AuthRequest(
            @NotBlank @Size(min = 3, max = 30) String username,
            @NotBlank @Size(min = 6, max = 100) String password) {
    }

    public record DeckRequest(@NotBlank String name, String description) {
    }

    public record CardRequest(@NotBlank String front, @NotBlank String back) {
    }

    // quality: 0 (blackout) to 5 (perfect). Frontend buttons: Again=1, Hard=3, Good=4, Easy=5
    public record ReviewRequest(@Min(0) @Max(5) int quality) {
    }

    // ---------- RESPONSES (backend -> frontend) ----------
    public record AuthResponse(String token, String username) {
    }

    public record DeckResponse(Long id, String name, String description, long cardCount, long dueCount) {
    }

    // Returned after a card review: next due date, XP earned, level and streak
    public record ReviewResult(String nextDue, int intervalDays, int xpGained,
                               int totalXp, int level, int streak) {
    }

    public record StatsResponse(long totalCards, long dueToday, long totalReviews, int accuracy,
                                int xp, int level, int levelStartXp, int nextLevelXp, int streak,
                                Map<String, Long> heatmap) {
    }

    public record LeaderboardEntry(String username, int xp, int level, int streak) {
    }
}
