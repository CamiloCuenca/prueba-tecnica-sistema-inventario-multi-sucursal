package com.camilocuenca.inventorysystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "transfer_alert")
public class TransferAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id")
    private Transfer transfer;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "missing_quantity", precision = 12, scale = 2)
    private BigDecimal missingQuantity;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "status", length = 50)
    private String status; // e.g., OPEN, RESOLVED

}

