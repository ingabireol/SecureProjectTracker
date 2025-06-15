package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.dto.task.*;
import com.buildmaster.projecttracker.model.TaskStatus;
import com.buildmaster.projecttracker.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TaskController {
    
    private final TaskService taskService;
    
    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(
            @Valid @RequestBody TaskRequestDto taskRequestDto,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Creating new task: {}", taskRequestDto.getTitle());
        TaskResponseDto task = taskService.createTask(taskRequestDto, actorName);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }
    
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequestDto taskRequestDto,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Updating task with ID: {}", taskId);
        TaskResponseDto task = taskService.updateTask(taskId, taskRequestDto, actorName);
        return ResponseEntity.ok(task);
    }
    
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long taskId,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Deleting task with ID: {}", taskId);
        taskService.deleteTask(taskId, actorName);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponseDto> getTask(@PathVariable Long taskId) {
        log.debug("Fetching task with ID: {}", taskId);
        TaskResponseDto task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(task);
    }
    
    @GetMapping
    public ResponseEntity<Page<TaskResponseDto>> getAllTasks(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.debug("Fetching all tasks with pagination");
        Page<TaskResponseDto> tasks = taskService.getAllTasks(pageable);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/project/{projectId}")
    public ResponseEntity<Page<TaskResponseDto>> getTasksByProject(
            @PathVariable Long projectId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.debug("Fetching tasks by project ID: {}", projectId);
        Page<TaskResponseDto> tasks = taskService.getTasksByProject(projectId, pageable);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/developer/{developerId}")
    public ResponseEntity<Page<TaskResponseDto>> getTasksByDeveloper(
            @PathVariable Long developerId,
            @PageableDefault(size = 20, sort = "dueDate") Pageable pageable) {
        log.debug("Fetching tasks by developer ID: {}", developerId);
        Page<TaskResponseDto> tasks = taskService.getTasksByDeveloper(developerId, pageable);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<TaskResponseDto>> getTasksByStatus(
            @PathVariable TaskStatus status,
            @PageableDefault(size = 20, sort = "dueDate") Pageable pageable) {
        log.debug("Fetching tasks by status: {}", status);
        Page<TaskResponseDto> tasks = taskService.getTasksByStatus(status, pageable);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/unassigned")
    public ResponseEntity<Page<TaskResponseDto>> getUnassignedTasks(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.debug("Fetching unassigned tasks");
        Page<TaskResponseDto> tasks = taskService.getUnassignedTasks(pageable);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<TaskResponseDto>> searchTasks(
            @RequestParam String title,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        log.debug("Searching tasks by title: {}", title);
        Page<TaskResponseDto> tasks = taskService.searchTasksByTitle(title, pageable);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponseDto>> getOverdueTasks() {
        log.debug("Fetching overdue tasks");
        List<TaskResponseDto> tasks = taskService.getOverdueTasks();
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/due-within")
    public ResponseEntity<List<TaskResponseDto>> getTasksDueWithin(@RequestParam(defaultValue = "7") int days) {
        log.debug("Fetching tasks due within {} days", days);
        List<TaskResponseDto> tasks = taskService.getTasksDueWithin(days);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/without-due-date")
    public ResponseEntity<List<TaskResponseDto>> getTasksWithoutDueDate() {
        log.debug("Fetching tasks without due date");
        List<TaskResponseDto> tasks = taskService.getTasksWithoutDueDate();
        return ResponseEntity.ok(tasks);
    }
    
    @PostMapping("/{taskId}/assign/{developerId}")
    public ResponseEntity<TaskResponseDto> assignTask(
            @PathVariable Long taskId,
            @PathVariable Long developerId,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Assigning task {} to developer {}", taskId, developerId);
        TaskResponseDto task = taskService.assignTask(taskId, developerId, actorName);
        return ResponseEntity.ok(task);
    }
    
    @PostMapping("/{taskId}/unassign")
    public ResponseEntity<TaskResponseDto> unassignTask(
            @PathVariable Long taskId,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Unassigning task {}", taskId);
        TaskResponseDto task = taskService.unassignTask(taskId, actorName);
        return ResponseEntity.ok(task);
    }
    
    @PostMapping("/bulk-assign")
    public ResponseEntity<Map<String, Object>> bulkAssignTasks(
            @RequestParam List<Long> taskIds,
            @RequestParam Long developerId,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Bulk assigning {} tasks to developer {}", taskIds.size(), developerId);
        int updatedCount = taskService.bulkAssignTasks(taskIds, developerId, actorName);
        return ResponseEntity.ok(Map.of("updatedCount", updatedCount, "taskIds", taskIds));
    }
    
    @PostMapping("/project/{projectId}/bulk-update-status")
    public ResponseEntity<Map<String, Object>> bulkUpdateTaskStatusByProject(
            @PathVariable Long projectId,
            @RequestParam TaskStatus status,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Bulk updating task status to {} for project {}", status, projectId);
        int updatedCount = taskService.bulkUpdateTaskStatusByProject(projectId, status, actorName);
        return ResponseEntity.ok(Map.of("updatedCount", updatedCount, "projectId", projectId, "status", status));
    }
    
    @PostMapping("/bulk-update")
    public ResponseEntity<List<TaskResponseDto>> bulkUpdateTasks(
            @RequestParam List<Long> taskIds,
            @Valid @RequestBody BulkTaskUpdateDto updateDto,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Bulk updating {} tasks", taskIds.size());
        List<TaskResponseDto> tasks = taskService.bulkUpdateTasks(taskIds, updateDto, actorName);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/search-criteria")
    public ResponseEntity<Page<TaskResponseDto>> getTasksByCriteria(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long developerId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String title,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.debug("Fetching tasks by criteria - project: {}, developer: {}, status: {}, title: {}", 
                 projectId, developerId, status, title);
        Page<TaskResponseDto> tasks = taskService.getTasksByCriteria(projectId, developerId, status, title, pageable);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/statistics/overall")
    public ResponseEntity<Map<String, Long>> getOverallTaskStatistics() {
        log.debug("Fetching overall task statistics");
        Map<String, Long> statistics = taskService.getOverallTaskStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/project/{projectId}")
    public ResponseEntity<Map<String, Long>> getTaskStatusStatisticsByProject(@PathVariable Long projectId) {
        log.debug("Fetching task status statistics for project: {}", projectId);
        Map<String, Long> statistics = taskService.getTaskStatusStatisticsByProject(projectId);
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/developer/{developerId}")
    public ResponseEntity<Map<String, Long>> getTaskStatusStatisticsByDeveloper(@PathVariable Long developerId) {
        log.debug("Fetching task status statistics for developer: {}", developerId);
        Map<String, Long> statistics = taskService.getTaskStatusStatisticsByDeveloper(developerId);
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/created-range")
    public ResponseEntity<List<TaskResponseDto>> getTasksByCreationDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("Fetching tasks by creation date range: {} to {}", startDate, endDate);
        List<TaskResponseDto> tasks = taskService.getTasksByCreationDateRange(startDate, endDate);
        return ResponseEntity.ok(tasks);
    }
}