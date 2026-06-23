package com.crm.karma.repositories;

import com.crm.karma.models.Project;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<@NonNull Project, @NonNull UUID> {

  List<Project> findAllByUserIdAndActiveTrueAndDeletedAtIsNullOrderByNameAsc(UUID userId);

  List<Project> findAllByUserIdAndDeletedAtIsNullOrderByNameAsc(UUID userId);
}
