import { useState } from "react";
import { importProductsFromCsv } from "./productApi";

export function useImportProductsFromCsv() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const importCsv = async (file) => {
    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const res = await importProductsFromCsv(file);
      setSuccess(res);
      return res;
    } catch (err) {
      setError(err.response?.data?.message || "Error al importar productos");
      return null;
    } finally {
      setLoading(false);
    }
  };

  return { importCsv, loading, error, success };
}
