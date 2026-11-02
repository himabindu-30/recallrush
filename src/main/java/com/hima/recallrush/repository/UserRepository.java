package com.hima.recallrush.repository;

import com.hima.recallrush.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY = database tho matladey layer (SQL rayalsina pani ledu).
 * Method peru chusi Spring ae query raayalo automatic ga ardham chesukuntundi.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM users WHERE username = ?
    Optional<User> findByUsername(String username);

    // username already undo ledo check
    boolean existsByUsername(String username);

    // Leaderboard: XP ekkuva nundi takkuva ki sort chesi top 10
    List<User> findTop10ByOrderByXpDesc();
}
