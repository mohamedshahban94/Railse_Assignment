package com.railse.workforcemgmt.model;

import lombok.Data;

@Data
public class TaskComment {
    private Long id;
    private Long taskId;
    private String commenter;
    private String comment;
    private Long timestamp;
}

