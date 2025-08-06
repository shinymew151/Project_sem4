package com.ai.video.FacelessVideo.repository;

import com.ai.video.FacelessVideo.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, String> {
    List<PromptTemplate> findByCategory(String category);
    List<PromptTemplate> findByActiveTrue();
    List<PromptTemplate> findByCategoryAndActiveTrue(String category);
}
