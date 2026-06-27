package com.enterprise.tasks.api;

import com.enterprise.tasks.api.dto.TaskDtos.*;
import com.enterprise.tasks.domain.TaskStatus;
import com.enterprise.tasks.service.TaskService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public Page<TaskResponse> list(Principal principal,
                                   @RequestParam(required = false) TaskStatus status,
                                   Pageable pageable) {
        return taskService.list(principal.getName(), status, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(Principal principal,
                               @Valid @RequestBody CreateTaskRequest request) {
        return taskService.create(principal.getName(), request);
    }

    @PutMapping("/{id}")
    public TaskResponse update(Principal principal, @PathVariable UUID id,
                               @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.update(principal.getName(), id, request);
    }

    @PostMapping("/{id}/assignee")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public TaskResponse assign(Principal principal, @PathVariable UUID id,
                               @Valid @RequestBody AssignTaskRequest request) {
        return taskService.assign(principal.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Principal principal, @PathVariable UUID id) {
        taskService.delete(principal.getName(), id);
    }
}

