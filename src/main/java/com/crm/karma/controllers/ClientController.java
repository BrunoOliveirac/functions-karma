package com.crm.karma.controllers;

import com.crm.karma.models.Client;
import com.crm.karma.requests.CheckEmailRequest;
import com.crm.karma.requests.UpsertClientRequest;
import com.crm.karma.responses.StatusResponse;
import com.crm.karma.services.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/clients")
@PreAuthorize("hasRole('USER')")
@Tag(name = "Clients")
public class ClientController {

  private final ClientService clientService;

  public ClientController(ClientService clientService) {
    this.clientService = clientService;
  }

  /**
   * List all clients of a user account
   *
   * @param userId User ID
   * @return A client list or an empty list
   */
  @Operation(summary = "List all clients of an account")
  @GetMapping("/list/{userId}")
  public List<Client> listClients(@PathVariable UUID userId) {
    return clientService.getAll(userId);
  }

  /**
   * Get the client to show your details
   *
   * @param clientId Client ID of client to be g
   * @return The client or null as the result of the query
   */
  @Operation(summary = "Get a specific client")
  @GetMapping("/{clientId}")
  public Optional<Client> getClient(@PathVariable UUID clientId) {
    return clientService.getById(clientId);
  }

  /**
   * Create or Update a client, based on UUID type of ID attribute
   *
   * @param client The client to be created or updated
   * @return Client ID created or updated
   */
  @Operation(summary = "Create or edit a client")
  @PostMapping("/upsert")
  public UUID upsertClient(@RequestBody UpsertClientRequest client) {
    return clientService.save(client);
  }

  /**
   * Favorite the client to list on Favorites category on client list
   *
   * @param clientId Client ID
   * @param favorite New updated favorite value
   * @return OK status when favoriting a client is successful
   */
  @Operation(summary = "Favorite a specific client")
  @PatchMapping("/favorite/{clientId}")
  public StatusResponse toggleFavoriteClient(@PathVariable UUID clientId, @RequestBody Boolean favorite) {
    clientService.toggleFavorite(clientId, favorite);
    return new StatusResponse("OK");
  }

  /**
   * Delete the client from the system
   *
   * @param client Client to be deleted
   * @return OK status when deletion a client is successful
   */
  @Operation(summary = "Delete logically a specific client")
  @DeleteMapping("/delete/{clientId}")
  public StatusResponse deleteClient(@PathVariable UUID clientId) {
    clientService.delete(clientId);
    return new StatusResponse("OK");
  }

  /**
   * Verify if the email is already registered in the user account
   *
   * @param checkEmailRequest Object with user ID and e-mail inside
   * @return Is valid / true when the query is empty
   */
  @Operation(summary = "Verify the availability of an e-mail address")
  @PostMapping("/check-email")
  public Boolean checkEmail(@RequestBody CheckEmailRequest checkEmailRequest) {
    return clientService.checkEmail(checkEmailRequest);
  }
}
