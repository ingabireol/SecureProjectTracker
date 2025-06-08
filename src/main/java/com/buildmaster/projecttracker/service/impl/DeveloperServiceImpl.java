package com.buildmaster.projecttracker.service.impl;

import com.buildmaster.projecttracker.dto.developer.*;
import com.buildmaster.projecttracker.dto.project.TaskSummaryDto;
import com.buildmaster.projecttracker.model.Developer;
import com.buildmaster.projecttracker.model.Task;
import com.buildmaster.projecttracker.repository.DeveloperRepository;
import com.buildmaster.projecttracker.repository.TaskRepository;
import com.buildmaster.projecttracker.service.AuditLogService;
import com.buildmaster.projecttracker.service.DeveloperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeveloperServiceImpl implements DeveloperService {
    
    private final DeveloperRepository developerRepository;
    private final TaskRepository taskRepository;
    private final AuditLogService auditLogService;
    
    @Override
    public DeveloperResponseDto createDeveloper(DeveloperRequestDto developerRequestDto, String actorName) {
        log.info("Creating developer with email: {}", developerRequestDto.getEmail());
        
        // Check if developer with same email already exists
        if (developerRepository.existsByEmail(developerRequestDto.getEmail())) {
            throw new IllegalArgumentException("Developer with email '" + developerRequestDto.getEmail() + "' already exists");
        }
        
        Developer developer = new Developer();
        developer.setName(developerRequestDto.getName());
        developer.setEmail(developerRequestDto.getEmail());
        
        // Process skills (normalize to lowercase)
        if (developerRequestDto.getSkills() != null) {
            Set<String> normalizedSkills = developerRequestDto.getSkills().stream()
                    .filter(Objects::nonNull)
                    .map(skill -> skill.trim().toLowerCase())
                    .filter(skill -> !skill.isEmpty())
                    .collect(Collectors.toSet());
            developer.setSkills(normalizedSkills);
        }
        
        Developer savedDeveloper = developerRepository.save(developer);
        
        // Log the creation
        Map<String, Object> payload = createDeveloperPayload(savedDeveloper);
        auditLogService.logAction("CREATE", "DEVELOPER", savedDeveloper.getId(), actorName, payload);
        
        log.info("Developer created successfully with ID: {}", savedDeveloper.getId());
        return convertToResponseDto(savedDeveloper);
    }
    
    @Override
    @CacheEvict(value = "developers", key = "#developerId")
    public DeveloperResponseDto updateDeveloper(Long developerId, DeveloperRequestDto developerRequestDto, String actorName) {
        log.info("Updating developer with ID: {}", developerId);
        
        Developer developer = findDeveloperById(developerId);
        
        // Store old data for audit
        Map<String, Object> oldData = createDeveloperPayload(developer);
        
        // Check email uniqueness if email is being changed
        if (!developer.getEmail().equals(developerRequestDto.getEmail()) &&
            developerRepository.existsByEmail(developerRequestDto.getEmail())) {
            throw new IllegalArgumentException("Developer with email '" + developerRequestDto.getEmail() + "' already exists");
        }
        
        developer.setName(developerRequestDto.getName());
        developer.setEmail(developerRequestDto.getEmail());
        
        // Process skills (normalize to lowercase)
        if (developerRequestDto.getSkills() != null) {
            Set<String> normalizedSkills = developerRequestDto.getSkills().stream()
                    .filter(Objects::nonNull)
                    .map(skill -> skill.trim().toLowerCase())
                    .filter(skill -> !skill.isEmpty())
                    .collect(Collectors.toSet());
            developer.setSkills(normalizedSkills);
        }
        
        Developer updatedDeveloper = developerRepository.save(developer);
        
        // Log the update
        Map<String, Object> payload = new HashMap<>();
        payload.put("oldData", oldData);
        payload.put("newData", createDeveloperPayload(updatedDeveloper));
        auditLogService.logAction("UPDATE", "DEVELOPER", updatedDeveloper.getId(), actorName, payload);
        
        log.info("Developer updated successfully with ID: {}", updatedDeveloper.getId());
        return convertToResponseDto(updatedDeveloper);
    }
    
    @Override
    @CacheEvict(value = "developers", key = "#developerId")
    public void deleteDeveloper(Long developerId, String actorName) {
        log.info("Deleting developer with ID: {}", developerId);
        
        Developer developer = findDeveloperById(developerId);
        
        // Store data for audit before deletion
        Map<String, Object> payload = createDeveloperPayload(developer);
        
        // Unassign developer from all tasks
        List<Task> assignedTasks = taskRepository.findByAssignedDeveloperId(developerId);
        for (Task task : assignedTasks) {
            task.setAssignedDeveloper(null);
            taskRepository.save(task);
        }
        
        // Delete developer
        developerRepository.delete(developer);
        
        // Log the deletion
        auditLogService.logAction("DELETE", "DEVELOPER", developerId, actorName, payload);
        
        log.info("Developer deleted successfully with ID: {}", developerId);
    }
    
    @Override
    @Cacheable(value = "developers", key = "#developerId")
    public DeveloperResponseDto getDeveloperById(Long developerId) {
        log.debug("Fetching developer with ID: {}", developerId);
        Developer developer = findDeveloperById(developerId);
        return convertToResponseDto(developer);
    }
    
    @Override
    @Cacheable(value = "developerDetails", key = "#developerId")
    public DeveloperDetailResponseDto getDeveloperDetails(Long developerId) {
        log.debug("Fetching developer details with ID: {}", developerId);
        Developer developer = findDeveloperById(developerId);
        return convertToDetailResponseDto(developer);
    }
    
    @Override
    public Page<DeveloperResponseDto> getAllDevelopers(Pageable pageable) {
        log.debug("Fetching all developers with pagination");
        Page<Developer> developers = developerRepository.findAll(pageable);
        return developers.map(this::convertToResponseDto);
    }
    
    @Override
    public Page<DeveloperResponseDto> searchDevelopers(String searchTerm, Pageable pageable) {
        log.debug("Searching developers by term: {}", searchTerm);
        Page<Developer> developers = developerRepository.findByNameOrEmailContaining(searchTerm, pageable);
        return developers.map(this::convertToResponseDto);
    }
    
    @Override
    public List<DeveloperResponseDto> getDevelopersBySkill(String skill) {
        log.debug("Fetching developers by skill: {}", skill);
        List<Developer> developers = developerRepository.findBySkill(skill.toLowerCase());
        return developers.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DeveloperResponseDto> getDevelopersBySkills(List<String> skills) {
        log.debug("Fetching developers by skills: {}", skills);
        List<String> normalizedSkills = skills.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        List<Developer> developers = developerRepository.findBySkillsIn(normalizedSkills);
        return developers.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DeveloperSummaryDto> getTopDevelopersByTaskCount(int limit) {
        log.debug("Fetching top {} developers by task count", limit);
        List<Developer> developers = developerRepository.findTopDevelopersByTaskCount(
                PageRequest.of(0, limit));
        return developers.stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DeveloperResponseDto> getDevelopersWithoutTasks() {
        log.debug("Fetching developers without tasks");
        List<Developer> developers = developerRepository.findDevelopersWithoutTasks();
        return developers.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DeveloperResponseDto> getAvailableDevelopers(int maxTasks) {
        log.debug("Fetching available developers with less than {} tasks", maxTasks);
        List<Developer> developers = developerRepository.findAvailableDevelopers(maxTasks);
        return developers.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @CacheEvict(value = {"developers", "developerDetails"}, key = "#developerId")
    public DeveloperResponseDto addSkillToDeveloper(Long developerId, String skill, String actorName) {
        log.info("Adding skill '{}' to developer with ID: {}", skill, developerId);
        
        Developer developer = findDeveloperById(developerId);
        developer.addSkill(skill);
        
        Developer updatedDeveloper = developerRepository.save(developer);
        
        // Log the skill addition
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "ADD_SKILL");
        payload.put("skill", skill.toLowerCase());
        payload.put("developerId", developerId);
        auditLogService.logAction("UPDATE", "DEVELOPER", developerId, actorName, payload);
        
        return convertToResponseDto(updatedDeveloper);
    }
    
    @Override
    @CacheEvict(value = {"developers", "developerDetails"}, key = "#developerId")
    public DeveloperResponseDto removeSkillFromDeveloper(Long developerId, String skill, String actorName) {
        log.info("Removing skill '{}' from developer with ID: {}", skill, developerId);
        
        Developer developer = findDeveloperById(developerId);
        developer.removeSkill(skill);
        
        Developer updatedDeveloper = developerRepository.save(developer);
        
        // Log the skill removal
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "REMOVE_SKILL");
        payload.put("skill", skill.toLowerCase());
        payload.put("developerId", developerId);
        auditLogService.logAction("UPDATE", "DEVELOPER", developerId, actorName, payload);
        
        return convertToResponseDto(updatedDeveloper);
    }
    
    @Override
    @CacheEvict(value = {"developers", "developerDetails"}, key = "#developerId")
    public DeveloperResponseDto updateDeveloperSkills(Long developerId, Set<String> skills, String actorName) {
        log.info("Updating skills for developer with ID: {}", developerId);
        
        Developer developer = findDeveloperById(developerId);
        
        // Store old skills for audit
        Set<String> oldSkills = new HashSet<>(developer.getSkills());
        
        // Normalize and set new skills
        Set<String> normalizedSkills = skills.stream()
                .filter(Objects::nonNull)
                .map(skill -> skill.trim().toLowerCase())
                .filter(skill -> !skill.isEmpty())
                .collect(Collectors.toSet());
        
        developer.setSkills(normalizedSkills);
        Developer updatedDeveloper = developerRepository.save(developer);
        
        // Log the skill update
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "UPDATE_SKILLS");
        payload.put("oldSkills", oldSkills);
        payload.put("newSkills", normalizedSkills);
        payload.put("developerId", developerId);
        auditLogService.logAction("UPDATE", "DEVELOPER", developerId, actorName, payload);
        
        return convertToResponseDto(updatedDeveloper);
    }
    
    @Override
    public Map<String, Object> getDeveloperTaskStatistics() {
        log.debug("Fetching developer task statistics");
        List<Object[]> stats = developerRepository.getDeveloperTaskStatistics();
        
        Map<String, Object> statistics = new HashMap<>();
        List<Map<String, Object>> developerStats = new ArrayList<>();
        
        for (Object[] stat : stats) {
            Map<String, Object> devStat = new HashMap<>();
            devStat.put("id", stat[0]);
            devStat.put("name", stat[1]);
            devStat.put("email", stat[2]);
            devStat.put("taskCount", ((Number) stat[3]).longValue());
            developerStats.add(devStat);
        }
        
        statistics.put("developerStatistics", developerStats);
        return statistics;
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return developerRepository.existsByEmail(email);
    }
    
    @Override
    public Map<String, Long> getSkillStatistics() {
        log.debug("Fetching skill statistics");
        List<Developer> allDevelopers = developerRepository.findAll();
        
        Map<String, Long> skillStats = new HashMap<>();
        for (Developer developer : allDevelopers) {
            for (String skill : developer.getSkills()) {
                skillStats.merge(skill, 1L, Long::sum);
            }
        }
        
        return skillStats;
    }
    
    // Helper methods
    private Developer findDeveloperById(Long developerId) {
        return developerRepository.findById(developerId)
                .orElseThrow(() -> new RuntimeException("Developer not found with ID: " + developerId));
    }
    
    private DeveloperResponseDto convertToResponseDto(Developer developer) {
        DeveloperResponseDto dto = new DeveloperResponseDto();
        dto.setId(developer.getId());
        dto.setName(developer.getName());
        dto.setEmail(developer.getEmail());
        dto.setSkills(new HashSet<>(developer.getSkills()));
        dto.setTaskCount(developer.getTaskCount());
        dto.setCreatedAt(developer.getCreatedAt());
        dto.setUpdatedAt(developer.getUpdatedAt());
        return dto;
    }
    
    private DeveloperDetailResponseDto convertToDetailResponseDto(Developer developer) {
        DeveloperDetailResponseDto dto = new DeveloperDetailResponseDto();
        dto.setId(developer.getId());
        dto.setName(developer.getName());
        dto.setEmail(developer.getEmail());
        dto.setSkills(new HashSet<>(developer.getSkills()));
        dto.setCreatedAt(developer.getCreatedAt());
        dto.setUpdatedAt(developer.getUpdatedAt());
        
        // Convert assigned tasks to summary DTOs
        if (developer.getAssignedTasks() != null) {
            List<TaskSummaryDto> taskSummaries = developer.getAssignedTasks().stream()
                    .map(this::convertToTaskSummaryDto)
                    .collect(Collectors.toList());
            dto.setAssignedTasks(taskSummaries);
        }
        
        return dto;
    }
    
    private DeveloperSummaryDto convertToSummaryDto(Developer developer) {
        DeveloperSummaryDto dto = new DeveloperSummaryDto();
        dto.setId(developer.getId());
        dto.setName(developer.getName());
        dto.setEmail(developer.getEmail());
        dto.setTaskCount(developer.getTaskCount());
        return dto;
    }
    
    private TaskSummaryDto convertToTaskSummaryDto(Task task) {
        TaskSummaryDto dto = new TaskSummaryDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setStatus(task.getStatus().toString());
        dto.setDueDate(task.getDueDate());
        dto.setAssignedDeveloperName(task.getAssignedDeveloper() != null ? 
                task.getAssignedDeveloper().getName() : null);
        return dto;
    }
    
    private Map<String, Object> createDeveloperPayload(Developer developer) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", developer.getId());
        payload.put("name", developer.getName());
        payload.put("email", developer.getEmail());
        payload.put("skills", developer.getSkills());
        payload.put("taskCount", developer.getTaskCount());
        return payload;
    }
}