package com.crm.karma.repositories;

import com.crm.karma.models.ProjectMember;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<@NonNull ProjectMember, @NonNull UUID> {
}
