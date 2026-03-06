package com.crm.kizuna.services;

import com.crm.kizuna.models.Credential;
import com.crm.kizuna.repositories.CredentialRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CredentialService {

  private final CredentialRepository credentialRepository;

  public CredentialService(CredentialRepository credentialRepository) {
    this.credentialRepository = credentialRepository;
  }

  public Credential getByUserId(UUID userId) {
    return credentialRepository.getByUserId(userId).orElse(null);
  }

  public void add(String hash, UUID userId) {
    Credential credential = new Credential(hash, userId);
    credentialRepository.save(credential);
  }

  public void delete(UUID id) {
    credentialRepository.deleteById(id);
  }
}
