package com.clearledger.user_service.repository;

import com.clearledger.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}

//Spring Data JPA generates the SQL for findByEmail and existsByEmail automatically from the method name. No SQL needed.