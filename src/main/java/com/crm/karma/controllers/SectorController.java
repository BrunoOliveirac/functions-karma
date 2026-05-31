package com.crm.karma.controllers;

import com.crm.karma.models.Sector;
import com.crm.karma.responses.StatusResponse;
import com.crm.karma.services.SectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/sectors")
@PreAuthorize("hasRole('USER')")
@Tag(name = "Sectors")
public class SectorController {

  private final SectorService sectorService;

  public SectorController(SectorService sectorService) {
    this.sectorService = sectorService;
  }

  /**
   * List all sectors of a user account
   *
   * @param userId User ID
   * @return A sector list or an empty list
   */
  @Operation(summary = "List all sectors of an account")
  @GetMapping("/list/{userId}")
  public List<Sector> listSectors(@PathVariable UUID userId) {
    return sectorService.getAll(userId);
  }

  /**
   * List all active sectors of a user account
   *
   * @param userId User ID
   * @return A sector list with only active items or an empty list
   */
  @Operation(summary = "List all active sectors of an account")
  @GetMapping("/list-actives/{userId}")
  public List<Sector> listActiveSectors(@PathVariable UUID userId) {
    return sectorService.getAllActive(userId);
  }

  /**
   * Get the sector to show your details
   *
   * @param sectorId Sector ID of sector to be g
   * @return The sector or null as the result of the query
   */
  @Operation(summary = "Get a specific sector")
  @GetMapping("/{sectorId}")
  public Optional<Sector> getSector(@PathVariable UUID sectorId) {
    return sectorService.getById(sectorId);
  }

  /**
   * Create or Update a sector, based on UUID type of ID attribute
   *
   * @param sector The sector to be created or updated
   * @return Sector ID created or updated
   */
  @Operation(summary = "Create or edit a sector")
  @PostMapping("/upsert")
  public UUID upsertSector(@RequestBody Sector sector) {
    return sectorService.save(sector);
  }

  /**
   * Delete the sector from the queries, adding deleted_at
   *
   * @param sector Sector to be deleted
   * @return OK status when deletion a sector is successful
   */
  @Operation(summary = "Delete logically a specific sector")
  @DeleteMapping("/delete")
  public StatusResponse deleteSector(@RequestBody Sector sector) {
    sectorService.delete(sector);
    return new StatusResponse("OK");
  }
}
