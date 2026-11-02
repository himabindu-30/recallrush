package com.hima.recallrush.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * DTO = Data Transfer Object. API ki vachey (request) mariyu velley (response) data ki "shape".
 *
 * Endukante entity ni direct ga pampakoodadu:
 *   - User entity lo password hash undi (leak avuthundi!)
 *   - Extra computed fields (cardCount, dueCount) kavali
 *
 * "record" = Java 16+ lo short class. Fields + constructor + getters automatic.
 * Ex: new AuthResponse("abc", "hima")  ->  response.token(), response.username()
 */
public class Dtos {

    private Dtos() {
    } // ee class object create cheyyakudadu, just container

    // ---------- REQUESTS (frontend -> backend) ----------
    public record AuthRequest(
            @NotBlank @Size(min = 3, max = 30) String username,
            @NotBlank @Size(min = 6, max = 100) String password) {
    }

    public record DeckRequest(@NotBlank String name, String description) {
    }

    public record CardRequest(@NotBlank String front, @NotBlank String back) {
    }

    // quality: 0 (blackout) ... 5 (perfect). Frontend buttons: Again=1, Hard=3, Good=4, Easy=5
    public record ReviewRequest(@Min(0) @Max(5) int quality) {
    }

    // ---------- RESPONSES (backend -> frontend) ----------
    public record AuthResponse(String token, String username) {
    }

    public record DeckResponse(Long id, String name, String description, long cardCount, long dueCount) {
    }

    // Card review chesaka frontend ki em cheppali: next date, XP, level, streak
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
