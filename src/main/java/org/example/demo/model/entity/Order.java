package org.example.demo.model.entity;

import java.time.LocalDateTime;

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
@Table("orders")
public class Order {
  @Id
  private Long id;

  private LocalDateTime date;

  @Column("total_gross")
  private Double totalGross;

  @Column("total_final")
  private Double totalFinal;

  private String state;
}
