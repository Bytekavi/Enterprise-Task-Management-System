package com.enterprise.tasks.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.enterprise.tasks.api.dto.TaskDtos.*;
import com.enterprise.tasks.domain.*;
import com.enterprise.tasks.repository.TaskRepository;
import com.enterprise.tasks.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskServiceSecurityTest {
    private TaskRepository tasks;
    private UserRepository users;
    private TaskService service;
    private User owner;

    @BeforeEach void setUp() {
        tasks = mock(TaskRepository.class);
        users = mock(UserRepository.class);
        EntityManager entityManager = mock(EntityManager.class);
        service = new TaskService(tasks, users, entityManager, false);
        owner = new User("owner@example.com", "hash", Role.USER);
        when(users.findByEmailIgnoreCase(owner.getEmail())).thenReturn(Optional.of(owner));
    }

    @Test void ownerCanCreateTask() {
        when(tasks.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TaskResponse result = service.create(owner.getEmail(),
            new CreateTaskRequest("Task", "Description", Priority.HIGH, Instant.now()));
        assertEquals(owner.getId(), result.ownerId());
    }

    @Test void nonOwnerCannotUpdateTask() {
        User other = new User("other@example.com", "hash", Role.USER);
        Task task = new Task("Task", null, Priority.MEDIUM, other, null);
        when(tasks.findByIdForUpdate(task.getId())).thenReturn(Optional.of(task));
        var request = new UpdateTaskRequest(
            "Changed", null, TaskStatus.DONE, Priority.HIGH, null, task.getVersion());
        assertThrows(ForbiddenException.class,
            () -> service.update(owner.getEmail(), task.getId(), request));
    }

    @Test void staleConcurrentUpdateIsRejected() {
        Task task = new Task("Task", null, Priority.MEDIUM, owner, null);
        when(tasks.findByIdForUpdate(task.getId())).thenReturn(Optional.of(task));
        var request = new UpdateTaskRequest(
            "Changed", null, TaskStatus.DONE, Priority.HIGH, null, 99L);
        assertThrows(OptimisticLockException.class,
            () -> service.update(owner.getEmail(), task.getId(), request));
    }

    @Test void normalUserCannotAssignTask() {
        Task task = new Task("Task", null, Priority.MEDIUM, owner, null);
        assertThrows(ForbiddenException.class,
            () -> service.assign(owner.getEmail(), task.getId(),
                new AssignTaskRequest(UUID.randomUUID())));
        verify(tasks, never()).findByIdForUpdate(any());
    }
}
