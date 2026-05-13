package com.crm.karma.repositories;

import com.crm.karma.models.Credential;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CredentialRepository extends JpaRepository<@NonNull Credential, @NonNull UUID> {

  Optional<Credential> getByUserId(UUID userId);
}
