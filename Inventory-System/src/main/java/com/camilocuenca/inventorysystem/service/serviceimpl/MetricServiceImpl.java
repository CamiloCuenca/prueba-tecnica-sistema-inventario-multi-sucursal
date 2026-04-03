package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.dto.metrics.*;
import com.camilocuenca.inventorysystem.repository.MetricRepositoryCustom;
import com.camilocuenca.inventorysystem.service.serviceInterface.metricService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;

@Service
public class MetricServiceImpl implements metricService {

    private final MetricRepositoryCustom metricRepository;

    @PersistenceContext
    private EntityManager em;

    public MetricServiceImpl(MetricRepositoryCustom metricRepository) {
        this.metricRepository = metricRepository;
    }

    /**
     * Retorna página de objetos con el volumen de ventas por mes para el rango dado.
     * Si from/to son nulos, por defecto se consideran los últimos 3 meses (incluyendo mes actual).
     *
     * Nota: cualquier usuario autenticado puede invocar este servicio (controlado por el layer de seguridad).
     */
    @Override
    public Page<SalesVolumeDto> salesVolume(UUID branchId, UUID productId, LocalDate from, LocalDate to, Pageable pageable) {
        // Si no se provee rango, por defecto tomar el mes actual y los 2 meses anteriores
        LocalDate toDate = to != null ? to : LocalDate.now();
        LocalDate fromDate = from != null ? from : toDate.minusMonths(2).withDayOfMonth(1);

        Instant fromInstant = Instant.from(fromDate.atStartOfDay(ZoneId.systemDefault()));
        Instant toInstant = Instant.from(toDate.atTime(23,59,59).atZone(ZoneId.systemDefault()));

        int offset = (int) pageable.getOffset();
        int limit = pageable.getPageSize();

        long total = metricRepository.countSalesVolume(branchId, productId, fromInstant, toInstant);
        List<Object[]> rows = metricRepository.findSalesVolume(branchId, productId, fromInstant, toInstant, offset, limit);

        List<SalesVolumeDto> dtos = new ArrayList<>();
        for (Object[] r : rows) {
            SalesVolumeDto dto = new SalesVolumeDto();
            dto.setBranchId((UUID) r[0]);
            dto.setBranchName((String) r[1]);
            dto.setProductId((UUID) r[2]);
            dto.setProductName((String) r[3]);
            dto.setYear(((Number) r[4]).intValue());
            dto.setMonth(((Number) r[5]).intValue());
            dto.setTotalUnitsSold((BigDecimal) r[6]);
            dto.setTotalRevenue((BigDecimal) r[7]);
            dto.setPeriodStart(LocalDate.of(dto.getYear(), dto.getMonth(), 1));
            dto.setPeriodEnd(dto.getPeriodStart().withDayOfMonth(dto.getPeriodStart().lengthOfMonth()));
            dtos.add(dto);
        }

        return new PageImpl<>(dtos, pageable, total);
    }

