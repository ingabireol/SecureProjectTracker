package com.buildmaster.projecttracker.model;

import com.buildmaster.projecttracker.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "developers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Developer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Developer name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Column(nullable = false, length = 50)
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "developer_skills", joinColumns = @JoinColumn(name = "developer_id"))
    @Column(name = "skill")
    private Set<String> skills = new HashSet<>();
    
    @OneToMany(mappedBy = "assignedDeveloper", fetch = FetchType.LAZY)
    private List<Task> assignedTasks = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void addSkill(String skill) {
        if (skill != null && !skill.trim().isEmpty()) {
            this.skills.add(skill.trim().toLowerCase());
        }
    }
    
    public void removeSkill(String skill) {
        if (skill != null) {
            this.skills.remove(skill.trim().toLowerCase());
        }
    }
    
    public int getTaskCount() {
        return assignedTasks != null ? assignedTasks.size() : 0;
    }
}