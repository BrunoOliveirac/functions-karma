package com.crm.karma.repositories;

import com.crm.karma.models.Client;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<@NonNull Client, @NonNull UUID> {

  List<Client> findAllByUserIdAndDeletedAtIsNullOrderByNameAsc(UUID userId);

  Optional<Client> findByUserIdAndEmailAndDeletedAtIsNull(UUID userId, String email);
}
