package org.example.demo.repository;

import org.example.demo.model.entity.Order;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, Long> {
}
