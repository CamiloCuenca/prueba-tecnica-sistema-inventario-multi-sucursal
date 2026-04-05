import { useEffect, useState } from 'react';
import { getProviderById, mapProviderApiError, normalizeProvider } from './providersApi';

export function useProviderDetail(providerId) {
  const [provider, setProvider] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!providerId) {
      setProvider(null);
      return;
    }

    const loadProvider = async () => {
      setLoading(true);
      setError(null);

      try {
        const data = await getProviderById(providerId);
        setProvider(normalizeProvider(data));
      } catch (err) {
        const mapped = mapProviderApiError(err, 'No se pudo cargar el proveedor');
        setError(mapped);
        setProvider(null);
      } finally {
        setLoading(false);
      }
    };

    loadProvider();
  }, [providerId]);

  return {
    provider,
    loading,
    error,
  };
}