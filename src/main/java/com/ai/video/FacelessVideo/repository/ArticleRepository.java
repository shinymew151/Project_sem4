package com.ai.video.FacelessVideo.repository;

import com.ai.video.FacelessVideo.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, String> {
    List<Article> findByStatus(String status);
    List<Article> findByKeywordId(String keywordId);
    List<Article> findByApprovedById(String approvedById);
    
    @Query("SELECT a FROM Article a ORDER BY a.createdAt DESC")
    List<Article> findAllByOrderByCreatedAtDesc();
    
    @Query("SELECT a FROM Article a WHERE a.createdAt > ?1 ORDER BY a.createdAt DESC")
    List<Article> findRecentArticles(LocalDateTime since);
    
    @Query("SELECT a FROM Article a WHERE a.status = 'GENERATED' AND a.approvedBy IS NULL")
    List<Article> findPendingApproval();
}
