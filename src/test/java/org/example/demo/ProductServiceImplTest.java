package org.example.demo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.demo.model.entity.Product;
import org.example.demo.model.request.CreateProductRequest;
import org.example.demo.repository.ProductRepository;
import org.example.demo.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class) // Para usar anotaciones de Mockito
public class ProductServiceImplTest {

  @Mock
  private ProductRepository productRepository;

  @InjectMocks // Crea una instancia de ProductServiceImpl e inyecta los mocks
  private ProductServiceImpl productService;

  @Test
  void createProduct_shouldSaveAndReturnProduct() {
    CreateProductRequest request = CreateProductRequest.builder().name("New Gadget").price(250.75)
        .stock(50).build();

    Product savedProduct = Product.builder().id(1L) // Simula ID generado por la base de datos
        .name(request.getName()).price(request.getPrice()).stock(request.getStock()).version(0L)
        .build();

    // Mockear el comportamiento del repositorio
    when(productRepository.save(any(Product.class))).thenReturn(Mono.just(savedProduct));

    Mono<Product> resultMono = productService.createProduct(request);

    StepVerifier.create(resultMono).expectNextMatches(product -> {
      // Verificaciones sobre el producto devuelto
      return product.getName().equals(request.getName())
          && product.getPrice().equals(request.getPrice())
          && product.getStock().equals(request.getStock()) && product.getId() != null
          && product.getVersion() == 0L;
    }).verifyComplete(); // Verifica que el Mono completa exitosamente
  }

  @Test
  void getProductById_whenProductExists_shouldReturnProduct() {
    Long productId = 1L;
    Product expectedProduct = Product.builder().id(productId).name("Existing Product").price(10.0)
        .stock(1).version(0L).build();
    when(productRepository.findById(productId)).thenReturn(Mono.just(expectedProduct));

    Mono<Product> resultMono = productService.getProductById(productId);

    StepVerifier.create(resultMono).expectNext(expectedProduct).verifyComplete();
  }

  @Test
  void getProductById_whenProductNotExists_shouldReturnError() {
    Long productId = 99L;
    when(productRepository.findById(productId)).thenReturn(Mono.empty()); // Simula que no se
                                                                          // encuentra

    Mono<Product> resultMono = productService.getProductById(productId);

    StepVerifier.create(resultMono)
        .expectErrorMatches(throwable -> throwable instanceof RuntimeException
            && throwable.getMessage().contains("Product not found with id: " + productId))
        .verify();
  }
}