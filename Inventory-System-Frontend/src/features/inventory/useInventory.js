
import { useState } from "react";
import { getInventory, getBranches, getBranchInventory } from "./InventoryApi";



export function useInventory() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // handleInventory acepta page y size
  const handleInventory = async ({ page = 0, size = 20 } = {}) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getInventory({ page, size });
      setLoading(false);
      return data;
    } catch (err) {
      setError(err.response?.data?.message || "No se pudo obtener el inventario. Intente de nuevo más tarde.");
      setLoading(false);
      return null;
    }
  };

  // Inventario de una sucursal específica
  const handleBranchInventory = async ({ branchId, page = 0, size = 20 }) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getBranchInventory({ branchId, page, size });
      setLoading(false);
      return data;
    } catch (err) {
      // Si el error es por falta de branchId, mostrar el mensaje personalizado
      const customMsg = err.message === "Seleccione una de las sucursales para ver su inventario."
        ? err.message
        : (err.response?.data?.message || "No se pudo obtener el inventario de la sucursal. Intente de nuevo más tarde.");
      setError(customMsg);
      setLoading(false);
      return null;
    }
  };

  const handleBranches = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getBranches();
      setLoading(false);
      return data;
    } catch (err) {
      setError(err.response?.data?.message || "No se pudieron cargar las sucursales. Intente de nuevo más tarde.");
      setLoading(false);
      return null;
    }
  };

  return { handleInventory, handleBranches, handleBranchInventory, loading, error };
}
