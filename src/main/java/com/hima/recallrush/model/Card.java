package com.hima.recallrush.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * CARD = oka flashcard (front = question, back = answer).
 *
 * Fields 4-7 (easeFactor, intervalDays, repetitions, dueDate) SM-2 algorithm ki kavali.
 * Ee 4 values batti "e card ni malli eppudu chudali" ani lekka vestam.
 */
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String front; // question

    @Column(nullable = false, length = 2000)
    private String back;  // answer

    // ----- SM-2 spaced repetition data -----
    private double easeFactor = 2.5;   // card ento easy: ekkuva = interval vegam perugutundi (min 1.3)
    private int intervalDays = 0;      // next review ki entha gap (days)
    private int repetitions = 0;       // varusaga ennisarlu correct ga cheppav
    private LocalDate dueDate = LocalDate.now(); // e date ki or antakante mundu "due" avthundi

    // MANY cards -> ONE deck
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id")
    @JsonIgnore
    private Deck deck;

    public Card() {
    }

    public Card(Deck deck, String front, String back) {
        this.deck = deck;
        this.front = front;
        this.back = back;
    }

    // ---------- Getters & Setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFront() { return front; }
    public void setFront(String front) { this.front = front; }

    public String getBack() { return back; }
    public void setBack(String back) { this.back = back; }

    public double getEaseFactor() { return easeFactor; }
    public void setEaseFactor(double easeFactor) { this.easeFactor = easeFactor; }

    public int getIntervalDays() { return intervalDays; }
    public void setIntervalDays(int intervalDays) { this.intervalDays = intervalDays; }

    public int getRepetitions() { return repetitions; }
    public void setRepetitions(int repetitions) { this.repetitions = repetitions; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Deck getDeck() { return deck; }
    public void setDeck(Deck deck) { this.deck = deck; }
}
