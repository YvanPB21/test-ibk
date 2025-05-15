package org.example.demo.service;

import org.example.demo.model.entity.Order;
import org.example.demo.model.request.CreateOrderRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {
  Mono<Order> createOrder(CreateOrderRequest request);

  Mono<Order> confirmOrder(Long orderId);

  Mono<Order> getOrderById(Long orderId);

  Flux<Order> getAllOrders();
}
