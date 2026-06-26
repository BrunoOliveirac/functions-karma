package com.crm.karma.services;

import com.crm.karma.models.User;
import com.crm.karma.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User getByEmail(String email) {
    return userRepository.findByEmailAndDeletedAtIsNull(email).orElse(null);
  }

  public User add(User user) {
    user.setActive(true);
    return userRepository.save(user);
  }

  public User save(User user) {
    return userRepository.save(user);
  }

}
