import { useState } from "react";
import { listGet } from "./productApi";

export function useProductList(initialPage = 0, initialSize = 20, initialSort = []) {
  const [products, setProducts] = useState([]);
  const [pageInfo, setPageInfo] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchProducts = async ({ page = initialPage, size = initialSize, sort = initialSort } = {}) => {
    setLoading(true);
    setError(null);
    try {
      const res = await listGet({ page, size, sort });
      setProducts(res.content || []);
      setPageInfo(res);
    } catch (err) {
      setError(err.response?.data?.message || "Error al listar productos");
    } finally {
      setLoading(false);
    }
  };

  return { products, pageInfo, loading, error, fetchProducts };
}