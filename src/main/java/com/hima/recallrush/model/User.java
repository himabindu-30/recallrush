package com.hima.recallrush.model;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * User entity, stored in the "users" table.
 *
 * NOTE: the table is named "users" because "user" is a reserved keyword in most databases.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment primary key
    private Long id;

    @Column(unique = true, nullable = false) // usernames must be unique
    private String username;

    @Column(nullable = false)
    private String password; // stores the BCrypt hash, never the plain password

    // ----- Gamification fields -----
    private int xp = 0;                // total experience points
    private int streak = 0;            // consecutive days studied
    private LocalDate lastStudyDate;   // last day the user studied (used to compute the streak)

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
