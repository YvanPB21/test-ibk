package org.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.example.demo.model.entity.Order;
import org.example.demo.model.request.CreateOrderRequest;
import org.example.demo.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order API", description = "API for order management")
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a new order in PENDING state",
          description = "Creates a new order with a list of items. The order is initially in PENDING state.", responses = {
      @ApiResponse(responseCode = "201", description = "Order created successfully",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class))),
      @ApiResponse(responseCode = "400",
              description = "Invalid input data (e.g., product not found, insufficient stock, invalid request format)",
              content = @Content(mediaType = "application/json",
                      schema = @Schema(implementation = org.example.demo.exception.GlobalExceptionHandler.ErrorResponse.class))) })
  public Mono<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    return orderService.createOrder(request);
  }

  @PutMapping("/{id}/confirm")
  @Operation(summary = "Confirm an order, calculate total with discounts, and update stock",
          description = "Confirms a PENDING order. This calculates the final total applying discounts and updates product stock. This operation is transactional.", responses = {
      @ApiResponse(responseCode = "200", description = "Order confirmed successfully",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class))),
      @ApiResponse(responseCode = "400", description = "Invalid operation (e.g., order not pending, order has no items, insufficient stock)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.example.demo.exception.GlobalExceptionHandler.ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "Order not found",
              content = @Content(mediaType = "application/json",
                      schema = @Schema(implementation = org.example.demo.exception.GlobalExceptionHandler.ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "Concurrency conflict during stock update",
              content = @Content(mediaType = "application/json",
                      schema = @Schema(implementation = org.example.demo.exception.GlobalExceptionHandler.ErrorResponse.class))) })
  public Mono<Order> confirmarOrder(
      @Parameter(description = "ID of the order to be confirmed") @PathVariable Long id) {
    return orderService.confirmOrder(id);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get an order by its ID", description = "Retrieves the details of a specific order.", responses = {
      @ApiResponse(responseCode = "200", description = "Order found",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class))),
      @ApiResponse(responseCode = "404", description = "Order not found",
              content = @Content(mediaType = "application/json",
                      schema = @Schema(implementation = org.example.demo.exception.GlobalExceptionHandler.ErrorResponse.class))) })
  public Mono<Order> getOrderById(
      @Parameter(description = "ID of the order to retrieve") @PathVariable Long id) {
    return orderService.getOrderById(id);
  }

  @GetMapping
  @Operation(summary = "Get all orders",
          description = "Retrieves a list of all orders. For detailed items per order, use the getOrderById endpoint or consider a dedicated DTO if performance for lists with details is critical.", responses = {
      @ApiResponse(responseCode = "200",
              description = "Successfully retrieved list of orders") })
  public Flux<Order> getAllOrders() {
    return orderService.getAllOrders();
  }
}