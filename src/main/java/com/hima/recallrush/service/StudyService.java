package com.hima.recallrush.service;

import com.hima.recallrush.dto.Dtos;
import com.hima.recallrush.model.Card;
import com.hima.recallrush.model.ReviewLog;
import com.hima.recallrush.model.User;
import com.hima.recallrush.repository.CardRepository;
import com.hima.recallrush.repository.ReviewLogRepository;
import com.hima.recallrush.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * STUDY SERVICE = app yokka core. Card review chesinappudu:
 *   1. SM-2 tho card next due date update
 *   2. XP add
 *   3. Streak update
 *   4. History (ReviewLog) save
 */
@Service
public class StudyService {

    private final CardRepository cardRepo;
    private final UserRepository userRepo;
    private final ReviewLogRepository logRepo;

    public StudyService(CardRepository cardRepo, UserRepository userRepo, ReviewLogRepository logRepo) {
        this.cardRepo = cardRepo;
        this.userRepo = userRepo;
        this.logRepo = logRepo;
    }

    // Ivvala study cheyyalsina cards (due date <= today). deckId ichhe aa deck matrame, lekapothe anni.
    public List<Card> dueCards(Long userId, Long deckId) {
        LocalDate today = LocalDate.now();
        if (deckId == null) {
            return cardRepo.findByDeckOwnerIdAndDueDateLessThanEqual(userId, today);
        }
        return cardRepo.findByDeckIdAndDeckOwnerIdAndDueDateLessThanEqual(deckId, userId, today);
    }

    /**
     * @Transactional = ee method lo anni DB changes (card, user, log) oka unit.
     * Madhyalo error vaste anni undo (rollback). Bank transfer laantidi: debit ayyi credit kakapothe ela? Anduke!
     *
     * Managed entity magic: card, user ni repository nundi techukunnam kabatti, values marchesthe
     * transaction ayyaka Hibernate automatic ga UPDATE query run chestundi (dirty checking).
     */
    @Transactional
    public Dtos.ReviewResult review(Long userId, Long cardId, int quality) {
        Card card = cardRepo.findByIdAndDeckOwnerId(cardId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        LocalDate today = LocalDate.now();

        // 1. SM-2: card next due date calculate
        Sm2.apply(card, quality, today);

        // 2. XP
        int gained = Gamification.xpFor(quality);
        user.setXp(user.getXp() + gained);

        // 3. Streak
        updateStreak(user, today);

        // 4. History log (heatmap & accuracy kosam)
        logRepo.save(new ReviewLog(userId, cardId, quality, today, gained));

        return new Dtos.ReviewResult(
                card.getDueDate().toString(),
                card.getIntervalDays(),
                gained,
                user.getXp(),
                Gamification.levelFor(user.getXp()),
                user.getStreak());
    }

    /**
     * STREAK logic:
     *   - ivvala already study chesav  -> em marchadu
     *   - ninna chesav                 -> streak + 1
     *   - ekkuva gap (or first time)   -> streak = 1 (restart)
     */
    private void updateStreak(User user, LocalDate today) {
        LocalDate last = user.getLastStudyDate();

        if (today.equals(last)) {
            return;
        }
        if (last != null && last.equals(today.minusDays(1))) {
            user.setStreak(user.getStreak() + 1);
        } else {
            user.setStreak(1);
        }
        user.setLastStudyDate(today);
    }
}
