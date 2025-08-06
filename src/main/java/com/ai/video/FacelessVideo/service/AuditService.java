package com.ai.video.FacelessVideo.service;

import com.ai.video.FacelessVideo.entity.AuditLog;
import com.ai.video.FacelessVideo.entity.User;
import com.ai.video.FacelessVideo.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;
    
    public void logAction(String action, String userId, String targetType, String targetId, String details) {
        try {
            AuditLog log = new AuditLog();
            log.setAction(action);
            if (userId != null) {
                User user = new User();
                user.setId(userId);
                log.setUser(user);
            }
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            log.setDetails(details);
            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Failed to log audit action: " + e.getMessage());
        }
    }
}


