package com.clearledger.income_service.repository;

import com.clearledger.income_service.entity.IncomeStream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeStreamRepository extends JpaRepository<IncomeStream, String> {

    List<IncomeStream> findByUserId(String userId);

    Optional<IncomeStream> findByIdAndUserId(String id, String userId);
}

// findByIdAndUserId is critical for security.
// When a user logs an entry under streamId X, we verify that stream actually belongs to THAT user.
// Without this check, user A could log entries under user B's stream by guessing a UUID.