package com.hima.recallrush.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Card: a single flashcard (front = question, back = answer).
 *
 * The fields easeFactor, intervalDays, repetitions and dueDate hold the SM-2
 * spaced repetition state. Together they determine when the card is shown next.
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

    // ----- SM-2 spaced repetition state -----
    private double easeFactor = 2.5;   // how easy the card is; higher grows the interval faster (minimum 1.3)
    private int intervalDays = 0;      // days until the next review
    private int repetitions = 0;       // consecutive successful reviews
    private LocalDate dueDate = LocalDate.now(); // the card is "due" on or after this date

    // Many cards belong to one deck
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
