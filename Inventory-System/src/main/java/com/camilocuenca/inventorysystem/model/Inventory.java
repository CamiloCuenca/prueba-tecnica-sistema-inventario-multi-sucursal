package com.camilocuenca.inventorysystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ColumnDefault("0")
    @Column(name = "quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    // Nuevo: stock mínimo configurado para este producto en esta sucursal
    @Column(name = "min_stock", precision = 12, scale = 2)
    private BigDecimal minStock;

    // Precio de venta actual en esta sucursal (moneda)
    @ColumnDefault("0")
    @Column(name = "sale_price", precision = 12, scale = 2)
    private BigDecimal salePrice;

    // Costo promedio ponderado del inventario en esta sucursal
    @ColumnDefault("0")
    @Column(name = "average_cost", precision = 12, scale = 2)
    private BigDecimal averageCost;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;


}