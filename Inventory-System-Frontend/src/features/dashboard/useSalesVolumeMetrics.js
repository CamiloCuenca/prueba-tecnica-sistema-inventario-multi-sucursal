import { useEffect, useState } from "react";
import { getSalesVolumeMetrics } from "./salesVolumeApi";

export function useSalesVolumeMetrics(filters = {}) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    getSalesVolumeMetrics(filters)
      .then((rows) => {
        setData(rows);
        setError(null);
      })
      .catch((err) => {
        setError(err.message);
        setData([]);
      })
      .finally(() => setLoading(false));
  }, [filters.branchId, filters.productId, filters.from, filters.to, filters.size]);

  return { data, loading, error };
}
