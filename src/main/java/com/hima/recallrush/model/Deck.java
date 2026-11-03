package com.hima.recallrush.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Deck: a named group of flashcards (for example "Java Basics" or "DBMS Unit 2").
 *
 * Relationships:
 *   User (1) ----- (many) Deck   via "owner"
 *   Deck (1) ----- (many) Card   via "cards"
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

    // Many decks belong to one user. LAZY = the user is loaded only when accessed (better performance).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id") // foreign key column name
    @JsonIgnore                    // keep out of JSON (avoids infinite recursion and leaking user data)
    private User owner;

    // One deck has many cards. mappedBy = the "deck" field in Card owns the relationship.
    // cascade ALL: deleting a deck also deletes its cards.
    // orphanRemoval: removing a card from this list deletes it from the database.
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
