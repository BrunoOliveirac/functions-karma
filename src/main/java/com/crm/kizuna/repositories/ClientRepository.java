package com.crm.kizuna.repositories;

import com.crm.kizuna.models.Client;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<@NonNull Client, @NonNull UUID> {

  List<Client> findByUserId(UUID userId);

  Optional<Client> findByUserIdAndEmail(UUID userId, String email);
}
