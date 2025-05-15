package org.example.demo.service.impl;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.example.demo.model.entity.Order;
import org.example.demo.model.entity.OrderItem;
import org.example.demo.model.entity.Product;
import org.example.demo.model.request.CreateOrderRequest;
import org.example.demo.model.request.OrderItemRequest;
import org.example.demo.repository.OrderItemRepository;
import org.example.demo.repository.OrderRepository;
import org.example.demo.repository.ProductRepository;
import org.example.demo.service.OrderService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor // Lombok
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final ProductRepository productRepository;
  private final TransactionalOperator transactionalOperator;

  @Override
  public Mono<Order> getOrderById(Long orderId) {
    return orderRepository.findById(orderId)
        .switchIfEmpty(Mono.error(new RuntimeException("Order not found with id: " + orderId)))
        .flatMap(
            order -> orderItemRepository.findByOrdersId(order.getId()).collectList().map(items -> {
              return order;
            }));
  }

  @Override
  public Flux<Order> getAllOrders() {
    return orderRepository.findAll();
  }

  public Mono<Order> createOrder(CreateOrderRequest request) {
    List<Long> productIds = request.getItems().stream().map(OrderItemRequest::getProductId)
        .collect(Collectors.toList());
    return productRepository.findByIdIn(productIds).collectMap(Product::getId)
        .flatMap(foundProducts -> {
          // Validar si todos los IDs existen
          if (foundProducts.size() != productIds.size()) {
            List<Long> foundProductIds = foundProducts.keySet().stream().toList();
            String missingIds = productIds.stream().filter(id -> !foundProductIds.contains(id))
                .map(String::valueOf).collect(Collectors.joining(", "));
            return Mono.error(
                new RuntimeException("One or more products not found. Missing IDs: " + missingIds));
          }
          // Validar stock preliminar (opcional, la validación real es al confirmar)
          for (OrderItemRequest itemReq : request.getItems()) {
            Product p = foundProducts.get(itemReq.getProductId());
            if (p.getStock() < itemReq.getQuantity()) {
              return Mono.error(new RuntimeException(
                  "Preliminary insufficient stock for product ID: " + p.getId()));
            }
          }

          // Crear Order en estado PENDIENTE
          Order nuevoOrder = Order.builder().date(LocalDateTime.now()).state("PENDIENTE").build();

          return orderRepository.save(nuevoOrder).flatMap(savedOrder -> {
            // Crear OrderItems asociados
            List<OrderItem> items = request.getItems().stream()
                .map(itemReq -> OrderItem.builder().ordersId(savedOrder.getId())
                    .productId(itemReq.getProductId()).quantity(itemReq.getQuantity())
                    .unitPrice(foundProducts.get(itemReq.getProductId()).getPrice()).build())
                .collect(Collectors.toList());

            return orderItemRepository.saveAll(items).then(Mono.just(savedOrder));
          });
        });
  }

  public Mono<Order> confirmOrder(Long orderId) {
    // ¡¡Esta es la operación CRÍTICA y necesita transacción!!
    return transactionalOperator.execute(status -> // Inicia la transacción reactiva
    orderRepository.findById(orderId)
        .switchIfEmpty(Mono.error(new RuntimeException("Order not found: " + orderId))) // Excepción
        // específica
        .filter(p -> "PENDIENTE".equals(p.getState())) // Solo confirmar pendientes
        .switchIfEmpty(
            Mono.error(new RuntimeException("Order is not in PENDING state: " + orderId)))
        .flatMap(
            order -> orderItemRepository.findByOrdersId(orderId).collectList().flatMap(items -> {
              if (items.isEmpty()) {
                status.setRollbackOnly(); // Marcar para rollback
                return Mono.error(new RuntimeException("Order has no items: " + orderId));
              }
              List<Long> productIds = items.stream().map(OrderItem::getProductId)
                  .collect(Collectors.toList());

              // 4. Obtener Products involucrados CON su versión actual
              return productRepository.findByIdIn(productIds).collectMap(Product::getId)
                  .flatMap(productsMap -> {
                    // Validar que todos los productos de los items existen en el mapa
                    for (OrderItem item : items) {
                      if (!productsMap.containsKey(item.getProductId())) {
                        status.setRollbackOnly();
                        return Mono.error(
                            new RuntimeException("Product from order item not found in database: "
                                + item.getProductId()));
                      }
                    }

                    // 5. Validar stock ACTUAL y calcular total bruto
                    double totalGross = 0.0;
                    for (OrderItem item : items) {
                      Product product = productsMap.get(item.getProductId());
                      // product null check ya no es necesario por la validación anterior
                      if (product.getStock() < item.getQuantity()) {
                        status.setRollbackOnly(); // Marcar para rollback
                        return Mono.error(new RuntimeException(
                            "Final insufficient stock for product ID: " + product.getId())); // Excepción
                        // específica
                      }
                      // Usar precio guardado en OrderItem para el cálculo
                      totalGross += item.getUnitPrice() * item.getQuantity();
                    }

                    // 6. Calcular Descuentos
                    double discountRate = 0.0;
                    if (totalGross > 1000.0) {
                      discountRate += 0.10;
                    }
                    // Contar products diferentes
                    long uniqueProducts = items.stream().map(OrderItem::getProductId).distinct()
                        .count();
                    if (uniqueProducts > 5) {
                      discountRate += 0.05;
                    }
                    double totalFinal = totalGross * (1.0 - discountRate);

// Dentro de confirmOrder, antes del Flux<Integer> updateStockFlux = ...

// Agrupar items por productId y sumar cantidades
                    Map<Long, Integer> totalQuantityPerProduct = items.stream()
                        .collect(Collectors.groupingBy(OrderItem::getProductId,
                            Collectors.summingInt(OrderItem::getQuantity)));

// Luego, itera sobre este mapa para actualizar el stock una vez por producto
                    Flux<Integer> updateStockFlux = Flux
                        .fromIterable(totalQuantityPerProduct.entrySet()).concatMap(entry -> {
                          Long productId = entry.getKey();
                          Integer totalQuantityToDecrement = entry.getValue();
                          Product p = productsMap.get(productId); // Obtener el producto

                          if (p == null) { // Seguridad adicional, aunque ya validaste productsMap
                            status.setRollbackOnly();
                            return Mono.error(new RuntimeException(
                                "Product not found in map during stock update: " + productId));
                          }
                          if (p.getStock() < totalQuantityToDecrement) { // Re-chequear stock con la
                                                                         // cantidad total
                            status.setRollbackOnly();
                            return Mono.error(new RuntimeException(
                                "Final insufficient stock (grouped) for product ID: " + p.getId()));
                          }

                          int newStock = p.getStock() - totalQuantityToDecrement;
                          System.out.println("Attempting to update stock for product ID: "
                              + p.getId() + ", currentStock: " + p.getStock()
                              + ", totalQuantityToDecrement: " + totalQuantityToDecrement
                              + ", newStockCalculated: " + newStock + ", expectedVersion: "
                              + p.getVersion());

                          return productRepository.updateStockOptimistic(p.getId(), newStock,
                              p.getVersion());
                        });

                    // 8. Actualizar Order (estado, totales)
                    order.setTotalGross(totalGross);
                    order.setTotalFinal(totalFinal);
                    order.setState("CONFIRMADO"); // Usar Enum EstadoOrder.CONFIRMADO

                    // Ejecutar actualizaciones de stock y LUEGO guardar el order
                    // Si updateStockFlux emite un error (ej. por switchIfEmpty), la transacción
                    // hará rollback.
                    return updateStockFlux.collectList().then(orderRepository.save(order));
                  });
            })))
        .single()
        .onErrorMap(ex -> {
          System.err.println(
              "Error confirming order, transaction will be rolled back: " + ex.getMessage());
          return ex; // Re-lanzar la excepción para que GlobalExceptionHandler la maneje
        });
  }

}