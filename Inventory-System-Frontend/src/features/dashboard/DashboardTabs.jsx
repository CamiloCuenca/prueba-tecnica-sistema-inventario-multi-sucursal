import { useEffect, useMemo, useState } from "react";
import ActiveTransfersImpactDashboard from "./ActiveTransfersImpactDashboard";
import BranchPerformanceDashboard from "./branchPerformance/BranchPerformanceDashboard";
import InventoryRotationDashboard from "./InventoryRotationDashboard";
import SalesVolumeDashboard from "./SalesVolumeDashboard";
import { getRoleFromToken } from "../../utils/tokenUtils";
import { useInventoryBehavior } from "./useInventoryBehavior";

export default function DashboardTabs() {
  const [activeTab, setActiveTab] = useState("rotation");
  const role = getRole();

  const {
    data: inventoryData,
    loading: inventoryLoading,
    error: inventoryError,
  } = useInventoryBehavior();

  const tabs = useMemo(() => [
    { id: "rotation", label: "Rotación de inventario" },
    { id: "sales-volume", label: "Volumen mensual de ventas" },
    { id: "active-transfers", label: "Transferencias activas" },
    ...(role === "ADMIN" ? [{ id: "branch-performance", label: "Rendimiento de sucursales" }] : []),
  ], [role]);

  useEffect(() => {
    if (!tabs.some((tab) => tab.id === activeTab)) {
      setActiveTab(tabs[0]?.id ?? "rotation");
    }
  }, [tabs, activeTab]);

  return (
    <div>
      <div className="mb-6 border-b border-border">
        <div className="flex flex-wrap gap-2">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveTab(tab.id)}
              className={`px-4 py-2 rounded-t-md text-sm font-semibold transition-colors ${
                activeTab === tab.id
                  ? "bg-primary text-text"
                  : "bg-surface text-text-secondary hover:text-text"
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {activeTab === "rotation" && (
        <>
          {inventoryError && <div className="text-red-500 mb-4">{inventoryError}</div>}
          <InventoryRotationDashboard data={inventoryData} loading={inventoryLoading} />
        </>
      )}

      {activeTab === "sales-volume" && (
        <SalesVolumeDashboard />
      )}

      {activeTab === "active-transfers" && (
        <ActiveTransfersImpactDashboard />
      )}

      {activeTab === "branch-performance" && role === "ADMIN" && (
        <BranchPerformanceDashboard />
      )}
    </div>
  );
}

function getRole() {
  return getRoleFromToken();
}
