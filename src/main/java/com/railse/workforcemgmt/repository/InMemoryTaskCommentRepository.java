package com.railse.workforcemgmt.repository;


import com.railse.workforcemgmt.model.TaskComment;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryTaskCommentRepository {
    private final Map<Long, List<TaskComment>> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public TaskComment save(TaskComment comment) {
        comment.setId(idCounter.incrementAndGet());
        store.computeIfAbsent(comment.getTaskId(), k -> new ArrayList<>()).add(comment);
        return comment;
    }

    public List<TaskComment> findByTaskId(Long taskId) {
        return store.getOrDefault(taskId, new ArrayList<>());
    }
}

