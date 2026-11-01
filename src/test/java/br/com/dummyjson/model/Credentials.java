package br.com.dummyjson.model;

/** Usuário e senha obtidos em {@code GET /users} (a API de demonstração expõe a senha). */
public record Credentials(String username, String password) {

  public LoginRequest toLoginRequest() {
    return new LoginRequest(username, password);
  }
}
