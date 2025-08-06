package com.ai.video.FacelessVideo.repository;

import com.ai.video.FacelessVideo.entity.SourceContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SourceContentRepository extends JpaRepository<SourceContent, String> {
    List<SourceContent> findByPlatform(String platform);
    List<SourceContent> findByStatus(String status);
    Optional<SourceContent> findByHash(String hash);
    
    @Query("SELECT sc FROM SourceContent sc WHERE sc.fetchedAt > ?1 ORDER BY sc.fetchedAt DESC")
    List<SourceContent> findRecentContent(LocalDateTime since);
    
    @Query("SELECT sc FROM SourceContent sc WHERE sc.platform = 'vnexpress' ORDER BY sc.fetchedAt DESC")
    List<SourceContent> findVNExpressContent();
}
