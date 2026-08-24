package com.crm.karma.services;

import com.crm.karma.models.User;
import com.crm.karma.models.UserMember;
import com.crm.karma.repositories.UserMemberRepository;
import com.crm.karma.repositories.UserRepository;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
