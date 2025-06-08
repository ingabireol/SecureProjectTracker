package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.developer.DeveloperDetailResponseDto;
import com.buildmaster.projecttracker.dto.developer.DeveloperRequestDto;
import com.buildmaster.projecttracker.dto.developer.DeveloperResponseDto;
import com.buildmaster.projecttracker.dto.developer.DeveloperSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DeveloperService {
    
    /**
     * Create a new developer
     */
    DeveloperResponseDto createDeveloper(DeveloperRequestDto developerRequestDto, String actorName);
    
    /**
     * Update an existing developer
     */
    DeveloperResponseDto updateDeveloper(Long developerId, DeveloperRequestDto developerRequestDto, String actorName);
    
    /**
     * Delete a developer (unassign from all tasks first)
     */
    void deleteDeveloper(Long developerId, String actorName);
    
    /**
     * Get developer by ID
     */
    DeveloperResponseDto getDeveloperById(Long developerId);
    
    /**
     * Get detailed developer information including assigned tasks
     */
    DeveloperDetailResponseDto getDeveloperDetails(Long developerId);
    
    /**
     * Get all developers with pagination and sorting
     */
    Page<DeveloperResponseDto> getAllDevelopers(Pageable pageable);
    
    /**
     * Search developers by name or email
     */
    Page<DeveloperResponseDto> searchDevelopers(String searchTerm, Pageable pageable);
    
    /**
     * Get developers by skill
     */
    List<DeveloperResponseDto> getDevelopersBySkill(String skill);
    
    /**
     * Get developers with any of the specified skills
     */
    List<DeveloperResponseDto> getDevelopersBySkills(List<String> skills);
    
    /**
     * Get top developers by task count
     */
    List<DeveloperSummaryDto> getTopDevelopersByTaskCount(int limit);
    
    /**
     * Get developers without any assigned tasks
     */
    List<DeveloperResponseDto> getDevelopersWithoutTasks();
    
    /**
     * Get available developers (with task count less than specified limit)
     */
    List<DeveloperResponseDto> getAvailableDevelopers(int maxTasks);
    
    /**
     * Add skill to developer
     */
    DeveloperResponseDto addSkillToDeveloper(Long developerId, String skill, String actorName);
    
    /**
     * Remove skill from developer
     */
    DeveloperResponseDto removeSkillFromDeveloper(Long developerId, String skill, String actorName);
    
    /**
     * Update developer skills (replace all)
     */
    DeveloperResponseDto updateDeveloperSkills(Long developerId, Set<String> skills, String actorName);
    
    /**
     * Get developer task statistics
     */
    Map<String, Object> getDeveloperTaskStatistics();
    
    /**
     * Check if developer exists by email
     */
    boolean existsByEmail(String email);
    
    /**
     * Get skill usage statistics
     */
    Map<String, Long> getSkillStatistics();
}