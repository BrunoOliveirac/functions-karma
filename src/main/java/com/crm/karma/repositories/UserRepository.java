package com.crm.karma.repositories;

import com.crm.karma.enums.UserType;
import com.crm.karma.models.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<@NonNull User, @NonNull UUID> {

  Optional<User> findByEmail(String email);

  Optional<User> findByEmailAndIdNot(String email, UUID id);

  List<User> findAllByTypeAndDeletedAtIsNullOrderByNameAsc(UserType type);

  Optional<User> findByIdAndTypeAndDeletedAtIsNull(UUID id, UserType type);
}
