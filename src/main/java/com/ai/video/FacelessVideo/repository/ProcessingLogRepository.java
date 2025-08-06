package com.ai.video.FacelessVideo.repository;

import com.ai.video.FacelessVideo.entity.ProcessingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProcessingLogRepository extends JpaRepository<ProcessingLog, String> {
    List<ProcessingLog> findByRelatedId(String relatedId);
    List<ProcessingLog> findByStage(String stage);
    List<ProcessingLog> findByStatus(String status);
    
    @Query("SELECT pl FROM ProcessingLog pl WHERE pl.relatedId = ?1 ORDER BY pl.timestamp DESC")
    List<ProcessingLog> findByRelatedIdOrderByTimestamp(String relatedId);
}