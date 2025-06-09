package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.dto.project.ProjectDetailResponseDto;
import com.buildmaster.projecttracker.dto.project.ProjectRequestDto;
import com.buildmaster.projecttracker.dto.project.ProjectResponseDto;
import com.buildmaster.projecttracker.model.ProjectStatus;
import com.buildmaster.projecttracker.service.ProjectService;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProjectController {
    
    private final ProjectService projectService;
    
    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(
            @Valid @RequestBody ProjectRequestDto projectRequestDto,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Creating new project: {}", projectRequestDto.getName());
        ProjectResponseDto project = projectService.createProject(projectRequestDto, actorName);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }
    
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequestDto projectRequestDto,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Updating project with ID: {}", projectId);
        ProjectResponseDto project = projectService.updateProject(projectId, projectRequestDto, actorName);
        return ResponseEntity.ok(project);
    }
    
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Deleting project with ID: {}", projectId);
        projectService.deleteProject(projectId, actorName);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> getProject(@PathVariable Long projectId) {
        log.debug("Fetching project with ID: {}", projectId);
        ProjectResponseDto project = projectService.getProjectById(projectId);
        return ResponseEntity.ok(project);
    }
    
    @GetMapping("/{projectId}/details")
    public ResponseEntity<ProjectDetailResponseDto> getProjectDetails(@PathVariable Long projectId) {
        log.debug("Fetching project details with ID: {}", projectId);
        ProjectDetailResponseDto projectDetails = projectService.getProjectDetails(projectId);
        return ResponseEntity.ok(projectDetails);
    }
    
    @GetMapping
    public ResponseEntity<Page<ProjectResponseDto>> getAllProjects(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.debug("Fetching all projects with pagination");
        Page<ProjectResponseDto> projects = projectService.getAllProjects(pageable);
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ProjectResponseDto>> getProjectsByStatus(
            @PathVariable ProjectStatus status,
            @PageableDefault(size = 20, sort = "deadline") Pageable pageable) {
        log.debug("Fetching projects by status: {}", status);
        Page<ProjectResponseDto> projects = projectService.getProjectsByStatus(status, pageable);
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<ProjectResponseDto>> searchProjects(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.debug("Searching projects by name: {}", name);
        Page<ProjectResponseDto> projects = projectService.searchProjectsByName(name, pageable);
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/without-tasks")
    public ResponseEntity<List<ProjectResponseDto>> getProjectsWithoutTasks() {
        log.debug("Fetching projects without tasks");
        List<ProjectResponseDto> projects = projectService.getProjectsWithoutTasks();
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/overdue")
    public ResponseEntity<List<ProjectResponseDto>> getOverdueProjects() {
        log.debug("Fetching overdue projects");
        List<ProjectResponseDto> projects = projectService.getOverdueProjects();
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/deadline-range")
    public ResponseEntity<List<ProjectResponseDto>> getProjectsByDeadlineRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("Fetching projects by deadline range: {} to {}", startDate, endDate);
        List<ProjectResponseDto> projects = projectService.getProjectsByDeadlineRange(startDate, endDate);
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/created-range")
    public ResponseEntity<List<ProjectResponseDto>> getProjectsByCreationDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("Fetching projects by creation date range: {} to {}", startDate, endDate);
        List<ProjectResponseDto> projects = projectService.getProjectsByCreationDateRange(startDate, endDate);
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/statistics/status")
    public ResponseEntity<Map<String, Long>> getProjectStatusStatistics() {
        log.debug("Fetching project status statistics");
        Map<String, Long> statistics = projectService.getProjectStatusStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkProjectExists(@RequestParam String name) {
        log.debug("Checking if project exists with name: {}", name);
        boolean exists = projectService.existsByName(name);
        return ResponseEntity.ok(exists);
    }
}