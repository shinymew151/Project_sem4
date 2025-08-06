package com.ai.video.FacelessVideo.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class QueueService {
    
    private final BlockingQueue<VideoTask> taskQueue = new LinkedBlockingQueue<>();
    private final AtomicInteger taskIdCounter = new AtomicInteger(0);
    
    public static class VideoTask {
        private final int id;
        private final String headline;
        private final String status;
        private final long timestamp;
        private String result;
        private String error;
        
        public VideoTask(int id, String headline) {
            this.id = id;
            this.headline = headline;
            this.status = "QUEUED";
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters and setters
        public int getId() { return id; }
        public String getHeadline() { return headline; }
        public String getStatus() { return status; }
        public long getTimestamp() { return timestamp; }
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
    
    /**
     * Add a new video generation task to the queue
     */
    public VideoTask addTask(String headline) {
        int taskId = taskIdCounter.incrementAndGet();
        VideoTask task = new VideoTask(taskId, headline);
        
        try {
            taskQueue.put(task);
            System.out.println("📋 Task " + taskId + " added to queue: " + headline);
            return task;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to add task to queue", e);
        }
    }
    
    /**
     * Get the next task from the queue (blocking)
     */
    public VideoTask getNextTask() throws InterruptedException {
        return taskQueue.take();
    }
    
    /**
     * Get the current queue size
     */
    public int getQueueSize() {
        return taskQueue.size();
    }
    
    /**
     * Check if queue is empty
     */
    public boolean isEmpty() {
        return taskQueue.isEmpty();
    }
    
    /**
     * Get queue status information
     */
    public String getQueueStatus() {
        return String.format("Queue size: %d, Total tasks processed: %d", 
                           getQueueSize(), taskIdCounter.get());
    }
}