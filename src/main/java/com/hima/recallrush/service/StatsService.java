package com.hima.recallrush.service;

import com.hima.recallrush.dto.Dtos;
import com.hima.recallrush.model.User;
import com.hima.recallrush.repository.CardRepository;
import com.hima.recallrush.repository.ReviewLogRepository;
import com.hima.recallrush.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * STATS SERVICE = dashboard numbers + heatmap + leaderboard.
 */
@Service
public class StatsService {

    private final CardRepository cardRepo;
    private final UserRepository userRepo;
    private final ReviewLogRepository logRepo;

    public StatsService(CardRepository cardRepo, UserRepository userRepo, ReviewLogRepository logRepo) {
        this.cardRepo = cardRepo;
        this.userRepo = userRepo;
        this.logRepo = logRepo;
    }

    public Dtos.StatsResponse stats(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        LocalDate today = LocalDate.now();

        long totalCards = cardRepo.countByDeckOwnerId(userId);
        long dueToday = cardRepo.countByDeckOwnerIdAndDueDateLessThanEqual(userId, today);

        // Accuracy = (quality >= 3 reviews) / total reviews x 100
        long totalReviews = logRepo.countByUserId(userId);
        long good = logRepo.countByUserIdAndQualityGreaterThanEqual(userId, 3);
        int accuracy = totalReviews == 0 ? 0 : (int) Math.round(good * 100.0 / totalReviews);

        // Heatmap: last 90 days. Key = "2026-11-02", Value = ee roju ennni reviews
        Map<String, Long> heatmap = new LinkedHashMap<>();
        for (Object[] row : logRepo.heatmap(userId, today.minusDays(89))) {
            heatmap.put(row[0].toString(), (Long) row[1]); // row[0]=date, row[1]=count
        }

        // Streak display: ninna kuda study cheyyakapothe streak 0 ga chupinchali
        int streak = user.getStreak();
        LocalDate last = user.getLastStudyDate();
        if (last == null || last.isBefore(today.minusDays(1))) {
            streak = 0;
        }

        int level = Gamification.levelFor(user.getXp());

        return new Dtos.StatsResponse(
                totalCards, dueToday, totalReviews, accuracy,
                user.getXp(), level,
                Gamification.levelStartXp(level), Gamification.nextLevelXp(level),
                streak, heatmap);
    }

    // Top 10 users by XP
    public List<Dtos.LeaderboardEntry> leaderboard() {
        return userRepo.findTop10ByOrderByXpDesc().stream()
                .map(u -> new Dtos.LeaderboardEntry(
                        u.getUsername(), u.getXp(), Gamification.levelFor(u.getXp()), u.getStreak()))
                .toList();
    }
}
