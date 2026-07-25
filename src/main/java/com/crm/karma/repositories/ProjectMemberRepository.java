package com.crm.karma.repositories;

import com.crm.karma.models.ProjectMember;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<
  @NonNull ProjectMember,
  @NonNull UUID
  > {

  @Query("SELECT projectMember.project.Id FROM ProjectMember projectMember WHERE projectMember.member.id = :memberId")
  List<UUID> findByMemberId(@Param("memberId") UUID memberId);

  Optional<ProjectMember> findFirstByMemberId(UUID memberId);

  @Modifying
  @Query(
    "DELETE FROM ProjectMember projectMember " +
    "WHERE projectMember.member.id = :memberId AND projectMember.project.id IN :projectIds"
  )
  void deleteByMemberIdAndProjectIdIn(
    @Param("memberId") UUID memberId,
    @Param("projectIds") List<UUID> projectIds
  );

}
