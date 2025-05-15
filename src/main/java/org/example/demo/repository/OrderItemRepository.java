package org.example.demo.repository;

import org.example.demo.model.entity.OrderItem;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;

@Repository
public interface OrderItemRepository extends R2dbcRepository<OrderItem, Long> {
  Flux<OrderItem> findByOrdersId(Long orderId);

}
