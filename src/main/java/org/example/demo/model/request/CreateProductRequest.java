package org.example.demo.model.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
  @NotBlank(message = "Product name cannot be blank")
  private String name;

  @NotNull(message = "Product price cannot be null")
  @Positive(message = "Product price must be positive")
  private Double price;

  @NotNull(message = "Product stock cannot be null")
  @PositiveOrZero(message = "Product stock must be positive or zero")
  private Integer stock;
}