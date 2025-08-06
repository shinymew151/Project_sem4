package com.ai.video.FacelessVideo.repository;

import com.ai.video.FacelessVideo.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, String> {
    List<Keyword> findBySourceContentId(String sourceContentId);
    
    @Query("SELECT k FROM Keyword k WHERE k.score >= ?1 ORDER BY k.score DESC")
    List<Keyword> findHighScoringKeywords(Float minScore);
    
    @Query("SELECT k FROM Keyword k ORDER BY k.score DESC")
    List<Keyword> findTopKeywords();
}
