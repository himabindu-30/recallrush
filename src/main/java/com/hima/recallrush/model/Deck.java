package com.hima.recallrush.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * DECK = flashcards group (ex: "Java Basics", "DBMS Unit 2").
 *
 * Relationships:
 *   User (1) ----- (many) Deck      -> "owner"
 *   Deck (1) ----- (many) Card      -> "cards"
 */
@Entity
@Table(name = "decks")
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    // MANY decks -> ONE user. LAZY = deck load chesinappudu user ni apudu techukoadu (performance ki manchidi)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id") // DB lo foreign key column peru
    @JsonIgnore                    // JSON lo pampakoodadu (infinite loop & password leak avvakunda)
    private User owner;

    // ONE deck -> MANY cards. mappedBy = "Card.deck" field ee relationship ni own chestundi
    // cascade ALL = deck delete chesthe cards kuda automatic ga delete avthayi
    // orphanRemoval = list nundi card teesesthe DB nundi kuda delete
    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Card> cards = new ArrayList<>();

    public Deck() {
    }

    public Deck(String name, String description, User owner) {
        this.name = name;
        this.description = description;
        this.owner = owner;
    }

    // ---------- Getters & Setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public List<Card> getCards() { return cards; }
    public void setCards(List<Card> cards) { this.cards = cards; }
}
