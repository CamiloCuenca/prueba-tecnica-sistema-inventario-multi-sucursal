import { useEffect, useMemo, useState } from "react";
import { decodeJWT } from "../../utils/jwt";
import { useInventory } from "../inventory/useInventory";
import SalesVolumeBranchSelector from "./components/SalesVolumeBranchSelector";
import SalesVolumeFilters from "./components/SalesVolumeFilters";
import SalesVolumeInsights from "./components/SalesVolumeInsights";
import SalesVolumeKpiCards from "./components/SalesVolumeKpiCards";
import SalesVolumeRevenueChart from "./components/SalesVolumeRevenueChart";
import SalesVolumeTopProductsTable from "./components/SalesVolumeTopProductsTable";
import SalesVolumeUnitsChart from "./components/SalesVolumeUnitsChart";
import {
  aggregateByMonth,
  DEFAULT_END,
  DEFAULT_START,
  monthInputToDateRange,
  shiftMonthValue,
  toMonthInputValue,
} from "./salesVolumeUtils";
import { useSalesVolumeMetrics } from "./useSalesVolumeMetrics";

export default function SalesVolumeDashboard() {
  const { handleBranches, loading: branchesLoading, error: branchesError } = useInventory();
  const [branches, setBranches] = useState([]);
  const [selectedBranchId, setSelectedBranchId] = useState(null);
  const [fromMonth, setFromMonth] = useState(toMonthInputValue(DEFAULT_START));
  const [toMonth, setToMonth] = useState(toMonthInputValue(DEFAULT_END));

  useEffect(() => {
    handleBranches().then((res) => {
      if (Array.isArray(res)) setBranches(res);
    });
    // eslint-disable-next-line
  }, []);

  useEffect(() => {
    const token = sessionStorage.getItem("token");
    if (!token) return;
    const payload = decodeJWT(token);
    setSelectedBranchId(payload?.branchId || null);
  }, []);

  useEffect(() => {
    if (!selectedBranchId && branches.length > 0) {
      setSelectedBranchId(branches[0].id);
    }
  }, [branches, selectedBranchId]);

  const normalizedFromMonth = fromMonth <= toMonth ? fromMonth : toMonth;
  const normalizedToMonth = fromMonth <= toMonth ? toMonth : fromMonth;
  const from = useMemo(() => monthInputToDateRange(normalizedFromMonth, "start"), [normalizedFromMonth]);
  const to = useMemo(() => monthInputToDateRange(normalizedToMonth, "end"), [normalizedToMonth]);

  const { data, loading: metricsLoading, error: metricsError } = useSalesVolumeMetrics({
    branchId: selectedBranchId,
    from,
    to,
    size: 200,
  });

  const monthly = useMemo(() => aggregateByMonth(data ?? []), [data]);
  const hasMetrics = monthly.length > 0;

  const currentMonth = hasMetrics ? monthly[monthly.length - 1] : null;
  const previousMonth = monthly.length > 1 ? monthly[monthly.length - 2] : null;
  const growthVsPrevious = currentMonth && previousMonth && previousMonth.revenue > 0
    ? ((currentMonth.revenue - previousMonth.revenue) / previousMonth.revenue) * 100
    : null;

  const latestMonthProducts = currentMonth
    ? (data ?? [])
      .filter((item) => Number(item.year) === currentMonth.year && Number(item.month) === currentMonth.month)
      .sort((a, b) => {
        const unitsDiff = (Number(b.totalUnitsSold) || 0) - (Number(a.totalUnitsSold) || 0);
        if (unitsDiff !== 0) return unitsDiff;
        return (Number(b.totalRevenue) || 0) - (Number(a.totalRevenue) || 0);
      })
      .slice(0, 10)
    : [];

  const trendDirection = !previousMonth || !currentMonth
    ? "sin referencia previa"
    : currentMonth.revenue >= previousMonth.revenue
      ? "creciente"
      : "decreciente";

  const selectedRangeLabel = `${normalizedFromMonth} a ${normalizedToMonth}`;

  const setQuickRange = (deltaMonths) => {
    const endMonth = toMonthInputValue(DEFAULT_END);
    setToMonth(endMonth);
    setFromMonth(shiftMonthValue(endMonth, deltaMonths));
  };

  return (
    <div className="space-y-8 text-black">
      <SalesVolumeBranchSelector
        loading={branchesLoading}
        error={branchesError}
        branches={branches}
        selectedBranchId={selectedBranchId}
        onSelect={setSelectedBranchId}
      />

      <SalesVolumeFilters
        fromMonth={fromMonth}
        toMonth={toMonth}
        onFromChange={setFromMonth}
        onToChange={setToMonth}
        selectedRangeLabel={selectedRangeLabel}
        onSetQuickRange={setQuickRange}
      />

      {metricsLoading && (
        <div className="w-full py-8 text-center text-black">Cargando metricas de ventas...</div>
      )}

      {metricsError && (
        <div className="w-full py-8 text-center text-red-500">{metricsError}</div>
      )}

      {!metricsLoading && !metricsError && !hasMetrics && (
        <div className="w-full py-8 text-center text-black">
          No hay datos de ventas para mostrar para el rango seleccionado.
        </div>
      )}

      {!metricsLoading && !metricsError && hasMetrics && (
        <>
          <SalesVolumeKpiCards currentMonth={currentMonth} growthVsPrevious={growthVsPrevious} />
          <SalesVolumeRevenueChart monthly={monthly} />
          <SalesVolumeUnitsChart monthly={monthly} />
          <SalesVolumeTopProductsTable latestMonthProducts={latestMonthProducts} />
          <SalesVolumeInsights
            growthVsPrevious={growthVsPrevious}
            trendDirection={trendDirection}
            selectedRangeLabel={selectedRangeLabel}
          />
        </>
      )}
    </div>
  );
}
