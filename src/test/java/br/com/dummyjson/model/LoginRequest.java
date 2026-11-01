package br.com.dummyjson.model;

/** Corpo do {@code POST /auth/login}. */
public record LoginRequest(String username, String password) {}
