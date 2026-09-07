package com.crm.karma.services;

import com.crm.karma.enums.UserType;
import com.crm.karma.models.*;
import com.crm.karma.repositories.ProjectMemberRepository;
import com.crm.karma.repositories.ProjectRepository;
import com.crm.karma.repositories.UserMemberRepository;
import com.crm.karma.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class ProjectMemberService {

  private final UserRepository userRepository;
  private final ProjectRepository projectRepository;
  private final NotificationService notificationService;
  private final UserMemberRepository userMemberRepository;
  private final ProjectMemberRepository projectMemberRepository;

  public ProjectMemberService(
    UserRepository userRepository,
    ProjectRepository projectRepository,
    NotificationService notificationService,
    UserMemberRepository userMemberRepository,
    ProjectMemberRepository projectMemberRepository
  ) {
    this.userRepository = userRepository;
    this.projectRepository = projectRepository;
    this.notificationService = notificationService;
    this.userMemberRepository = userMemberRepository;
    this.projectMemberRepository = projectMemberRepository;
  }

  public List<UUID> findByMemberId(UUID memberId) {
    return projectMemberRepository.findByMemberId(memberId);
  }

  @Transactional
  public void saveAll(List<UUID> projectIds, User user, User member) {
    if (projectIds.isEmpty()) return;

    List<Project> projects = projectRepository.findAllById(projectIds);
    List<ProjectMember> projectMembers = new ArrayList<>();
    List<Notification> notifications = new ArrayList<>();

    for (Project project : projects) {
      projectMembers.add(
        ProjectMember
          .builder()
          .user(user)
          .member(member)
          .project(project)
          .build()
      );

      notifications.add(
        Notification
          .builder()
          .code("linked_to_project")
          .type("project")
          .referenceLabel(project.getName())
          .referenceId(project.getId())
          .actor(user)
          .user(member)
          .build()
      );
    }

    notificationService.saveAll(notifications);
    projectMemberRepository.saveAll(projectMembers);
  }

  @Transactional
  public void managementProjectMembers(
    UUID memberId,
    List<UUID> projectIds,
    List<UUID> initialProjectIds
  ) {
    Set<UUID> selectedProjectIds = new HashSet<>(projectIds != null ? projectIds : List.of());
    Set<UUID> initialIds = new HashSet<>(initialProjectIds != null ? initialProjectIds : List.of());

    List<UUID> projectIdsToCreate = selectedProjectIds
      .stream()
      .filter(id -> !initialIds.contains(id))
      .toList();

    List<UUID> projectIdsToDelete = initialIds
      .stream()
      .filter(id -> !selectedProjectIds.contains(id))
      .toList();

    if (!projectIdsToDelete.isEmpty()) {
      projectMemberRepository.deleteByMemberIdAndProjectIdIn(memberId, projectIdsToDelete);
    }

    if (!projectIdsToCreate.isEmpty()) {
      User member = userRepository
        .findByIdAndTypeAndDeletedAtIsNull(memberId, UserType.MEMBER)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found!"));

      User user = projectMemberRepository
        .findFirstByMemberId(memberId)
        .map(ProjectMember::getUser)
        .orElseGet(() ->
          userMemberRepository
            .findFirstByMemberId(memberId)
            .map(UserMember::getUser)
            .orElseThrow(() ->
              new ResponseStatusException(HttpStatus.NOT_FOUND, "Member link not found!")
            )
        );

      saveAll(projectIdsToCreate, user, member);
    }
  }
}
