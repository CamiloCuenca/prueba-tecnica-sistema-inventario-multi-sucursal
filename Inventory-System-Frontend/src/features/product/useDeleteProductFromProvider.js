import { useState } from "react";
import { deleteProductFromProvider } from "./productApi";

export function useDeleteProductFromProvider() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const deleteFromProvider = async (productId, providerId) => {
    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const res = await deleteProductFromProvider(productId, providerId);
      setSuccess(res);
      return res;
    } catch (err) {
      setError(err.response?.data?.message || "Error al eliminar producto del proveedor");
      return null;
    } finally {
      setLoading(false);
    }
  };

  return { deleteFromProvider, loading, error, success };
}
