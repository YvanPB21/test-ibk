package org.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.example.demo.model.entity.Product;
import org.example.demo.model.request.CreateProductRequest;
import org.example.demo.model.request.UpdateProductRequest;
import org.example.demo.service.ProductService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product API", description = "API for product management")
public class ProductController {

  private final ProductService productService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a new product",
          description = "Creates a new product with the given details. Version is managed by the system.", responses = {
      @ApiResponse(responseCode = "201", description = "Product created successfully",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input data",
              content = @Content(mediaType = "application/json",
                      schema = @Schema(implementation = org.example.demo.exception.GlobalExceptionHandler.ErrorResponse.class))) })
  public Mono<Product> createProduct(@Valid @RequestBody CreateProductRequest request) {
    return productService.createProduct(request)
            .flatMap(savedProduct -> productService.getProductById(savedProduct.getId()));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a product by its ID", responses = {
      @ApiResponse(responseCode = "200", description = "Product found",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))),
      @ApiResponse(responseCode = "404", description = "Product not found",
              content = @Content(mediaType = "application/json",
                      schema = @Schema(implementation = org.example.demo.exception.GlobalExceptionHandler.ErrorResponse.class))) })
  public Mono<Product> getProductById(
      @Parameter(description = "ID of the product to be retrieved") @PathVariable Long id) {
    return productService.getProductById(id);
  }

  @GetMapping
  @Operation(summary = "Get all products", description = "Retrieves a list of all products.", responses = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved list of products",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))) })
  public Flux<Product> getAllProducts() {
    return productService.getAllProducts();
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing product", description = "Updates the details of an existing product identified by its ID. " +
          "Optimistic locking for version is implicitly handled if configured at entity level or explicitly in service " +
          "for specific fields like stock.", responses = {
      @ApiResponse(responseCode = "200", description = "Product updated successfully",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "404", description = "Product not found") })
  public Mono<Product> updateProduct(
      @Parameter(description = "ID of the product to be updated") @PathVariable Long id,
      @Valid @RequestBody UpdateProductRequest request) {
    return productService.updateProduct(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a product by its ID", responses = {
      @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Product not found") })
  public Mono<Void> deleteProduct(
      @Parameter(description = "ID of the product to be deleted") @PathVariable Long id) {
    return productService.deleteProduct(id);
  }
}