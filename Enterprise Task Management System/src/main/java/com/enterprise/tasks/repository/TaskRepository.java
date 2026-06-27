package com.enterprise.tasks.repository;

import com.enterprise.tasks.domain.Task;
import com.enterprise.tasks.domain.TaskStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    @Query("""
        select t from Task t
        where (t.owner.id = :userId or t.assignee.id = :userId)
          and (:status is null or t.status = :status)
        """)
    Page<Task> findVisibleTasks(@Param("userId") UUID userId,
                                @Param("status") TaskStatus status,
                                Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Task t where t.id = :id")
    Optional<Task> findByIdForUpdate(@Param("id") UUID id);
}

