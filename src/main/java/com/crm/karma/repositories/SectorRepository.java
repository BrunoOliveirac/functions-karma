package com.crm.karma.repositories;

import com.crm.karma.models.Sector;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SectorRepository extends JpaRepository<@NonNull Sector, @NonNull UUID> {

  List<Sector> findAllByUserIdAndActiveTrueAndDeletedAtIsNull(UUID userId);

  List<Sector> findAllByUserIdAndDeletedAtIsNull(UUID userId);
}
