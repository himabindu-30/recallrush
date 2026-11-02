package com.hima.recallrush.controller;

import com.hima.recallrush.dto.Dtos;
import com.hima.recallrush.service.StatsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Dashboard stats & leaderboard API.
 */
@RestController
@RequestMapping("/api")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    // GET /api/stats -> naa numbers + heatmap
    @GetMapping("/stats")
    public Dtos.StatsResponse stats(@AuthenticationPrincipal Long userId) {
        return statsService.stats(userId);
    }

    // GET /api/leaderboard -> top 10 users
    @GetMapping("/leaderboard")
    public List<Dtos.LeaderboardEntry> leaderboard() {
        return statsService.leaderboard();
    }
}
