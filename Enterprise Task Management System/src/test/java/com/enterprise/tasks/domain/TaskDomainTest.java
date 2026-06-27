package com.enterprise.tasks.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class TaskDomainTest {
    private final User owner = new User("owner@example.com", "hash", Role.USER);

    @Test void newTaskStartsTodo() {
        assertEquals(TaskStatus.TODO, task().getStatus());
    }

    @Test void newTaskAssignsOwner() {
        assertEquals(owner.getId(), task().getOwner().getId());
    }

    @Test void newTaskAssignsOwnerAsInitialAssignee() {
        assertEquals(owner.getId(), task().getAssignee().getId());
    }

    @Test void updateChangesStatus() {
        Task task = task();
        task.update("Updated", "Description", TaskStatus.DONE, Priority.HIGH, null);
        assertEquals(TaskStatus.DONE, task.getStatus());
    }

    @Test void updateChangesPriority() {
        Task task = task();
        task.update("Updated", null, TaskStatus.BLOCKED, Priority.CRITICAL, null);
        assertEquals(Priority.CRITICAL, task.getPriority());
    }

    @Test void assignmentChangesAssignee() {
        Task task = task();
        User assignee = new User("assignee@example.com", "hash", Role.USER);
        task.assignTo(assignee);
        assertEquals(assignee.getId(), task.getAssignee().getId());
    }

    @Test void userEmailIsNormalized() {
        User user = new User("USER@EXAMPLE.COM", "hash", Role.USER);
        assertEquals("user@example.com", user.getEmail());
    }

    @Test void taskHasCreationTimestamp() {
        assertNotNull(task().getCreatedAt());
    }

    private Task task() {
        return new Task("Task", "Description", Priority.MEDIUM, owner, Instant.now());
    }
}

