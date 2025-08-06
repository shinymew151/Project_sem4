package com.ai.video.FacelessVideo.repository;

import com.ai.video.FacelessVideo.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    List<Post> findByVideoId(String videoId);
    List<Post> findByArticleId(String articleId);
    List<Post> findByPlatform(String platform);
    List<Post> findByStatus(String status);
}