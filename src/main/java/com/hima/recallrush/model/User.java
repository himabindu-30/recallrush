package com.hima.recallrush.model;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * USER entity = "users" table lo oka row.
 *
 * NOTE: table peru "users" (user kaadu) endukante "user" anedi database lo reserved keyword!
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment id
    private Long id;

    @Column(unique = true, nullable = false) // same username rendu sarlu undakudadu
    private String username;

    @Column(nullable = false)
    private String password; // BCrypt HASH save avthundi, asalu password kaadu!

    // ----- Gamification fields -----
    private int xp = 0;                // total experience points
    private int streak = 0;            // continuous ga ela days study chesaro
    private LocalDate lastStudyDate;   // last ga study chesina date (streak calculate cheyyadaniki)

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ---------- Getters & Setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public LocalDate getLastStudyDate() { return lastStudyDate; }
    public void setLastStudyDate(LocalDate lastStudyDate) { this.lastStudyDate = lastStudyDate; }
}
