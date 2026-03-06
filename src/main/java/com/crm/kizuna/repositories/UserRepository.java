package com.crm.kizuna.repositories;

import com.crm.kizuna.models.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<@NonNull User, @NonNull UUID> {

  List<User> findByActive(Boolean active);

  Optional<User> findByEmail(String email);
}
