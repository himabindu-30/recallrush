package com.hima.recallrush.model;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * REVIEW LOG = prathi sari card review chesinappudu oka "history entry".
 * Idi heatmap, accuracy lanti stats ki base.
 *
 * DESIGN NOTE: userId, cardId ni simple Long ga petam (foreign key relation kaadu).
 * Endukante card/deck delete chesina tarvata kuda "history" alaage undali,
 * FK untey delete cheyyadaniki error vastundi.
 */
@Entity
@Table(name = "review_logs")
public class ReviewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long cardId;
    private int quality;          // 0-5 rating
    private LocalDate reviewedOn; // e roju review chesaru
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
