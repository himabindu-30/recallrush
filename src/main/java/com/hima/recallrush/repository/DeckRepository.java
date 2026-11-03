package com.hima.recallrush.repository;

import com.hima.recallrush.model.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    // "Owner" (Deck.owner) + "Id" (User.id) -> WHERE owner_id = ?
    List<Deck> findByOwnerId(Long ownerId);

    // SECURITY: both the deck id and the owner id must match,
    // so a user can never access another user's deck.
    Optional<Deck> findByIdAndOwnerId(Long id, Long ownerId);
}
