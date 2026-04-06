import { useState } from "react";
import { getById } from "./productApi";

export function useProductById() {
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchProduct = async (id) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getById(id);
      setProduct(data);
      setLoading(false);
      return data;
    } catch (err) {
      setError(err.response?.data?.message || "Error al obtener el producto");
      setLoading(false);
      setProduct(null);
      return null;
    }
  };

  return { product, fetchProduct, loading, error };
}