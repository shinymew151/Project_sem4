package com.ai.video.FacelessVideo.repository;

import com.ai.video.FacelessVideo.entity.ScheduledTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, String> {
    List<ScheduledTask> findByUserId(String userId);
    List<ScheduledTask> findByStatus(String status);
    List<ScheduledTask> findByTaskType(String taskType);
    
    @Query("SELECT st FROM ScheduledTask st WHERE st.status = 'ACTIVE' AND st.lastRunAt < ?1")
    List<ScheduledTask> findTasksToRun(LocalDateTime cutoff);
}