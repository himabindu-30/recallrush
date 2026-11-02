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
 * SERVICE = business logic.  Analogy: Restaurant lo Chef.
 * Flow:  Browser -> Controller (waiter) -> Service (chef) -> Repository (store room) -> DB
 *
 * Ee class: Decks & Cards CRUD. Important: prathi method lo "userId" tho OWNERSHIP check chestam,
 * anduke oka user inkokari decks ni chudalemu / delete cheyyalemu.
 */
@Service
public class DeckService {

    private final DeckRepository deckRepo;
    private final CardRepository cardRepo;
    private final UserRepository userRepo;

    // Constructor injection: Spring ee objects ni automatic ga istundi (Dependency Injection)
    public DeckService(DeckRepository deckRepo, CardRepository cardRepo, UserRepository userRepo) {
        this.deckRepo = deckRepo;
        this.cardRepo = cardRepo;
        this.userRepo = userRepo;
    }

    // Helper: ee user di aina deck ni techuko, lekapothe 404
    private Deck ownedDeck(Long userId, Long deckId) {
        return deckRepo.findByIdAndOwnerId(deckId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deck not found"));
    }

    // User decks anni + prathi deck ki card count, due count
    // (NOTE: prathi deck ki 2 queries = "N+1 problem". Chinna project ki ok, big ki JOIN query vaadali)
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
        // getReferenceById = DB hit cheyyakunda user "reference" matrame techukuntundi (foreign key set cheyyadaniki chaalu)
        User owner = userRepo.getReferenceById(userId);
        Deck deck = deckRepo.save(new Deck(req.name().trim(), req.description(), owner));
        return new Dtos.DeckResponse(deck.getId(), deck.getName(), deck.getDescription(), 0, 0);
    }

    public void delete(Long userId, Long deckId) {
        Deck deck = ownedDeck(userId, deckId);
        deckRepo.delete(deck); // cascade valla cards kuda delete avthayi
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
     * Kotha user register ayinappudu "Welcome" deck automatic ga create chestam,
     * so first login lo empty screen kakunda study cheyyadaniki cards untayi.
     * @Transactional = anni DB operations oka group: ekkada fail aina anni rollback.
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
