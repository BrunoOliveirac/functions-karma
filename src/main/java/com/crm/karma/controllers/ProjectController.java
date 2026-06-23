package com.crm.karma.controllers;

import com.crm.karma.models.Project;
import com.crm.karma.requests.UpsertProjectRequest;
import com.crm.karma.responses.StatusResponse;
import com.crm.karma.services.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@PreAuthorize("hasRole('USER')")
@Tag(name = "Projects")
public class ProjectController {

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  /**
   * List all projects of a user account
   *
   * @param userId User ID
   * @return A project list or an empty list
   */
  @Operation(summary = "List all projects of an account")
  @GetMapping("/list/{userId}")
  public List<Project> listProjects(@PathVariable UUID userId) {
    return projectService.getAll(userId);
  }

  /**
   * List all active projects of a user account
   *
   * @param userId User ID
   * @return A project list with only active items or an empty list
   */
  @Operation(summary = "List all active projects of an account")
  @GetMapping("/list-actives/{userId}")
  public List<Project> listActiveProjects(@PathVariable UUID userId) {
    return projectService.getAllActive(userId);
  }

  /**
   * Get the project to show your details
   *
   * @param projectId Project ID of project to be retrieved
   * @return The project or null as the result of the query
   */
  @Operation(summary = "Get a specific project")
  @GetMapping("/{projectId}")
  public Optional<Project> getProject(@PathVariable UUID projectId) {
    return projectService.getById(projectId);
  }

  /**
   * Create or Update a project, based on UUID type of ID attribute
   *
   * @param project The project to be created or updated
   * @return Project ID created or updated
   */
  @Operation(summary = "Create or edit a project")
  @PostMapping("/upsert")
  public UUID upsertProject(@RequestBody UpsertProjectRequest project) {
    return projectService.save(project);
  }

  /**
   * Update the active status of a project
   *
   * @param projectId Project ID
   * @return OK status when updating a project active status is successful
   */
  @Operation(summary = "Toggle active status of a specific project")
  @PatchMapping("/toggle-active/{projectId}")
  public StatusResponse toggleActiveProject(@PathVariable UUID projectId) {
    projectService.toggleActive(projectId);
    return new StatusResponse("OK");
  }

  /**
   * Delete the project from the queries, adding deleted_at
   *
   * @param projectId Project to be deleted
   * @return OK status when deletion a project is successful
   */
  @Operation(summary = "Delete logically a specific project")
  @DeleteMapping("/delete/{projectId}")
  public StatusResponse deleteProject(@PathVariable UUID projectId) {
    projectService.delete(projectId);
    return new StatusResponse("OK");
  }
}
