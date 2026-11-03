package com.hima.recallrush.controller;

import com.hima.recallrush.dto.Dtos;
import com.hima.recallrush.model.Card;
import com.hima.recallrush.service.DeckService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for decks and cards.
 *
 * @AuthenticationPrincipal Long userId receives the user id that the JWT filter extracted from the token.
 * The frontend never sends a user id, because a client-supplied id could not be trusted.
 */
@RestController
@RequestMapping("/api")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    // GET /api/decks -> all of the user's decks
    @GetMapping("/decks")
    public List<Dtos.DeckResponse> list(@AuthenticationPrincipal Long userId) {
        return deckService.list(userId);
    }

    // POST /api/decks -> create a deck
    @PostMapping("/decks")
    @ResponseStatus(HttpStatus.CREATED)
    public Dtos.DeckResponse create(@AuthenticationPrincipal Long userId,
                                    @Valid @RequestBody Dtos.DeckRequest req) {
        return deckService.create(userId, req);
    }

    // DELETE /api/decks/5
    @DeleteMapping("/decks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204: success with no response body
    public void delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        deckService.delete(userId, id);
    }

    // GET /api/decks/5/cards -> cards in that deck
    @GetMapping("/decks/{deckId}/cards")
    public List<Card> cards(@AuthenticationPrincipal Long userId, @PathVariable Long deckId) {
        return deckService.cards(userId, deckId);
    }

    // POST /api/decks/5/cards -> add a card to that deck
    @PostMapping("/decks/{deckId}/cards")
    @ResponseStatus(HttpStatus.CREATED)
    public Card addCard(@AuthenticationPrincipal Long userId, @PathVariable Long deckId,
                        @Valid @RequestBody Dtos.CardRequest req) {
        return deckService.addCard(userId, deckId, req);
    }

    // DELETE /api/cards/12
    @DeleteMapping("/cards/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@AuthenticationPrincipal Long userId, @PathVariable Long cardId) {
        deckService.deleteCard(userId, cardId);
    }
}
