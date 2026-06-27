package com.enterprise.tasks.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    private UUID id;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(length = 5000)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;
    @Column(name = "due_at")
    private Instant dueAt;
    @Version
    private long version;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Task() {}

    public Task(String title, String description, Priority priority, User owner, Instant dueAt) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.status = TaskStatus.TODO;
        this.priority = priority;
        this.owner = owner;
        this.assignee = owner;
        this.dueAt = dueAt;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void update(String title, String description, TaskStatus status,
                       Priority priority, Instant dueAt) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueAt = dueAt;
        this.updatedAt = Instant.now();
    }

    public void assignTo(User user) {
        this.assignee = user;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public User getOwner() { return owner; }
    public User getAssignee() { return assignee; }
    public Instant getDueAt() { return dueAt; }
    public long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

