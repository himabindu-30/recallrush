package com.hima.recallrush.repository;

import com.hima.recallrush.model.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {

    long countByUserId(Long userId);

    // Used for accuracy: a rating of 3 or higher counts as a correct answer
    long countByUserIdAndQualityGreaterThanEqual(Long userId, int quality);

    /**
     * Heatmap query: number of reviews per day.
     * Custom JPQL (similar to SQL, but uses entity names instead of table names).
     * Each returned row is [date, count].
     * GROUP BY collapses all rows with the same date into one group.
     */
    @Query("select r.reviewedOn, count(r) from ReviewLog r " +
            "where r.userId = :userId and r.reviewedOn >= :since " +
            "group by r.reviewedOn")
    List<Object[]> heatmap(@Param("userId") Long userId, @Param("since") LocalDate since);
}
