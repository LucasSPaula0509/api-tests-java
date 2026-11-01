package br.com.dummyjson.model;

/** Corpo do {@code POST /products/add}. */
public record ProductRequest(
    String title,
    String description,
    double price,
    double discountPercentage,
    double rating,
    int stock,
    String brand,
    String category,
    String thumbnail) {}
