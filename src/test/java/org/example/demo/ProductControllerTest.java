package org.example.demo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.r2dbc.spi.ConnectionFactory;
import org.example.demo.controller.ProductController;
import org.example.demo.exception.GlobalExceptionHandler;
import org.example.demo.model.entity.Product;
import org.example.demo.model.request.CreateProductRequest;
import org.example.demo.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.webflux.core.configuration.SpringDocWebFluxConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcTransactionManagerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

// Asegúrate que NO tienes @SpringBootTest aquí si quieres un test de slice
@WebFluxTest(controllers = ProductController.class) // Solo el controlador
public class ProductControllerTest {

  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private ProductService productService;

  @MockBean // Mockea la ConnectionFactory
  private ConnectionFactory connectionFactory;

  @Test
  void createProduct_shouldReturnCreatedProduct() {
    CreateProductRequest request = CreateProductRequest.builder().name("Test Product").price(100.0)
        .stock(10).build();

    Product expectedProduct = Product.builder().id(1L).name("Test Product").price(100.0).stock(10)
        .version(0L).build();

    when(productService.createProduct(any(CreateProductRequest.class)))
        .thenReturn(Mono.just(expectedProduct));

    webTestClient.post().uri("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request).exchange().expectStatus().isCreated().expectBody(Product.class)
        .isEqualTo(expectedProduct);
  }

  @Test
  void createProduct_whenInvalidRequest_shouldReturnBadRequest() {
    CreateProductRequest invalidRequest = CreateProductRequest.builder().name("") // Nombre vacío,
                                                                                  // debería fallar
                                                                                  // la validación
        .price(-10.0) // Precio negativo
        .stock(-1).build();

    // No necesitamos mockear el servicio aquí ya que la validación ocurre antes
    // Spring Boot Test maneja WebExchangeBindException y devuelve 400

    webTestClient.post().uri("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
        .bodyValue(invalidRequest).exchange().expectStatus().isBadRequest();
    // Puedes también verificar el cuerpo del error si tu GlobalExceptionHandler
    // está activo en el contexto de prueba
    // .expectBody().jsonPath("$.message").isNotEmpty();
  }

  @Test
  void getProductById_whenProductExists_shouldReturnProduct() {
    Long productId = 1L;
    Product expectedProduct = Product.builder().id(productId).name("Found Product").price(50.0)
        .stock(5).version(1L).build();

    when(productService.getProductById(productId)).thenReturn(Mono.just(expectedProduct));

    webTestClient.get().uri("/api/v1/products/{id}", productId).exchange().expectStatus().isOk()
        .expectBody(Product.class).isEqualTo(expectedProduct);
  }

  @Test
  void getProductById_whenProductNotExists_shouldReturnNotFound() {
    Long productId = 99L;
    when(productService.getProductById(productId))
        .thenReturn(Mono.error(new RuntimeException("Product not found"))); // Simula error del
                                                                            // servicio

    webTestClient.get().uri("/api/v1/products/{id}", productId).exchange().expectStatus()
        .isNotFound(); // Asumiendo que GlobalExceptionHandler mapea "not found" a 404
  }
}