package com.railse.workforcemgmt.service;

import com.railse.workforcemgmt.dto.*;
import com.railse.workforcemgmt.model.enums.Priority;

import java.util.List;

public interface TaskManagementService {
    List<TaskManagementDto> createTasks(TaskCreateRequest request);
    List<TaskManagementDto> updateTasks(UpdateTaskRequest request);
    String assignByReference(AssignByReferenceRequest request);
    List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request);
    TaskManagementDto findTaskById(Long id);
    TaskManagementDto updateTaskPriority(Long taskId, Priority priority);
    List<TaskManagementDto> getTasksByPriority(Priority priority);
    List<TaskManagementDto> fetchSmartTasksByDate(TaskFetchByDateRequest request);
    TaskDetailsDto findTaskDetailsById(Long id);
}


