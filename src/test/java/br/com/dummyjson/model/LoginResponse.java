package br.com.dummyjson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Resposta do {@code POST /auth/login}: dados do usuário e os tokens de acesso. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginResponse(
    int id,
    String username,
    String email,
    String firstName,
    String lastName,
    String gender,
    String image,
    String accessToken,
    String refreshToken) {}
