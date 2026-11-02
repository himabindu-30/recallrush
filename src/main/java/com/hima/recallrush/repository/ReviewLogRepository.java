package com.hima.recallrush.repository;

import com.hima.recallrush.model.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {

    long countByUserId(Long userId);

    // Accuracy ki: quality >= 3 ante "correct" ga cheppinattu
    long countByUserIdAndQualityGreaterThanEqual(Long userId, int quality);

    /**
     * HEATMAP query: roju ki ennni reviews chesaro.
     * Custom JPQL (SQL laantide, kani table names kaadu Entity names vaadatam).
     * Each row = [date, count]
     * GROUP BY = same date unna rows ni oka group ga chesi count chestundi.
     */
    @Query("select r.reviewedOn, count(r) from ReviewLog r " +
            "where r.userId = :userId and r.reviewedOn >= :since " +
            "group by r.reviewedOn")
    List<Object[]> heatmap(@Param("userId") Long userId, @Param("since") LocalDate since);
}
