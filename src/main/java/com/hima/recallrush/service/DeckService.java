package com.hima.recallrush.service;

import com.hima.recallrush.dto.Dtos;
import com.hima.recallrush.model.Card;
import com.hima.recallrush.model.Deck;
import com.hima.recallrush.model.User;
import com.hima.recallrush.repository.CardRepository;
import com.hima.recallrush.repository.DeckRepository;
import com.hima.recallrush.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * Service layer: business logic for decks and cards.
 * Request flow: Browser -> Controller -> Service -> Repository -> Database
 *
 * Every method receives the userId and checks ownership, so a user
 * can never view or modify another user's decks or cards.
 */
@Service
public class DeckService {

    private final DeckRepository deckRepo;
    private final CardRepository cardRepo;
    private final UserRepository userRepo;

    // Constructor injection: Spring supplies these dependencies automatically
    public DeckService(DeckRepository deckRepo, CardRepository cardRepo, UserRepository userRepo) {
        this.deckRepo = deckRepo;
        this.cardRepo = cardRepo;
        this.userRepo = userRepo;
    }

    // Helper: returns the deck if it belongs to the user, otherwise responds with 404
    private Deck ownedDeck(Long userId, Long deckId) {
        return deckRepo.findByIdAndOwnerId(deckId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deck not found"));
    }

    // Lists the user's decks with card and due counts.
    // NOTE: this issues 2 queries per deck (the "N+1 problem"). Fine for a small app;
    // a larger system would use a single JOIN query.
    public List<Dtos.DeckResponse> list(Long userId) {
        LocalDate today = LocalDate.now();
        return deckRepo.findByOwnerId(userId).stream()
                .map(d -> new Dtos.DeckResponse(
                        d.getId(),
                        d.getName(),
                        d.getDescription(),
                        cardRepo.countByDeckId(d.getId()),
                        cardRepo.countByDeckIdAndDueDateLessThanEqual(d.getId(), today)))
                .toList();
    }

    public Dtos.DeckResponse create(Long userId, Dtos.DeckRequest req) {
        // getReferenceById returns a lightweight reference without hitting the database;
        // that is enough to set the foreign key.
        User owner = userRepo.getReferenceById(userId);
        Deck deck = deckRepo.save(new Deck(req.name().trim(), req.description(), owner));
        return new Dtos.DeckResponse(deck.getId(), deck.getName(), deck.getDescription(), 0, 0);
    }

    public void delete(Long userId, Long deckId) {
        Deck deck = ownedDeck(userId, deckId);
        deckRepo.delete(deck); // cascade also deletes the deck's cards
    }

    public List<Card> cards(Long userId, Long deckId) {
        ownedDeck(userId, deckId); // ownership check
        return cardRepo.findByDeckId(deckId);
    }

    public Card addCard(Long userId, Long deckId, Dtos.CardRequest req) {
        Deck deck = ownedDeck(userId, deckId);
        return cardRepo.save(new Card(deck, req.front().trim(), req.back().trim()));
    }

    public void deleteCard(Long userId, Long cardId) {
        Card card = cardRepo.findByIdAndDeckOwnerId(cardId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        cardRepo.delete(card);
    }

    /**
     * Creates a sample deck for every new user, so the first login is not an empty screen.
     * @Transactional groups all database operations into one unit: if any fails, all roll back.
     */
    @Transactional
    public void createStarterDeck(User owner) {
        Deck deck = deckRepo.save(new Deck("Java Basics (sample)", "Starter deck, try studying it!", owner));

        String[][] qa = {
                {"What is the JVM?", "Java Virtual Machine: runs bytecode, makes Java platform independent."},
                {"== vs equals()?", "== compares references (same object?). equals() compares content (when overridden)."},
                {"4 pillars of OOP?", "Encapsulation, Inheritance, Polymorphism, Abstraction."},
                {"ArrayList vs LinkedList?", "ArrayList: dynamic array, O(1) random access. LinkedList: doubly linked list, O(1) insert/delete at a known node."},
                {"HashMap get/put time complexity?", "O(1) average, O(n) worst case (Java 8+ treeifies long buckets to O(log n))."}
        };
        for (String[] pair : qa) {
            cardRepo.save(new Card(deck, pair[0], pair[1]));
        }
    }
}
