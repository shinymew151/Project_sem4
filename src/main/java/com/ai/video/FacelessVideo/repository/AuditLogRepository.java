package com.ai.video.FacelessVideo.repository;

import com.ai.video.FacelessVideo.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    List<AuditLog> findByUserId(String userId);
    List<AuditLog> findByAction(String action);
    List<AuditLog> findByTargetTypeAndTargetId(String targetType, String targetId);
    
    @Query("SELECT al FROM AuditLog al WHERE al.timestamp > ?1 ORDER BY al.timestamp DESC")
    List<AuditLog> findRecentLogs(LocalDateTime since);
}