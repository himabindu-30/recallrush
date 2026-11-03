package com.hima.recallrush.repository;

import com.hima.recallrush.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository: the data access layer (no hand-written SQL needed).
 * Spring Data derives each query from the method name.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM users WHERE username = ?
    Optional<User> findByUsername(String username);

    // Checks whether a username is already taken
    boolean existsByUsername(String username);

    // Leaderboard: top 10 users ordered by XP, highest first
    List<User> findTop10ByOrderByXpDesc();
}
