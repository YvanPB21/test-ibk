package org.example.demo.service;

import org.example.demo.model.entity.Product;
import org.example.demo.model.request.CreateProductRequest;
import org.example.demo.model.request.UpdateProductRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {
  Mono<Product> createProduct(CreateProductRequest request);

  Mono<Product> getProductById(Long productId);

  Flux<Product> getAllProducts();

  Mono<Product> updateProduct(Long productId, UpdateProductRequest request);

  Mono<Void> deleteProduct(Long productId);
}