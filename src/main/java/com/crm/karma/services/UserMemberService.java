package com.crm.karma.services;

import com.crm.karma.models.User;
import com.crm.karma.models.UserMember;
import com.crm.karma.repositories.UserMemberRepository;
import com.crm.karma.repositories.UserRepository;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserMemberService {

  private final UserRepository userRepository;
  private final NotificationService notificationService;
  private final UserMemberRepository userMemberRepository;

  public UserMemberService(
    UserRepository userRepository,
    NotificationService notificationService,
    UserMemberRepository userMemberRepository
  ) {
    this.userRepository = userRepository;
    this.notificationService = notificationService;
    this.userMemberRepository = userMemberRepository;
  }

  /**
   * Search paginated and filtered linked members of a user
   *
   * @param query    Provided term for filter member
   * @param userId   Logged user ID
   * @param pageable Sort and pagination information
   * @return An object with totalPages and the members
   */
  public Page<@NonNull User> getAllLinkedMembers(
    String query,
    UUID userId,
    Pageable pageable
  ) {
    return userMemberRepository
      .searchDistinctMemberByUserIdContainingIgnoreCase(
        query,
        userId,
        pageable
      );
  }

  /**
   * List every member linked to a user, ordered by name.
   *
   * @param userId Logged user ID
   * @return Linked members, or an empty list
   */
  public List<@NonNull User> listAllLinkedMembers(UUID userId) {
    return userMemberRepository.findDistinctMembersByUserId(userId);
  }

  /**
   * List linked members of a user whose ids were selected for export.
   *
   * @param userId    Logged user ID
   * @param memberIds Selected member IDs
   * @return Matching linked members, ordered by name
   */
  public List<@NonNull User> listLinkedMembersByIds(UUID userId, List<UUID> memberIds) {
    if (memberIds == null || memberIds.isEmpty()) {
      return List.of();
    }

    return userMemberRepository.findDistinctMembersByUserIdAndIdIn(userId, memberIds);
  }

  /**
   * Search a member to know his is already linked to the user or not
   *
   * @param memberId ID of the member
   * @param userId   User ID creating the member
   * @return Return null or a member
   */
  public Optional<UserMember> findByMemberIdAndUserId(UUID memberId, UUID userId) {
    return userMemberRepository.findByMemberIdAndUserId(memberId, userId);
  }

  public UserMember add(User member, User user) {
    UserMember userMember = UserMember.builder().member(member).user(user).build();
    return userMemberRepository.save(userMember);
  }

  /**
   * Unlink a member from a user
   *
   * @param memberId ID of the member
   * @param userId   User ID
   */
  public void unlink(UUID memberId, UUID userId) {
    UserMember userMember = userMemberRepository
      .findByMemberIdAndUserId(memberId, userId)
      .orElseThrow(() -> new RuntimeException("UserMember not found!"));

    User member = userRepository
      .findById(memberId)
      .orElseThrow(() -> new RuntimeException("Member not found!"));

    User user = userRepository
      .findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found!"));

    notificationService.add(
      "unlinked_by_user",
      "user",
      user,
      member,
      user.getId(),
      user.getName()
    );

    userMemberRepository.delete(userMember);

  }
}
