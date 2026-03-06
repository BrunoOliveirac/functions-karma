package com.crm.kizuna.services;

import com.crm.kizuna.models.User;
import com.crm.kizuna.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User getByEmail(String email) {
    return userRepository.findByEmail(email).orElse(null);
  }

  public User add(User user) {
    user.setActive(true);
    return userRepository.save(user);
  }

}
