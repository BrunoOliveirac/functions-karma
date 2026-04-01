package com.crm.kizuna.services;

import com.crm.kizuna.models.Client;
import com.crm.kizuna.repositories.ClientRepository;
import com.crm.kizuna.requests.CheckEmailRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientService {

  private final ClientRepository clientRepository;

  public ClientService(ClientRepository clientRepository) {
    this.clientRepository = clientRepository;
  }

  public List<Client> getAll(UUID userId) {
    return clientRepository.findByUserId(userId);
  }

  public Optional<Client> getById(UUID clientId) {
    return clientRepository.findById(clientId);
  }

  public UUID save(Client client) {
    Client newClient = clientRepository.save(client);
    return newClient.getId();
  }

  public void toggleFavorite(UUID clientId, Boolean favorite) {
    Client client = getById(clientId)
      .orElseThrow(() -> new RuntimeException("Client not found!"));

    client.setFavorite(favorite);
    clientRepository.save(client);
  }

  public void delete(UUID clientId) {
    clientRepository.deleteById(clientId);
  }

  public Boolean checkEmail(CheckEmailRequest checkEmailRequest) {
    var client = clientRepository.findByUserIdAndEmail(
      checkEmailRequest.getUserId(),
      checkEmailRequest.getEmail()
    );

    return client.isEmpty();
  }
}
