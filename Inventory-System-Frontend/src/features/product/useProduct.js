import { useState } from "react";
import { getProduct } from "./productApi";

export function useProduct() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleProducts = async ({ branchId, productId }) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getProduct({ branchId, productId });
      setLoading(false);
      return data;
    } catch (err) {
      setError(err.response?.data?.message || "Error al obtener los detalles del producto");
      setLoading(false);
      return null;
    }
  };

  return { handleProducts, loading, error };
}
