package com.ai.video.FacelessVideo.repository;

import com.ai.video.FacelessVideo.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    List<Video> findByAudioId(String audioId);
    List<Video> findByStatus(String status);
    List<Video> findByLipsyncModel(String lipsyncModel);
    
    @Query("SELECT v FROM Video v JOIN v.audio a ORDER BY a.article.createdAt DESC")
    List<Video> findAllOrderByArticleCreatedAt();
}