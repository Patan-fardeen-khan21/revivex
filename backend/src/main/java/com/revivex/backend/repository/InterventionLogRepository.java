package com.revivex.backend.repository;

import com.revivex.backend.entity.InterventionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterventionLogRepository extends JpaRepository<InterventionLog, Long> {
    Optional<InterventionLog> findFirstByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
    List<InterventionLog> findAllByOrderByCreatedAtDesc();
}
