// Inventario de una sucursal específica
export const getBranchInventory = async ({ branchId, page = 0, size = 20 }) => {
  if (!branchId) throw new Error("Seleccione una de las sucursales para ver su inventario.");
  const response = await api.get(`/api/branches/${branchId}/inventory`, {
    params: { page, size },
  });
  return response.data;
};
import { api } from "../../api/client";

// Recibe page y size como parámetros para paginación
export const getInventory = async ({ page = 0, size = 20 } = {}) => {
  const response = await api.get("/api/my/catalog", {
    params: { page, size },
  });
  return response.data;
};

export const getBranches = async ( ) => {
  const response = await api.get("/api/branches");
  return response.data;
};
