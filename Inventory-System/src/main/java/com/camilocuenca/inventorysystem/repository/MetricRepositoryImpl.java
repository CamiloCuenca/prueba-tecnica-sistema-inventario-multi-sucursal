package com.camilocuenca.inventorysystem.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class MetricRepositoryImpl implements MetricRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Object[]> findSalesVolume(UUID branchId, UUID productId, Instant fromDate, Instant toDate, int offset, int limit) {
        String sql = "SELECT b.id as branch_id, b.name as branch_name, sd.product_id as product_id, p.name as product_name, " +
                "EXTRACT(YEAR FROM s.created_at) as year, EXTRACT(MONTH FROM s.created_at) as month, " +
                "SUM(sd.quantity) as total_units, SUM(sd.quantity * sd.price) as total_revenue " +
                "FROM sale s " +
                "JOIN sale_detail sd ON sd.sale_id = s.id " +
                "JOIN branch b ON b.id = s.branch_id " +
                "JOIN product p ON p.id = sd.product_id " +
                "WHERE s.created_at >= :fromDate AND s.created_at <= :toDate " +
                (branchId != null ? "AND s.branch_id = :branchId " : "") +
                (productId != null ? "AND sd.product_id = :productId " : "") +
                "GROUP BY b.id, b.name, sd.product_id, p.name, EXTRACT(YEAR FROM s.created_at), EXTRACT(MONTH FROM s.created_at) " +
                "ORDER BY EXTRACT(YEAR FROM s.created_at) DESC, EXTRACT(MONTH FROM s.created_at) DESC " +
                "LIMIT :limit OFFSET :offset";

        Query query = em.createNativeQuery(sql);
        Timestamp tsFrom = Timestamp.from(fromDate);
        Timestamp tsTo = Timestamp.from(toDate);
        query.setParameter("fromDate", tsFrom);
        query.setParameter("toDate", tsTo);
        if (branchId != null) query.setParameter("branchId", branchId);
        if (productId != null) query.setParameter("productId", productId);
        query.setParameter("limit", limit);
        query.setParameter("offset", offset);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows;
    }

    @Override
    public long countSalesVolume(UUID branchId, UUID productId, Instant fromDate, Instant toDate) {
        String sql = "SELECT COUNT(1) FROM (" +
                "SELECT 1 FROM sale s " +
                "JOIN sale_detail sd ON sd.sale_id = s.id " +
                "JOIN branch b ON b.id = s.branch_id " +
                "JOIN product p ON p.id = sd.product_id " +
                "WHERE s.created_at >= :fromDate AND s.created_at <= :toDate " +
                (branchId != null ? "AND s.branch_id = :branchId " : "") +
                (productId != null ? "AND sd.product_id = :productId " : "") +
                "GROUP BY b.id, b.name, sd.product_id, p.name, EXTRACT(YEAR FROM s.created_at), EXTRACT(MONTH FROM s.created_at) " +
                ") t";

        Query query = em.createNativeQuery(sql);
        Timestamp tsFrom = Timestamp.from(fromDate);
        Timestamp tsTo = Timestamp.from(toDate);
        query.setParameter("fromDate", tsFrom);
        query.setParameter("toDate", tsTo);
        if (branchId != null) query.setParameter("branchId", branchId);
        if (productId != null) query.setParameter("productId", productId);
        Number count = ((Number) query.getSingleResult());
        return count.longValue();
    }
}
