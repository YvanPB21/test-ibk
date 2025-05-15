package org.example.demo.service.impl;

import lombok.RequiredArgsConstructor;

import org.example.demo.model.entity.Product;
import org.example.demo.model.request.CreateProductRequest;
import org.example.demo.model.request.UpdateProductRequest;
import org.example.demo.repository.ProductRepository;
import org.example.demo.service.ProductService;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;

  @Override
  public Mono<Product> createProduct(CreateProductRequest request) {
    Product product = Product.builder().name(request.getName()).price(request.getPrice())
        .stock(request.getStock()).build();
    return productRepository.save(product);
  }

  @Override
  public Mono<Product> getProductById(Long productId) {
    return productRepository.findById(productId)
        .switchIfEmpty(Mono.error(new RuntimeException("Product not found with id: " + productId)));
  }

  @Override
  public Flux<Product> getAllProducts() {
    return productRepository.findAll();
  }

  @Override
  public Mono<Product> updateProduct(Long productId, UpdateProductRequest request) {
    return productRepository.findById(productId)
        .switchIfEmpty(Mono.error(new RuntimeException("Product not found with id: " + productId)))
        .flatMap(existingProduct -> {
          existingProduct.setName(request.getName());
          existingProduct.setPrice(request.getPrice());
          existingProduct.setStock(request.getStock());
          // The 'version' field will be handled by optimistic locking if you save it.
          // Spring Data R2DBC @Version might automatically increment if supported,
          // otherwise, your manual optimistic lock query in confirmOrder is specific.
          // For a general update, ensure the version is handled if needed,
          // or rely on the specific updateStockOptimistic for stock changes.
          // For this general update, we'll save, and if concurrent stock updates are
          // critical,
          // they should go through a method that uses updateStockOptimistic.
          return productRepository.save(existingProduct);
        });
  }

  @Override
  public Mono<Void> deleteProduct(Long productId) {
    return productRepository.findById(productId)
        .switchIfEmpty(Mono.error(new RuntimeException("Product not found with id: " + productId)))
        .flatMap(productRepository::delete);
  }
}