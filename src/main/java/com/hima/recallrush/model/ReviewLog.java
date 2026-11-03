package com.hima.recallrush.model;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * ReviewLog: one history entry written every time a card is reviewed.
 * It is the data source for the activity heatmap and the accuracy statistic.
 *
 * DESIGN NOTE: userId and cardId are plain Long values rather than foreign keys,
 * so the history survives even after a card or deck is deleted.
 */
@Entity
@Table(name = "review_logs")
public class ReviewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long cardId;
    private int quality;          // rating from 0 to 5
    private LocalDate reviewedOn; // the day the review happened
    private int xpGained;

    public ReviewLog() {
    }

    public ReviewLog(Long userId, Long cardId, int quality, LocalDate reviewedOn, int xpGained) {
        this.userId = userId;
        this.cardId = cardId;
        this.quality = quality;
        this.reviewedOn = reviewedOn;
        this.xpGained = xpGained;
    }

    // ---------- Getters & Setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }

    public int getQuality() { return quality; }
    public void setQuality(int quality) { this.quality = quality; }

    public LocalDate getReviewedOn() { return reviewedOn; }
    public void setReviewedOn(LocalDate reviewedOn) { this.reviewedOn = reviewedOn; }

    public int getXpGained() { return xpGained; }
    public void setXpGained(int xpGained) { this.xpGained = xpGained; }
}
