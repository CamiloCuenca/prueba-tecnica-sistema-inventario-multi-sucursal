import { useEffect, useState } from "react";
import { getBranchPerformanceMetrics } from "./branchPerformanceApi";

export function useBranchPerformance() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    getBranchPerformanceMetrics()
      .then((rows) => {
        setData(Array.isArray(rows) ? rows : []);
        setError(null);
      })
      .catch((err) => {
        setError(err.message);
        setData([]);
      })
      .finally(() => setLoading(false));
  }, []);

  return { data, loading, error };
}
