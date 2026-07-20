package com.crm.karma.services;

import com.crm.karma.models.Project;
import com.crm.karma.models.ProjectMember;
import com.crm.karma.models.User;
import com.crm.karma.repositories.ProjectMemberRepository;
import com.crm.karma.repositories.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectMemberService {

  private final ProjectRepository projectRepository;
  private final ProjectMemberRepository projectMemberRepository;

  public ProjectMemberService(
    ProjectRepository projectRepository,
    ProjectMemberRepository projectMemberRepository
  ) {
    this.projectRepository = projectRepository;
    this.projectMemberRepository = projectMemberRepository;
  }

  public void saveAll(List<UUID> projectIds, User user, User member) {
    if (projectIds.isEmpty()) return;

    List<Project> projects = projectRepository.findAllById(projectIds);
    List<ProjectMember> projectMembers = new ArrayList<>();

    for (Project project : projects) {
      projectMembers.add(
        ProjectMember
          .builder()
          .user(user)
          .member(member)
          .project(project)
          .build()
      );
    }

    projectMemberRepository.saveAll(projectMembers);
  }
}
