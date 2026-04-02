import { useState, useEffect } from "react";
import { getBranchInventory } from "../inventory/InventoryApi";
import { postSale } from "./salesApi";
import { decodeJWT } from "../../utils/jwt";

export function useSales() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Obtiene branchId del token
  const getBranchId = () => {
    const token = sessionStorage.getItem("token");
    const payload = decodeJWT(token);
    return payload?.branchId || null;
  };

  // Carga productos de la branch
  const fetchProducts = async () => {
    setLoading(true);
    setError(null);
    try {
      const branchId = getBranchId();
      const res = await getBranchInventory({ branchId });
      setProducts(res?.content || []);
    } catch (err) {
      setError("Error al cargar productos de la sucursal");
    } finally {
      setLoading(false);
    }
  };

  // Registra la venta
  const registerSale = async (items, discountTotal = 0) => {
    setLoading(true);
    setError(null);
    try {
      const branchId = getBranchId();
      const data = await postSale({ branchId, items, discountTotal });
      return data;
    } catch (err) {
      setError(err.response?.data?.message || "Error al registrar la venta");
      return null;
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
    // eslint-disable-next-line
  }, []);

  return { products, loading, error, registerSale };
}
