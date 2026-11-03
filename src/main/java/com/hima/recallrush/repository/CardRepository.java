package com.hima.recallrush.repository;

import com.hima.recallrush.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Method names read like English sentences:
 *   "DeckOwnerId"          -> card.deck.owner.id (navigates through relationships)
 *   "DueDateLessThanEqual" -> dueDate <= ?
 */
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByDeckId(Long deckId);

    // Per-deck counts (shown in the deck list)
    long countByDeckId(Long deckId);
    long countByDeckIdAndDueDateLessThanEqual(Long deckId, LocalDate date);

    // Per-user counts (used by the stats dashboard)
    long countByDeckOwnerId(Long userId);
    long countByDeckOwnerIdAndDueDateLessThanEqual(Long userId, LocalDate date);

    // Study session: all cards due for a user
    List<Card> findByDeckOwnerIdAndDueDateLessThanEqual(Long userId, LocalDate date);

    // Study session: due cards of one deck (with an ownership check)
    List<Card> findByDeckIdAndDeckOwnerIdAndDueDateLessThanEqual(Long deckId, Long userId, LocalDate date);

    // Fetch a card only if it belongs to the given user
    Optional<Card> findByIdAndDeckOwnerId(Long cardId, Long userId);
}
