package com.hima.recallrush.service;

import com.hima.recallrush.model.Card;

import java.time.LocalDate;

/**
 * SM-2 (SuperMemo-2) spaced repetition algorithm, the basis of Anki.
 *
 * IDEA: reviewing a fact just before you would forget it strengthens memory the most.
 * Cards you recall easily are shown less often; cards you forget come back sooner.
 *
 * Rating (quality) 0-5:  below 3 = forgotten (fail),  3 or above = recalled (pass)
 *
 * Interval rules:
 *   fail         -> repetitions = 0, interval = 1 day
 *   1st pass     -> interval = 1 day
 *   2nd pass     -> interval = 6 days
 *   3rd+ pass    -> interval = previous interval x easeFactor
 *
 * Example with easeFactor 2.5: 1 -> 6 -> 15 -> 38 -> 94 days
 */
public final class Sm2 {

    private Sm2() {
    }

    public static void apply(Card card, int quality, LocalDate today) {

        if (quality < 3) {
            // Forgotten: start the card over
            card.setRepetitions(0);
            card.setIntervalDays(1);
        } else {
            int reps = card.getRepetitions();
            if (reps == 0) {
                card.setIntervalDays(1);
            } else if (reps == 1) {
                card.setIntervalDays(6);
            } else {
                // previous interval x ease factor, rounded to whole days
                card.setIntervalDays((int) Math.round(card.getIntervalDays() * card.getEaseFactor()));
            }
            card.setRepetitions(reps + 1);
        }

        // Ease factor update (original SM-2 formula):
        // EF' = EF + (0.1 - (5-q) * (0.08 + (5-q) * 0.02))
        // q=5 -> +0.10 (easy), q=4 -> 0, q=3 -> -0.14, q=1 -> -0.54 (hard)
        double diff = 5 - quality;
        double newEase = card.getEaseFactor() + (0.1 - diff * (0.08 + diff * 0.02));
        card.setEaseFactor(Math.max(1.3, newEase)); // never drops below 1.3

        // Next due date = today + interval
        card.setDueDate(today.plusDays(card.getIntervalDays()));
    }
}