    /**
     * Calcula el comportamiento del inventario para la sucursal indicada.
     * - rotationIndex = total_sold / current_stock (si current_stock == 0 -> usar total_sold)
     * - top 5 productos de alta demanda y top 5 de baja demanda (por total_sold)
     *
     * Si branchId == null se retorna una vista agregada global (útil para ADMIN).
     */
    @Override
    public InventoryBehaviorDto getInventoryBehavior(UUID branchId) {
        String sql;
        Query q;

        if (branchId == null) {
            // Vista global: sumar ventas y stock en todas las sucursales
            sql = "SELECT p.id as product_id, p.sku as sku, p.name as product_name, " +
                    "COALESCE(SUM(sd.quantity),0) as total_sold, COALESCE(SUM(i.quantity),0) as current_stock " +
                    "FROM product p " +
                    "LEFT JOIN sale_detail sd ON sd.product_id = p.id " +
                    "LEFT JOIN sale s ON s.id = sd.sale_id " +
                    "LEFT JOIN inventory i ON i.product_id = p.id " +
                    "GROUP BY p.id, p.sku, p.name";
            q = em.createNativeQuery(sql);
        } else {
            // Vista por sucursal: filtrar ventas e inventario por branch
            sql = "SELECT p.id as product_id, p.sku as sku, p.name as product_name, " +
                    "COALESCE(SUM(sd.quantity),0) as total_sold, COALESCE(SUM(i.quantity),0) as current_stock " +
                    "FROM product p " +
                    "LEFT JOIN sale_detail sd ON sd.product_id = p.id " +
                    "LEFT JOIN sale s ON s.id = sd.sale_id AND s.branch_id = ?1 " +
                    "LEFT JOIN inventory i ON i.product_id = p.id AND i.branch_id = ?2 " +
                    "GROUP BY p.id, p.sku, p.name";
            q = em.createNativeQuery(sql);
            q.setParameter(1, branchId);
            q.setParameter(2, branchId);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        List<InventoryRotationDto> all = new ArrayList<>();
        for (Object[] r : rows) {
            InventoryRotationDto dto = new InventoryRotationDto();
            dto.setProductId((UUID) r[0]);
            dto.setSku((String) r[1]);
            dto.setProductName((String) r[2]);

            BigDecimal totalSold = BigDecimal.ZERO;
            BigDecimal currentStock = BigDecimal.ZERO;

            // Manejar distintos tipos retornados por JDBC (BigDecimal, Long, Integer)
            if (r[3] instanceof BigDecimal) {
                totalSold = (BigDecimal) r[3];
            } else if (r[3] instanceof Number) {
                totalSold = BigDecimal.valueOf(((Number) r[3]).doubleValue());
            }

            if (r[4] instanceof BigDecimal) {
                currentStock = (BigDecimal) r[4];
            } else if (r[4] instanceof Number) {
                currentStock = BigDecimal.valueOf(((Number) r[4]).doubleValue());
            }

            dto.setTotalSold(totalSold);
            dto.setCurrentStock(currentStock);
            double rotation;
            if (currentStock.compareTo(BigDecimal.ZERO) > 0) {
                rotation = totalSold.divide(currentStock, 4, RoundingMode.HALF_UP).doubleValue();
            } else {
                rotation = totalSold.doubleValue();
            }
            dto.setRotationIndex(rotation);
            all.add(dto);
        }

        // Crear listas top high/low
        List<InventoryRotationDto> sortedDesc = new ArrayList<>(all);
        sortedDesc.sort(Comparator.comparing(InventoryRotationDto::getTotalSold).reversed());
        List<InventoryRotationDto> topHigh = sortedDesc.size() > 5 ? new ArrayList<>(sortedDesc.subList(0,5)) : sortedDesc;

        List<InventoryRotationDto> sortedAsc = new ArrayList<>(all);
        sortedAsc.sort(Comparator.comparing(InventoryRotationDto::getTotalSold));
        List<InventoryRotationDto> topLow = sortedAsc.size() > 5 ? new ArrayList<>(sortedAsc.subList(0,5)) : sortedAsc;

        InventoryBehaviorDto result = new InventoryBehaviorDto();
        result.setRotations(all);
        result.setTopHighDemand(topHigh);
        result.setTopLowDemand(topLow);
        return result;
    }

    /**
     * Impacto de transfers activos hacia la sucursal destino.
     */
    @Override
    public List<TransferImpactDto> getActiveTransfersImpact(UUID destinationBranchId) {
        String sql = "SELECT t.id as transfer_id, t.origin_branch_id as origin_branch_id, t.destination_branch_id as destination_branch_id, t.status as status, td.product_id as product_id, SUM(td.quantity) as qty " +
                "FROM transfer t " +
                "JOIN transfer_detail td ON td.transfer_id = t.id " +
                "WHERE (UPPER(t.status) IN ('EN_TRANSITO','EN_TRANSIT','PREPARANDO','PREPARING') OR t.status IN ('EN_TRANSITO','EN_TRANSIT','PREPARANDO','PREPARING')) " +
                "AND t.destination_branch_id = ?1 " +
                "GROUP BY t.id, t.origin_branch_id, t.destination_branch_id, t.status, td.product_id";

        Query q = em.createNativeQuery(sql);
        q.setParameter(1, destinationBranchId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        Map<UUID, TransferImpactDto> map = new LinkedHashMap<>();
        for (Object[] r : rows) {
            UUID transferId = (UUID) r[0];
            UUID originBranchId = (UUID) r[1];
            UUID destinationId = (UUID) r[2];
            String status = (String) r[3];
            UUID productId = (UUID) r[4];
            BigDecimal qty = r[5] != null ? (BigDecimal) r[5] : BigDecimal.ZERO;

            TransferImpactDto dto = map.get(transferId);
            if (dto == null) {
                dto = new TransferImpactDto();
                dto.setTransferId(transferId);
                dto.setOriginBranchId(originBranchId);
                dto.setDestinationBranchId(destinationId);
                dto.setStatus(status);
                dto.setOriginBranchName(null);
                dto.setDestinationBranchName(null);
                dto.setItems(new ArrayList<>());
                map.put(transferId, dto);
            }

            TransferImpactItemDto item = new TransferImpactItemDto();
            item.setProductId(productId);
            item.setQuantity(qty);
            dto.getItems().add(item);
        }

        return new ArrayList<>(map.values());
    }

    /**
     * Comparativa de performance entre sucursales para el mes actual.
     * Retorna página de objetos con el revenue total por sucursal, paginado.
     */
    @Override
    public Page<BranchPerformanceDto> getBranchPerformanceComparison(Pageable pageable) {
        // Determinar primer y último día del mes actual en UTC
        LocalDate now = LocalDate.now();
        LocalDate firstDay = now.withDayOfMonth(1);
        LocalDate lastDay = now.withDayOfMonth(now.lengthOfMonth());
        Instant from = firstDay.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant to = lastDay.atTime(23,59,59).atZone(ZoneId.systemDefault()).toInstant();

        int offset = (int) pageable.getOffset();
        int limit = pageable.getPageSize();

        // Count total branches with sales in month (for pagination)
        String countSql = "SELECT COUNT(1) FROM (" +
                "SELECT s.branch_id FROM sale s JOIN sale_detail sd ON sd.sale_id = s.id " +
                "WHERE s.created_at >= ?1 AND s.created_at <= ?2 GROUP BY s.branch_id) t";
        Query countQ = em.createNativeQuery(countSql);
        countQ.setParameter(1, from);
        countQ.setParameter(2, to);
        Number totalCount = ((Number) countQ.getSingleResult());
        long total = totalCount != null ? totalCount.longValue() : 0L;

        // Main query: sum revenue per branch
        String sql = "SELECT b.id as branch_id, b.name as branch_name, SUM(sd.quantity * sd.price) as total_revenue " +
                "FROM sale s " +
                "JOIN sale_detail sd ON sd.sale_id = s.id " +
                "JOIN branch b ON b.id = s.branch_id " +
                "WHERE s.created_at >= ?1 AND s.created_at <= ?2 " +
                "GROUP BY b.id, b.name " +
                "ORDER BY total_revenue DESC " +
                "LIMIT ?3 OFFSET ?4";

        Query q = em.createNativeQuery(sql);
        q.setParameter(1, from);
        q.setParameter(2, to);
        q.setParameter(3, limit);
        q.setParameter(4, offset);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        List<BranchPerformanceDto> items = new ArrayList<>();
        for (Object[] r : rows) {
            BranchPerformanceDto dto = new BranchPerformanceDto();
            dto.setBranchId((UUID) r[0]);
            dto.setBranchName((String) r[1]);
            BigDecimal rev = r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO;
            dto.setTotalRevenue(rev.setScale(2, RoundingMode.HALF_UP));
            items.add(dto);
        }

        return new PageImpl<>(items, pageable, total);
    }

}
