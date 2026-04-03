import { useEffect, useState } from "react";
import { getActiveTransfersImpact } from "./activeTransfersApi";

export function useActiveTransfersImpact(branchId) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!branchId) {
      setData([]);
      setError(null);
      setLoading(false);
      return;
    }

    setLoading(true);
    getActiveTransfersImpact({ branchId })
      .then((rows) => {
        setData(Array.isArray(rows) ? rows : []);
        setError(null);
      })
      .catch((err) => {
        setError(err.message);
        setData([]);
      })
      .finally(() => setLoading(false));
  }, [branchId]);

  return { data, loading, error };
}
