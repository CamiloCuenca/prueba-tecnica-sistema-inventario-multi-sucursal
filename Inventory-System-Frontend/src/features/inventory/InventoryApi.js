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
