import { useEffect, useState } from "react";
import Card from "../../components/Card";
import { decodeJWT } from "../../utils/jwt";
import { useInventory } from "../inventory/useInventory";
import SalesVolumeBranchSelector from "./components/SalesVolumeBranchSelector";
import { useActiveTransfersImpact } from "./useActiveTransfersImpact";

function groupProductsInTransit(transfers) {
  const grouped = new Map();

  transfers.forEach((transfer) => {
    (transfer.items ?? []).forEach((item) => {
      const productId = item.productId;
      if (!productId) return;

      const current = grouped.get(productId) ?? {
        productId,
        totalQuantity: 0,
      };

      current.totalQuantity += Number(item.quantity) || 0;
      grouped.set(productId, current);
    });
  });

  return Array.from(grouped.values()).sort((a, b) => b.totalQuantity - a.totalQuantity);
}

function numberFormat(value) {
  if (value == null) return "-";
  return value.toLocaleString("es-MX");
}

export default function ActiveTransfersImpactDashboard() {
  const { handleBranches, loading: branchesLoading, error: branchesError } = useInventory();
  const [branches, setBranches] = useState([]);
  const [selectedBranchId, setSelectedBranchId] = useState(null);

  useEffect(() => {
    handleBranches().then((res) => {
      if (Array.isArray(res)) {
        setBranches(res);
      }
    });
    // eslint-disable-next-line
  }, []);

  useEffect(() => {
    const token = sessionStorage.getItem("token");
    if (token) {
      const payload = decodeJWT(token);
      setSelectedBranchId(payload?.branchId || null);
    }
  }, []);

  useEffect(() => {
    if (!selectedBranchId && branches.length > 0) {
      setSelectedBranchId(branches[0].id);
    }
  }, [branches, selectedBranchId]);

  const { data, loading, error } = useActiveTransfersImpact(selectedBranchId);

  if (loading) {
    return <div className="w-full py-8 text-center text-black">Cargando transferencias activas...</div>;
  }

  if (error) {
    return <div className="w-full py-8 text-center text-red-500">{error}</div>;
  }

  if (!data || data.length === 0) {
    return <div className="w-full py-8 text-center text-black">No hay transferencias activas para mostrar.</div>;
  }

  const groupedProducts = groupProductsInTransit(data);

  return (
    <div className="space-y-8 text-black">
      <SalesVolumeBranchSelector
        loading={branchesLoading}
        error={branchesError}
        branches={branches}
        selectedBranchId={selectedBranchId}
        onSelect={setSelectedBranchId}
      />

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <Card className="border border-border bg-white p-4 text-black">
          <h2 className="mb-4 text-lg font-bold text-black">Productos en tránsito</h2>
          <div className="overflow-x-auto rounded-lg border border-border">
            <table className="min-w-full bg-white text-black">
              <thead>
                <tr className="text-left text-sm text-black">
                  <th className="border-b border-border px-4 py-3">productId</th>
                  <th className="border-b border-border px-4 py-3">Cantidad total en tránsito</th>
                </tr>
              </thead>
              <tbody>
                {groupedProducts.map((item) => (
                  <tr key={item.productId}>
                    <td className="border-b border-border px-4 py-3 break-all">{item.productId}</td>
                    <td className="border-b border-border px-4 py-3">{numberFormat(item.totalQuantity)}</td>
                  </tr>
                ))}
                {groupedProducts.length === 0 && (
                  <tr>
                    <td colSpan={2} className="px-4 py-6 text-center text-black">
                      No hay productos en tránsito.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </Card>

        <Card className="border border-border bg-white p-4 text-black">
          <h2 className="mb-4 text-lg font-bold text-black">Transferencias activas</h2>
          <div className="space-y-3">
            {data.map((transfer) => (
              <div key={transfer.transferId} className="rounded-lg border border-border bg-white p-3">
                <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
                  <span className="font-semibold break-all">{transfer.transferId}</span>
                  <span className="rounded-full border border-border px-3 py-1 text-sm font-semibold text-black">
                    {transfer.status}
                  </span>
                </div>
                <div className="mt-2 text-sm text-black">
                      <p>
                        Origen: <span className="font-medium">{transfer.originBranchName ?? transfer.originBranchId}</span>
                      </p>
                      <p>
                        Destino: <span className="font-medium">{transfer.destinationBranchName ?? transfer.destinationBranchId}</span>
                      </p>
                      <p>Productos: <span className="font-medium">{(transfer.items ?? []).length}</span></p>
                </div>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
}
