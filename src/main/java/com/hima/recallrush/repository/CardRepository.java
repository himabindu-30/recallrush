package com.hima.recallrush.repository;

import com.hima.recallrush.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Method names ni chadivi chudandi, English sentence laaga untayi:
 *   "DeckOwnerId"        -> card.deck.owner.id   (relationships lo lopaliki velthundi)
 *   "DueDateLessThanEqual" -> dueDate <= ?
 */
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByDeckId(Long deckId);

    // Deck card counts (deck list lo chupinchadaniki)
    long countByDeckId(Long deckId);
    long countByDeckIdAndDueDateLessThanEqual(Long deckId, LocalDate date);

    // User total counts (stats ki)
    long countByDeckOwnerId(Long userId);
    long countByDeckOwnerIdAndDueDateLessThanEqual(Long userId, LocalDate date);

    // Study session ki: user ki due ayina anni cards
    List<Card> findByDeckOwnerIdAndDueDateLessThanEqual(Long userId, LocalDate date);

    // Study session ki: oka deck lo matrame due cards (owner check tho)
    List<Card> findByDeckIdAndDeckOwnerIdAndDueDateLessThanEqual(Long deckId, Long userId, LocalDate date);

    // Card ni teeskovadam, kani aa card ee user di ayithene
    Optional<Card> findByIdAndDeckOwnerId(Long cardId, Long userId);
}
