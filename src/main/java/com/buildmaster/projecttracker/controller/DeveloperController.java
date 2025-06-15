package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.dto.developer.*;
import com.buildmaster.projecttracker.service.DeveloperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/developers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DeveloperController {
    
    private final DeveloperService developerService;
    
    @PostMapping
    public ResponseEntity<DeveloperResponseDto> createDeveloper(
            @Valid @RequestBody DeveloperRequestDto developerRequestDto,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Creating new developer with email: {}", developerRequestDto.getEmail());
        DeveloperResponseDto developer = developerService.createDeveloper(developerRequestDto, actorName);
        return ResponseEntity.status(HttpStatus.CREATED).body(developer);
    }
    
    @PutMapping("/{developerId}")
    public ResponseEntity<DeveloperResponseDto> updateDeveloper(
            @PathVariable Long developerId,
            @Valid @RequestBody DeveloperRequestDto developerRequestDto,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Updating developer with ID: {}", developerId);
        DeveloperResponseDto developer = developerService.updateDeveloper(developerId, developerRequestDto, actorName);
        return ResponseEntity.ok(developer);
    }
    
    @DeleteMapping("/{developerId}")
    public ResponseEntity<Void> deleteDeveloper(
            @PathVariable Long developerId,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Deleting developer with ID: {}", developerId);
        developerService.deleteDeveloper(developerId, actorName);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{developerId}")
    public ResponseEntity<DeveloperResponseDto> getDeveloper(@PathVariable Long developerId) {
        log.debug("Fetching developer with ID: {}", developerId);
        DeveloperResponseDto developer = developerService.getDeveloperById(developerId);
        return ResponseEntity.ok(developer);
    }
    
    @GetMapping("/{developerId}/details")
    public ResponseEntity<DeveloperDetailResponseDto> getDeveloperDetails(@PathVariable Long developerId) {
        log.debug("Fetching developer details with ID: {}", developerId);
        DeveloperDetailResponseDto developerDetails = developerService.getDeveloperDetails(developerId);
        return ResponseEntity.ok(developerDetails);
    }
    
    @GetMapping
    public ResponseEntity<Page<DeveloperResponseDto>> getAllDevelopers(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.debug("Fetching all developers with pagination");
        Page<DeveloperResponseDto> developers = developerService.getAllDevelopers(pageable);
        return ResponseEntity.ok(developers);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<DeveloperResponseDto>> searchDevelopers(
            @RequestParam String searchTerm,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.debug("Searching developers by term: {}", searchTerm);
        Page<DeveloperResponseDto> developers = developerService.searchDevelopers(searchTerm, pageable);
        return ResponseEntity.ok(developers);
    }
    
    @GetMapping("/by-skill")
    public ResponseEntity<List<DeveloperResponseDto>> getDevelopersBySkill(@RequestParam String skill) {
        log.debug("Fetching developers by skill: {}", skill);
        List<DeveloperResponseDto> developers = developerService.getDevelopersBySkill(skill);
        return ResponseEntity.ok(developers);
    }
    
    @GetMapping("/by-skills")
    public ResponseEntity<List<DeveloperResponseDto>> getDevelopersBySkills(@RequestParam List<String> skills) {
        log.debug("Fetching developers by skills: {}", skills);
        List<DeveloperResponseDto> developers = developerService.getDevelopersBySkills(skills);
        return ResponseEntity.ok(developers);
    }
    
    @GetMapping("/top-by-tasks")
    public ResponseEntity<List<DeveloperSummaryDto>> getTopDevelopersByTaskCount(
            @RequestParam(defaultValue = "5") int limit) {
        log.debug("Fetching top {} developers by task count", limit);
        List<DeveloperSummaryDto> developers = developerService.getTopDevelopersByTaskCount(limit);
        return ResponseEntity.ok(developers);
    }
    
    @GetMapping("/without-tasks")
    public ResponseEntity<List<DeveloperResponseDto>> getDevelopersWithoutTasks() {
        log.debug("Fetching developers without tasks");
        List<DeveloperResponseDto> developers = developerService.getDevelopersWithoutTasks();
        return ResponseEntity.ok(developers);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<DeveloperResponseDto>> getAvailableDevelopers(
            @RequestParam(defaultValue = "5") int maxTasks) {
        log.debug("Fetching available developers with less than {} tasks", maxTasks);
        List<DeveloperResponseDto> developers = developerService.getAvailableDevelopers(maxTasks);
        return ResponseEntity.ok(developers);
    }
    
    @PostMapping("/{developerId}/skills")
    public ResponseEntity<DeveloperResponseDto> addSkillToDeveloper(
            @PathVariable Long developerId,
            @RequestParam String skill,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Adding skill '{}' to developer with ID: {}", skill, developerId);
        DeveloperResponseDto developer = developerService.addSkillToDeveloper(developerId, skill, actorName);
        return ResponseEntity.ok(developer);
    }
    
    @DeleteMapping("/{developerId}/skills")
    public ResponseEntity<DeveloperResponseDto> removeSkillFromDeveloper(
            @PathVariable Long developerId,
            @RequestParam String skill,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Removing skill '{}' from developer with ID: {}", skill, developerId);
        DeveloperResponseDto developer = developerService.removeSkillFromDeveloper(developerId, skill, actorName);
        return ResponseEntity.ok(developer);
    }
    
    @PutMapping("/{developerId}/skills")
    public ResponseEntity<DeveloperResponseDto> updateDeveloperSkills(
            @PathVariable Long developerId,
            @RequestBody Set<String> skills,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Updating skills for developer with ID: {}", developerId);
        DeveloperResponseDto developer = developerService.updateDeveloperSkills(developerId, skills, actorName);
        return ResponseEntity.ok(developer);
    }
    
    @GetMapping("/statistics/tasks")
    public ResponseEntity<Map<String, Object>> getDeveloperTaskStatistics() {
        log.debug("Fetching developer task statistics");
        Map<String, Object> statistics = developerService.getDeveloperTaskStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/skills")
    public ResponseEntity<Map<String, Long>> getSkillStatistics() {
        log.debug("Fetching skill statistics");
        Map<String, Long> statistics = developerService.getSkillStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkDeveloperExists(@RequestParam String email) {
        log.debug("Checking if developer exists with email: {}", email);
        boolean exists = developerService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}