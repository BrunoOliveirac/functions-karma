package com.crm.karma.responses;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status returned in requests0")
public record StatusResponse(String status) {
}
