package com.crm.karma.repositories;

import com.crm.karma.models.User;
import com.crm.karma.models.UserMember;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserMemberRepository extends JpaRepository<@NonNull UserMember, @NonNull UUID> {

  @Query(
    value = "SELECT DISTINCT member.* FROM user_members um " +
      "INNER JOIN users member ON member.id = um.member_id " +
      "WHERE um.user_id = :userId " +
      "AND slugify(member.name) LIKE slugify(CONCAT('%', :name, '%')) " +
      "ORDER BY member.name ASC",
    countQuery = "SELECT COUNT(DISTINCT um.member_id) FROM user_members um " +
      "INNER JOIN users member ON member.id = um.member_id " +
      "WHERE um.user_id = :userId " +
      "AND slugify(member.name) LIKE slugify(CONCAT('%', :name, '%'))",
    nativeQuery = true
  )
  Page<@NonNull User> searchDistinctMemberByUserIdContainingIgnoreCase(
    @Param("name") String name,
    @Param("userId") UUID userId,
    Pageable pageable
  );

  @Query(
    value = "SELECT DISTINCT member.* FROM user_members um " +
      "INNER JOIN users member ON member.id = um.member_id " +
      "WHERE um.user_id = :userId " +
      "ORDER BY member.name ASC",
    nativeQuery = true
  )
  List<@NonNull User> findDistinctMembersByUserId(@Param("userId") UUID userId);

  @Query(
    value = "SELECT DISTINCT member.* FROM user_members um " +
      "INNER JOIN users member ON member.id = um.member_id " +
      "WHERE um.user_id = :userId AND member.id IN (:memberIds) " +
      "ORDER BY member.name ASC",
    nativeQuery = true
  )
  List<@NonNull User> findDistinctMembersByUserIdAndIdIn(
    @Param("userId") UUID userId,
    @Param("memberIds") List<UUID> memberIds
  );

  Optional<UserMember> findByMemberIdAndUserId(UUID memberId, UUID userId);

  Optional<UserMember> findFirstByMemberId(UUID memberId);
}
