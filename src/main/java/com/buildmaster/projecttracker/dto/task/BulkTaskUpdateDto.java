package com.buildmaster.projecttracker.dto.task;

import com.buildmaster.projecttracker.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkTaskUpdateDto {
    private TaskStatus status;
    private Long assignedDeveloperId;
    private LocalDateTime dueDate;
}