package com.railse.workforcemgmt.service.impl;

import com.railse.workforcemgmt.common.exception.ResourceNotFoundException;
import com.railse.workforcemgmt.dto.*;
import com.railse.workforcemgmt.mapper.ITaskManagementMapper;
import com.railse.workforcemgmt.model.TaskActivity;
import com.railse.workforcemgmt.model.TaskComment;
import com.railse.workforcemgmt.model.TaskManagement;
import com.railse.workforcemgmt.model.enums.Priority;
import com.railse.workforcemgmt.model.enums.Task;
import com.railse.workforcemgmt.model.enums.TaskStatus;
import com.railse.workforcemgmt.repository.InMemoryTaskActivityRepository;
import com.railse.workforcemgmt.repository.InMemoryTaskCommentRepository;
import com.railse.workforcemgmt.repository.TaskRepository;
import com.railse.workforcemgmt.service.TaskManagementService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class TaskManagementServiceImpl implements TaskManagementService {


    private final TaskRepository taskRepository;
    private final ITaskManagementMapper taskMapper;
    private final InMemoryTaskCommentRepository commentRepository;
    private final InMemoryTaskActivityRepository activityRepository;



    public TaskManagementServiceImpl(TaskRepository taskRepository, ITaskManagementMapper taskMapper,
    InMemoryTaskCommentRepository commentRepository, InMemoryTaskActivityRepository activityRepository) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.commentRepository = commentRepository;
        this.activityRepository = activityRepository;
    }


    @Override
    public TaskManagementDto findTaskById(Long id) {
        TaskManagement task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return taskMapper.modelToDto(task);
    }


    @Override
    public List<TaskManagementDto> createTasks(TaskCreateRequest createRequest) {
        List<TaskManagement> createdTasks = new ArrayList<>();
        for (TaskCreateRequest.RequestItem item : createRequest.getRequests()) {
            TaskManagement newTask = new TaskManagement();
            newTask.setReferenceId(item.getReferenceId());
            newTask.setReferenceType(item.getReferenceType());
            newTask.setTask(item.getTask());
            newTask.setAssigneeId(item.getAssigneeId());
            newTask.setPriority(item.getPriority());
            newTask.setTaskDeadlineTime(item.getTaskDeadlineTime());
            newTask.setStatus(TaskStatus.ASSIGNED);
            newTask.setDescription("New task created.");
            createdTasks.add(taskRepository.save(newTask));
        }
        return taskMapper.modelListToDtoList(createdTasks);
    }


    @Override
    public List<TaskManagementDto> updateTasks(UpdateTaskRequest updateRequest) {
        List<TaskManagement> updatedTasks = new ArrayList<>();
        for (UpdateTaskRequest.RequestItem item : updateRequest.getRequests()) {
            TaskManagement task = taskRepository.findById(item.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + item.getTaskId()));


            if (item.getTaskStatus() != null) {
                task.setStatus(item.getTaskStatus());
            }
            if (item.getDescription() != null) {
                task.setDescription(item.getDescription());
            }
            updatedTasks.add(taskRepository.save(task));
        }
        return taskMapper.modelListToDtoList(updatedTasks);
    }


    @Override
    public String assignByReference(AssignByReferenceRequest request) {
        List<Task> applicableTasks = Task.getTasksByReferenceType(request.getReferenceType());
        List<TaskManagement> existingTasks = taskRepository.findByReferenceIdAndReferenceType(request.getReferenceId(), request.getReferenceType());


        for (Task taskType : applicableTasks) {
            List<TaskManagement> tasksOfType = existingTasks.stream()
                    .filter(t -> t.getTask() == taskType &&
                            t.getStatus() != TaskStatus.COMPLETED &&
                            t.getStatus() != TaskStatus.CANCELLED)
                    .collect(Collectors.toList());


            // BUG #1 is here. It should assign one and cancel the rest.
            // Instead, it reassigns ALL of them.
//            if (!tasksOfType.isEmpty()) {
//                for (TaskManagement taskToUpdate : tasksOfType) {
//                    System.out.println(taskToUpdate);
//                    System.out.println(request.getAssigneeId());
//                    taskToUpdate.setAssigneeId(request.getAssigneeId());
//                    taskRepository.save(taskToUpdate);
//                }
            if (!tasksOfType.isEmpty()) {
                TaskManagement taskToAssign = tasksOfType.get(0);
                taskToAssign.setAssigneeId(request.getAssigneeId());
                taskToAssign.setStatus(TaskStatus.ASSIGNED);
                taskRepository.save(taskToAssign);

                for (int i = 1; i < tasksOfType.size(); i++) {
                    TaskManagement toCancel = tasksOfType.get(i);
                    toCancel.setStatus(TaskStatus.CANCELLED);
                    taskRepository.save(toCancel);
                }
            } else {
                // Create a new task if none exist
                TaskManagement newTask = new TaskManagement();
                newTask.setReferenceId(request.getReferenceId());
                newTask.setReferenceType(request.getReferenceType());
                newTask.setTask(taskType);
                newTask.setAssigneeId(request.getAssigneeId());
                newTask.setStatus(TaskStatus.ASSIGNED);
                taskRepository.save(newTask);
            }
        }
        return "Tasks assigned successfully for reference " + request.getReferenceId();
    }


    @Override
    public List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request) {
        List<TaskManagement> tasks = taskRepository.findByAssigneeIdIn(request.getAssigneeIds());


        // BUG #2 is here. It should filter out CANCELLED tasks but doesn't.
        List<TaskManagement> filteredTasks = tasks.stream()
                .filter(task ->
//                        {
//                            return true;
//                        }
                            task.getStatus() != TaskStatus.CANCELLED &&
                            task.getTaskDeadlineTime() != null &&
                            task.getTaskDeadlineTime() >= request.getStartDate() &&
                            task.getTaskDeadlineTime() <= request.getEndDate()
                )
                .collect(Collectors.toList());


        return taskMapper.modelListToDtoList(filteredTasks);
    }

    @Override
    public TaskManagementDto updateTaskPriority(Long taskId, Priority priority) {
        TaskManagement task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        task.setPriority(priority);
        taskRepository.save(task);

        return taskMapper.modelToDto(task);
    }

    @Override
    public List<TaskManagementDto> getTasksByPriority(Priority priority) {
        List<TaskManagement> tasks = taskRepository.findByPriority(priority);
        return taskMapper.modelListToDtoList(tasks);
    }

    @Override
    public List<TaskManagementDto> fetchSmartTasksByDate(TaskFetchByDateRequest request) {
        List<TaskManagement> allTasks = taskRepository.findByAssigneeIdIn(request.getAssigneeIds());

        List<TaskManagement> filtered = allTasks.stream()
                .filter(task -> {
                    boolean isActive = task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.CANCELLED;
                    boolean hasDeadline = task.getTaskDeadlineTime() != null;

                    boolean createdWithinRange = task.getCreatedAt() != null &&
                            task.getCreatedAt() >= request.getStartDate() &&
                            task.getCreatedAt() <= request.getEndDate();

                    boolean createdBeforeAndStillOpen = task.getCreatedAt() != null &&
                            task.getCreatedAt() < request.getStartDate() &&
                            isActive;

                    return isActive && hasDeadline && (createdWithinRange || createdBeforeAndStillOpen);
                })
                .collect(Collectors.toList());

        return taskMapper.modelListToDtoList(filtered);
    }

    @Override
    public TaskDetailsDto findTaskDetailsById(Long id) {
        TaskManagement task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        List<TaskComment> comments = commentRepository.findByTaskId(id);
        List<TaskActivity> activity = activityRepository.findByTaskId(id);

        TaskDetailsDto dto = new TaskDetailsDto();
        dto.setTask(taskMapper.modelToDto(task));
        dto.setComments(comments.stream().sorted(Comparator.comparing(TaskComment::getTimestamp)).toList());
        dto.setActivity(activity.stream().sorted(Comparator.comparing(TaskActivity::getTimestamp)).toList());
        return dto;
    }


}


