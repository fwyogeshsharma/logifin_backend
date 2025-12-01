package com.logifin.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDTO {

    private Long id;

    @NotBlank(message = "Role name is required")
    @Size(max = 50, message = "Role name must not exceed 50 characters")
    private String roleName;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
