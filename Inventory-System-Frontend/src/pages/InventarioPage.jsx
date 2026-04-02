import TableInventory from "../features/inventory/TableInventory";
import BranchList from "../features/inventory/BranchList";

export default function InventarioPage() {
  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4 text-text">Inventario</h1>
      <BranchList />

      <p className="text-text">Contenido de Inventario</p>

       <TableInventory />
    </div>
  );
}
