package com.railse.workforcemgmt.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.railse.workforcemgmt.model.TaskActivity;
import com.railse.workforcemgmt.model.TaskComment;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TaskDetailsDto {
    private TaskManagementDto task;
    private List<TaskComment> comments;
    private List<TaskActivity> activity;
}
