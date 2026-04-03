import { useState, useEffect } from "react";
import { getInventoryBehaviorMetrics } from "./dashboardApi";

export function useInventoryBehavior() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    getInventoryBehaviorMetrics()
      .then((json) => {
        setData(json);
        setError(null);
      })
      .catch((err) => {
        setError(err.message);
        setData(null);
      })
      .finally(() => setLoading(false));
  }, []);

  return { data, loading, error };
}
