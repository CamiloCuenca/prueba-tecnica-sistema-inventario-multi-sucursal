package com.camilocuenca.inventorysystem.specification;

import com.camilocuenca.inventorysystem.model.AuditLog;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class AuditLogSpecifications {

    private AuditLogSpecifications() {}

    public static Specification<AuditLog> filter(
            String usuario,
            String accion,
            String resultado,
            LocalDateTime desde,
            LocalDateTime hasta
    ) {
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (usuario != null && !usuario.isBlank()) {
                preds.add(cb.equal(cb.lower(root.get("usuario")), usuario.toLowerCase()));
            }
            if (accion != null && !accion.isBlank()) {
                preds.add(cb.equal(cb.lower(root.get("accion")), accion.toLowerCase()));
            }
            if (resultado != null && !resultado.isBlank()) {
                preds.add(cb.equal(root.get("resultado"), resultado));
            }
            if (desde != null) {
                preds.add(cb.greaterThanOrEqualTo(root.get("fecha"), desde));
            }
            if (hasta != null) {
                preds.add(cb.lessThanOrEqualTo(root.get("fecha"), hasta));
            }
            return preds.isEmpty() ? cb.conjunction() : cb.and(preds.toArray(new Predicate[0]));
        };
    }
}
