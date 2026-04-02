import { useState } from "react";
import TableInventory from "../features/inventory/TableInventory";
import BranchList from "../features/inventory/BranchList";

export default function InventarioPage() {
  const [selectedBranch, setSelectedBranch] = useState(null);

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4 text-text">Inventario</h1>
      <BranchList onBranchSelect={setSelectedBranch} selectedBranchId={selectedBranch} />

      <p className="text-text">Contenido de Inventario</p>

      <TableInventory branchId={selectedBranch} />
    </div>
  );
}
