import { useState } from "react";
import { deleteProductFromBranch } from "./productApi";

export function useDeleteProductFromBranch() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const deleteFromBranch = async (branchId, productId) => {
    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const res = await deleteProductFromBranch(branchId, productId);
      setSuccess(res);
      return res;
    } catch (err) {
      setError(err.response?.data?.message || "Error al eliminar producto de la sucursal");
      return null;
    } finally {
      setLoading(false);
    }
  };

  return { deleteFromBranch, loading, error, success };
}
