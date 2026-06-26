package com.crm.karma.repositories;

import com.crm.karma.enums.UserType;
import com.crm.karma.models.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<@NonNull User, @NonNull UUID> {

  Optional<User> findByEmailAndDeletedAtIsNull(String email);

  List<User> findAllByTypeAndDeletedAtIsNullOrderByNameAsc(UserType type);

  Optional<User> findByIdAndTypeAndDeletedAtIsNull(UUID id, UserType type);
}
