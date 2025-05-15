package org.example.demo.repository;

import org.example.demo.model.entity.Product;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ProductRepository extends R2dbcRepository<Product, Long> {
    @Query("UPDATE PRODUCT SET stock = :newStock, version = version + 1 WHERE id = :id AND version = :expectedVersion")
    Mono<Integer> updateStockOptimistic(Long id, Integer newStock, Long expectedVersion);

  Flux<Product> findByIdIn(Collection<Long> id);
}
