package com.camilocuenca.inventorysystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;
import java.math.BigDecimal;

import com.camilocuenca.inventorysystem.Enums.RoutePriority;

/**
 * Entidad Transfer: representa una transferencia de productos entre sucursales.
 * Se añaden campos logísticos (carrier, estimatedArrival, routePriority, routeCost,
 * estimatedTransitMinutes) para soportar visibilidad de rutas y tiempos de envío
 * en el módulo de logística.
 */
@Getter
@Setter
@Entity
@Table(name = "transfer")
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_branch_id", nullable = false)
    private Branch originBranch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_branch_id", nullable = false)
    private Branch destinationBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    @Column(name = "received_at")
    private Instant receivedAt;

    /** Transportista responsable del envío */
    @Column(name = "carrier", length = 200)
    private String carrier;

    /** Fecha estimada de llegada */
    @Column(name = "estimated_arrival")
    private Instant estimatedArrival;

    /** Identificador de la ruta (opcional) */
    @Column(name = "route_id")
    private String routeId;

    /** Prioridad de la ruta */
    @Enumerated(EnumType.STRING)
    @Column(name = "route_priority", length = 20)
    private RoutePriority routePriority;


    /** Minutos reales de tránsito calculados cuando se recibe */
    @Column(name = "actual_transit_minutes")
    private Integer actualTransitMinutes;

    /** Costo estimado de la ruta (valor monetario) */
    @Column(name = "route_cost", precision = 18, scale = 2)
    private BigDecimal routeCost;

}