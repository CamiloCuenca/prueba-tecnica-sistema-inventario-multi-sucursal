
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
      setError(err.response?.data?.message || "Error al obtener el inventario");
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
      setError(err.response?.data?.message || "Error al obtener el inventario de la sucursal");
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
      setError(err.response?.data?.message || "Error al obtener las sucursales");
      setLoading(false);
      return null;
    }
  };

  return { handleInventory, handleBranches, handleBranchInventory, loading, error };
}
