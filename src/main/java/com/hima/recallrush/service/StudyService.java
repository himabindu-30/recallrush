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
 * Study service: the core of the application. When a card is reviewed it:
 *   1. Reschedules the card using SM-2
 *   2. Awards XP
 *   3. Updates the daily streak
 *   4. Saves a history entry (ReviewLog)
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

    // Cards due today or earlier. If deckId is given, only that deck; otherwise all decks.
    public List<Card> dueCards(Long userId, Long deckId) {
        LocalDate today = LocalDate.now();
        if (deckId == null) {
            return cardRepo.findByDeckOwnerIdAndDueDateLessThanEqual(userId, today);
        }
        return cardRepo.findByDeckIdAndDeckOwnerIdAndDueDateLessThanEqual(deckId, userId, today);
    }

    /**
     * @Transactional makes all database changes here (card, user, log) one atomic unit.
     * If anything fails midway, everything is rolled back, like a bank transfer
     * where the debit and the credit must both succeed or both be undone.
     *
     * Dirty checking: card and user are loaded as managed entities, so when their values
     * change Hibernate automatically issues the UPDATE statements when the transaction commits.
     */
    @Transactional
    public Dtos.ReviewResult review(Long userId, Long cardId, int quality) {
        Card card = cardRepo.findByIdAndDeckOwnerId(cardId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        LocalDate today = LocalDate.now();

        // 1. SM-2: calculate the card's next due date
        Sm2.apply(card, quality, today);

        // 2. XP
        int gained = Gamification.xpFor(quality);
        user.setXp(user.getXp() + gained);

        // 3. Streak
        updateStreak(user, today);

        // 4. History entry (used by the heatmap and accuracy statistics)
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
     * Streak logic:
     *   - already studied today        -> no change
     *   - studied yesterday            -> streak + 1
     *   - longer gap (or first time)   -> streak restarts at 1
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
