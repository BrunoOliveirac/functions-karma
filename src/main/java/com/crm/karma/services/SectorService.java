package com.crm.karma.services;

import com.crm.karma.models.Sector;
import com.crm.karma.repositories.SectorRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SectorService {

  private final SectorRepository sectorRepository;

  public SectorService(SectorRepository sectorRepository) {
    this.sectorRepository = sectorRepository;
  }

  public List<Sector> getAll(UUID userId) {
    return sectorRepository.findAllByUserIdAndDeletedAtIsNull(userId);
  }

  public List<Sector> getAllActive(UUID userId) {
    return sectorRepository.findAllByUserIdAndActiveTrueAndDeletedAtIsNull(userId);
  }

  public Optional<Sector> getById(UUID sectorId) {
    return sectorRepository.findById(sectorId);
  }

  public UUID save(Sector sector) {
    Sector newSector = sectorRepository.save(sector);
    return newSector.getId();
  }

  public void delete(UUID sectorId) {
    Sector sector = sectorRepository.findById(sectorId).orElseThrow(() -> new RuntimeException("Sector not found!"));

    sector.setDeletedAt(Instant.now());
    sectorRepository.save(sector);
  }

}
