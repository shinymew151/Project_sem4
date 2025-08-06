package com.ai.video.FacelessVideo.repository;

import com.ai.video.FacelessVideo.entity.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, String> {
    List<PointTransaction> findByUserId(String userId);
    List<PointTransaction> findByType(String type);
    
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.user.id = ?1 ORDER BY pt.createdAt DESC")
    List<PointTransaction> findByUserIdOrderByCreatedAt(String userId);
}