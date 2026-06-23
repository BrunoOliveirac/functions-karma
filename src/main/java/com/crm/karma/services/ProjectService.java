package com.crm.karma.services;

import com.crm.karma.models.Client;
import com.crm.karma.models.Project;
import com.crm.karma.repositories.ProjectRepository;
import com.crm.karma.requests.UpsertProjectRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectService {

  private final ClientService clientService;
  private final ProjectRepository projectRepository;

  public ProjectService(ProjectRepository projectRepository, ClientService clientService) {
    this.projectRepository = projectRepository;
    this.clientService = clientService;
  }

  public List<Project> getAll(UUID userId) {
    return projectRepository.findAllByUserIdAndDeletedAtIsNullOrderByNameAsc(userId);
  }

  public List<Project> getAllActive(UUID userId) {
    return projectRepository.findAllByUserIdAndActiveTrueAndDeletedAtIsNullOrderByNameAsc(userId);
  }

  public Optional<Project> getById(UUID projectId) {
    return projectRepository.findById(projectId);
  }

  public UUID save(UpsertProjectRequest upsertProject) {
    if (upsertProject.getClientId() == null) {
      throw new RuntimeException("Client ID is required!");
    }

    Project project = new Project();

    if (upsertProject.getId() != null) {
      project = getById(upsertProject.getId())
        .orElseThrow(() -> new RuntimeException("Project not found!"));
    }

    Client client = clientService.getById(upsertProject.getClientId())
      .orElseThrow(() -> new RuntimeException("Client not found!"));

    project.setName(upsertProject.getName());
    project.setUserId(upsertProject.getUserId());
    project.setClient(client);

    Project newProject = projectRepository.save(project);
    return newProject.getId();
  }

  public void toggleActive(UUID projectId) {
    Project project = getById(projectId)
      .orElseThrow(() -> new RuntimeException("Project not found!"));

    project.toggleStatus();
    projectRepository.save(project);
  }

  public void delete(UUID projectId) {
    Project project = projectRepository.findById(projectId)
      .orElseThrow(() -> new RuntimeException("Project not found!"));

    project.setDeletedAt(Instant.now());
    projectRepository.save(project);
  }

}
