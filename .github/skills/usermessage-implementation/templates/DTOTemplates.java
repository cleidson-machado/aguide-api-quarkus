// Template for Request DTO (used for POST/PUT endpoints)
// Replace [Resource] with actual resource name

package br.com.aguideptbr.features.usermessage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class Create[Resource]Request
{

    // ✅ PRIVATE FIELDS (never public)
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    // ✅ DEFAULT CONSTRUCTOR (required by Jackson)
    public Create[Resource]Request() {
    }

    // ✅ CONSTRUCTOR WITH FIELDS (optional, for testing)
    public Create[Resource]Request(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // ✅ GETTERS AND SETTERS (required for Jackson and Bean Validation)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }}

    // -------------------------------------------------------------------

    // Template for Response DTO (used for GET endpoints)
    // Replace [Resource] with actual resource name

    package br.com.aguideptbr.features.usermessage.dto;

import java.time.LocalDateTime;
    import java.util.UUID;

    public class[Resource]Response{

    // ✅ PRIVATE FIELDS (never public)
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ CONSTRUCTOR (for easy mapping from entity)
    public [Resource]Response(UUID id, String name, String description,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ✅ GETTERS ONLY (response DTOs are typically immutable)
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

// -------------------------------------------------------------------

// Template for Nested DTO (used inside other DTOs)
// Example: UserInfo inside MessageResponse

package br.com.aguideptbr.features.usermessage.dto;

import java.util.UUID;

public class UserInfo {

    private UUID id;
    private String name;
    private String fullName;

    public UserInfo(UUID id, String name, String fullName) {
        this.id = id;
        this.name = name;
        this.fullName = fullName;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }
}
