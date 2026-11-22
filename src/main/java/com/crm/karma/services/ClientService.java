package com.crm.karma.services;

import com.crm.karma.models.Client;
import com.crm.karma.models.Sector;
import com.crm.karma.repositories.ClientRepository;
import com.crm.karma.requests.CheckEmailRequest;
import com.crm.karma.requests.UpsertClientRequest;
import com.crm.karma.utils.NormalizerImage;
import com.crm.karma.validations.Base64JpegValidator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientService {

  private final SectorService sectorService;
  private final ClientRepository clientRepository;

  public ClientService(
      SectorService sectorService,
      ClientRepository clientRepository
  ) {
    this.sectorService = sectorService;
    this.clientRepository = clientRepository;
  }

  public List<Client> getAll(UUID userId) {
    return clientRepository.findAllByUserIdAndDeletedAtIsNullOrderByNameAsc(userId);
  }

  public Optional<Client> getById(UUID clientId) {
    return clientRepository.findById(clientId);
  }

  public List<Client> getByIds(List<UUID> clientIds) {
    return clientRepository.findAllById(clientIds);
  }

  public UUID save(UpsertClientRequest upsertClient) {
    Base64JpegValidator.decodeAndValidateJpeg(upsertClient.getAvatar());
    Base64JpegValidator.decodeAndValidateJpeg(upsertClient.getCover());
    Client client = new Client();

    if (upsertClient.getId() != null) {
      client = getById(upsertClient.getId())
          .orElseThrow(() -> new RuntimeException("Client not found!"));
    }

    client.setBudget(upsertClient.getBudget());
    client.setEmail(upsertClient.getEmail());
    client.setName(upsertClient.getName());
    client.setNotes(upsertClient.getNotes());
    client.setPhone(upsertClient.getPhone());
    client.setUserId(upsertClient.getUserId());

    if (upsertClient.getAvatar() != null) {
      client.setAvatar(NormalizerImage.normalizeImage(upsertClient.getAvatar()));
    }

    if (upsertClient.getCover() != null) {
      client.setCover(NormalizerImage.normalizeImage(upsertClient.getCover()));
    }

    if (upsertClient.getSectorId() != null) {
      Sector sector = sectorService.getById(upsertClient.getSectorId())
          .orElseThrow(() -> new RuntimeException("Sector not found!"));

      client.setSector(sector);
    }

    Client newClient = clientRepository.save(client);
    return newClient.getId();
  }

  public void toggleFavorite(
      UUID clientId,
      Boolean favorite
  ) {
    Client client = getById(clientId)
        .orElseThrow(() -> new RuntimeException("Client not found!"));

    client.setFavorite(favorite);
    clientRepository.save(client);
  }

  public void delete(UUID clientId) {
    Client client = clientRepository.findById(clientId).orElseThrow(() -> new RuntimeException(("Client not found!")));

    client.setDeletedAt(Instant.now());
    clientRepository.save(client);
  }

  public Boolean checkEmail(CheckEmailRequest checkEmailRequest) {
    var client = clientRepository.findByUserIdAndEmailAndDeletedAtIsNull(
        checkEmailRequest.getUserId(),
        checkEmailRequest.getEmail()
    );

    return client.isEmpty();
  }
}
