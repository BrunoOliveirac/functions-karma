package com.crm.karma.services;

import com.crm.karma.models.User;
import com.crm.karma.models.UserMember;
import com.crm.karma.repositories.UserMemberRepository;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserMemberService {

  private final UserMemberRepository userMemberRepository;

  public UserMemberService(UserMemberRepository userMemberRepository) {
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
      .findDistinctMemberByUserIdContainingIgnoreCase(
        query,
        userId,
        pageable
      );
  }

  /**
   * Search a member to know his is already linked to the user or not
   *
   * @param memberId ID of the member
   * @param userId User ID creating the member
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

    userMemberRepository.delete(userMember);
  }
}
