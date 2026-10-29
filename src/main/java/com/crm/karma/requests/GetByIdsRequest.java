package com.crm.karma.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class GetByIdsRequest {
    @Schema(description = "List of IDs of any table")
    private List<UUID> ids;
}
