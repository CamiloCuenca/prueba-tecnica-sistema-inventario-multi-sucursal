package com.camilocuenca.inventorysystem.model;

import com.camilocuenca.inventorysystem.Enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "stock_alert")
public class StockAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "urgency", nullable = false, length = 20)
    private String urgency; // CRÍTICO, ALTO, MEDIO

    @Column(name = "notified_at")
    private Instant notifiedAt;

    @Column(name = "created_by")
    private UUID createdBy;
}

