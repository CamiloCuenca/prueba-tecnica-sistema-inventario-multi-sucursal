import { useState } from "react";
import { getCsvTemplate } from "./productApi";

export function useCsvTemplate() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [template, setTemplate] = useState(null);

  const fetchTemplate = async () => {
    setLoading(true);
    setError(null);
    setTemplate(null);
    try {
      const res = await getCsvTemplate();
      setTemplate(res);
      return res;
    } catch (err) {
      setError(err.response?.data?.message || "Error al obtener la plantilla");
      return null;
    } finally {
      setLoading(false);
    }
  };

  return { fetchTemplate, loading, error, template };
}
