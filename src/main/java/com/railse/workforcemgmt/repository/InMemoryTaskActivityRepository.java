package com.railse.workforcemgmt.repository;

import com.railse.workforcemgmt.model.TaskActivity;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryTaskActivityRepository {
    private final Map<Long, List<TaskActivity>> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public TaskActivity save(TaskActivity activity) {
        activity.setId(idCounter.incrementAndGet());
        store.computeIfAbsent(activity.getTaskId(), k -> new ArrayList<>()).add(activity);
        return activity;
    }

    public List<TaskActivity> findByTaskId(Long taskId) {
        return store.getOrDefault(taskId, new ArrayList<>());
    }
}

