package com.hima.recallrush.repository;

import com.hima.recallrush.model.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    // "Owner" (Deck.owner) yokka "Id" (User.id) -> WHERE owner_id = ?
    List<Deck> findByOwnerId(Long ownerId);

    // SECURITY: deck id + owner id rendu match avvali. Inkokari deck ni access cheyyakunda ee check.
    Optional<Deck> findByIdAndOwnerId(Long id, Long ownerId);
}
