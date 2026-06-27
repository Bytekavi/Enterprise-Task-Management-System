package com.enterprise.tasks.service;

import com.enterprise.tasks.api.dto.TaskDtos.*;
import com.enterprise.tasks.domain.*;
import com.enterprise.tasks.repository.TaskRepository;
import com.enterprise.tasks.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
    private final TaskRepository tasks;
    private final UserRepository users;
    private final EntityManager entityManager;

    private final boolean rlsEnabled;

    public TaskService(TaskRepository tasks, UserRepository users, EntityManager entityManager,
                       @Value("${database.rls-enabled:true}") boolean rlsEnabled) {
        this.tasks = tasks;
        this.users = users;
        this.entityManager = entityManager;
        this.rlsEnabled = rlsEnabled;
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> list(String email, TaskStatus status, Pageable pageable) {
        User user = requireUser(email);
        applyDatabaseContext(user);
        return tasks.findVisibleTasks(user.getId(), status, pageable).map(TaskResponse::from);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TaskResponse create(String email, CreateTaskRequest request) {
        User user = requireUser(email);
        applyDatabaseContext(user);
        Task task = new Task(request.title(), request.description(), request.priority(),
            user, request.dueAt());
        return TaskResponse.from(tasks.save(task));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TaskResponse update(String email, UUID id, UpdateTaskRequest request) {
        User user = requireUser(email);
        applyDatabaseContext(user);
        Task task = tasks.findByIdForUpdate(id)
            .orElseThrow(() -> new NotFoundException("Task not found"));
        ensureOwnerOrAdmin(user, task);
        if (task.getVersion() != request.version()) {
            throw new OptimisticLockException("Task was modified by another transaction");
        }
        task.update(request.title(), request.description(), request.status(),
            request.priority(), request.dueAt());
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse assign(String email, UUID id, AssignTaskRequest request) {
        User actor = requireUser(email);
        applyDatabaseContext(actor);
        if (actor.getRole() == Role.USER) {
            throw new ForbiddenException("Manager or admin role required");
        }
        Task task = tasks.findByIdForUpdate(id)
            .orElseThrow(() -> new NotFoundException("Task not found"));
        User assignee = users.findById(request.assigneeId())
            .orElseThrow(() -> new NotFoundException("Assignee not found"));
        task.assignTo(assignee);
        return TaskResponse.from(task);
    }

    @Transactional
    public void delete(String email, UUID id) {
        User actor = requireUser(email);
        applyDatabaseContext(actor);
        Task task = tasks.findByIdForUpdate(id)
            .orElseThrow(() -> new NotFoundException("Task not found"));
        ensureOwnerOrAdmin(actor, task);
        tasks.delete(task);
    }

    private User requireUser(String email) {
        return users.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void ensureOwnerOrAdmin(User actor, Task task) {
        if (!task.getOwner().getId().equals(actor.getId()) && actor.getRole() != Role.ADMIN) {
            throw new ForbiddenException("You cannot modify this task");
        }
    }

    private void applyDatabaseContext(User user) {
        if (rlsEnabled) {
            entityManager.createNativeQuery("select set_config('app.current_user_id', :id, true)")
                .setParameter("id", user.getId().toString()).getSingleResult();
            entityManager.createNativeQuery("select set_config('app.is_admin', :admin, true)")
                .setParameter("admin", Boolean.toString(user.getRole() == Role.ADMIN))
                .getSingleResult();
        }
    }
}
