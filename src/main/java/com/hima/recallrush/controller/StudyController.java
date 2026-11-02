package com.hima.recallrush.controller;

import com.hima.recallrush.dto.Dtos;
import com.hima.recallrush.model.Card;
import com.hima.recallrush.service.StudyService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Study session API.
 */
@RestController
@RequestMapping("/api/study")
public class StudyController {

    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    // GET /api/study/due              -> anni decks lo due cards
    // GET /api/study/due?deckId=5     -> aa deck lo matrame
    @GetMapping("/due")
    public List<Card> due(@AuthenticationPrincipal Long userId,
                          @RequestParam(required = false) Long deckId) { // required=false = optional param
        return studyService.dueCards(userId, deckId);
    }

    // POST /api/study/review/12   body: {"quality": 4}
    @PostMapping("/review/{cardId}")
    public Dtos.ReviewResult review(@AuthenticationPrincipal Long userId,
                                    @PathVariable Long cardId,
                                    @Valid @RequestBody Dtos.ReviewRequest req) {
        return studyService.review(userId, cardId, req.quality());
    }
}
