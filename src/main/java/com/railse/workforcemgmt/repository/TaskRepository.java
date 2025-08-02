package com.railse.workforcemgmt.repository;

import com.railse.workforcemgmt.model.TaskManagement;
import com.railse.workforcemgmt.model.enums.Priority;

import java.util.List;
import java.util.Optional;


public interface TaskRepository {
    Optional<TaskManagement> findById(Long id);
    TaskManagement save(TaskManagement task);
    List<TaskManagement> findAll();
    List<TaskManagement> findByReferenceIdAndReferenceType(Long referenceId, com.railse.workforcemgmt.common.model.enums.ReferenceType referenceType);
    List<TaskManagement> findByAssigneeIdIn(List<Long> assigneeIds);
    List<TaskManagement> findByPriority(Priority priority);
}



