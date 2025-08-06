package com.ai.video.FacelessVideo.repository;

import com.ai.video.FacelessVideo.entity.VideoRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRecordRepository extends JpaRepository<VideoRecord, Long> {
    
    /**
     * Find all video records ordered by creation time (newest first)
     * Using Spring Data JPA method naming convention
     */
    List<VideoRecord> findAllByOrderByIdDesc();
    
    /**
     * Find video records that contain a specific text (case-insensitive)
     * Using Spring Data JPA method naming convention
     */
    List<VideoRecord> findByTextContainingIgnoreCase(String searchText);
    
    /**
     * Find the most recently created video record
     */
    @Query(value = "SELECT * FROM video_record ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<VideoRecord> findLatestRecord();
}