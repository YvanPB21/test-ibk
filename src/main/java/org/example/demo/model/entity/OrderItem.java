package org.example.demo.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("ORDERS_ITEM")
public class OrderItem {
  @Id
  private Long id;
  @Column("orders_id")
  private Long ordersId;
  @Column("product_id")
  private Long productId;
  private Integer quantity;
  @Column("unit_price")
  private Double unitPrice;
}
