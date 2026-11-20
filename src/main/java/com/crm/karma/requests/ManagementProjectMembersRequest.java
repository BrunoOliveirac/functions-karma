package com.crm.karma.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class ManagementProjectMembersRequest {
  @Schema(description = "Project IDs before update / on init manage projects dialog")
  private List<UUID> initialProjectIds;

  @Schema(description = "Selected project IDs")
  private List<UUID> projectIds;
}
