package com.enterprise.tasks.api.dto;

import com.enterprise.tasks.domain.Priority;
import com.enterprise.tasks.domain.Task;
import com.enterprise.tasks.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class TaskDtos {
    private TaskDtos() {}

    public record CreateTaskRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        @NotNull Priority priority,
        Instant dueAt
    ) {}

    public record UpdateTaskRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        @NotNull TaskStatus status,
        @NotNull Priority priority,
        Instant dueAt,
        @NotNull Long version
    ) {}

    public record AssignTaskRequest(@NotNull UUID assigneeId) {}

    public record TaskResponse(
        UUID id, String title, String description, TaskStatus status, Priority priority,
        UUID ownerId, UUID assigneeId, Instant dueAt, long version,
        Instant createdAt, Instant updatedAt
    ) {
        public static TaskResponse from(Task task) {
            return new TaskResponse(
                task.getId(), task.getTitle(), task.getDescription(), task.getStatus(),
                task.getPriority(), task.getOwner().getId(),
                task.getAssignee() == null ? null : task.getAssignee().getId(),
                task.getDueAt(), task.getVersion(), task.getCreatedAt(), task.getUpdatedAt()
            );
        }
    }
}

