package com.clearledger.income_service.repository;

import com.clearledger.income_service.entity.IncomeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IncomeEntryRepository extends JpaRepository<IncomeEntry, String> {

    List<IncomeEntry> findByUserIdOrderByEarnedOnDesc(String userId);

    List<IncomeEntry> findByStream_IdAndUserId(String streamId, String userId);

    // Aggregate total income per stream for a given month/year
    // This is the JPQL query that powers the summary endpoint
    @Query("""
    SELECT e.stream.id, e.stream.name,
           SUM(e.amount), COUNT(e)
    FROM IncomeEntry e
    WHERE e.userId = :userId
      AND EXTRACT(MONTH FROM e.earnedOn) = :month
      AND EXTRACT(YEAR  FROM e.earnedOn) = :year
    GROUP BY e.stream.id, e.stream.name
""")
    List<Object[]> findMonthlySummaryByUser(
            @Param("userId") String userId,
            @Param("month")  int month,
            @Param("year")   int year
    );
}

//JPQL @Query with GROUP BY and SUM result is List of Object arrays which we map to DTOs in the service layer.