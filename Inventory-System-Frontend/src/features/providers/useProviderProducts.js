import { useCallback, useEffect, useState } from 'react';
import { getProviderProducts, mapProviderApiError, normalizePageResponse, normalizeProviderProduct } from './providersApi';

const defaultPageInfo = {
  currentPage: 0,
  totalPages: 0,
  totalElements: 0,
  isFirst: true,
  isLast: true,
};

export function useProviderProducts(providerId) {
  const [products, setProducts] = useState([]);
  const [pageInfo, setPageInfo] = useState(defaultPageInfo);
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [search, setSearch] = useState('');
  const [active, setActive] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const loadProducts = useCallback(async () => {
    if (!providerId) return;

    setLoading(true);
    setError(null);

    try {
      const activeValue = active === '' ? undefined : active === 'true';
      const data = await getProviderProducts(providerId, {
        page,
        size,
        q: search.trim() || undefined,
        active: activeValue,
      });

      const normalized = normalizePageResponse(data, normalizeProviderProduct);
      setProducts(normalized.content);
      setPageInfo(normalized.pageInfo);
    } catch (err) {
      const mapped = mapProviderApiError(err, 'No se pudieron cargar los productos del proveedor');
      setError(mapped);
      setProducts([]);
      setPageInfo(defaultPageInfo);
    } finally {
      setLoading(false);
    }
  }, [active, page, providerId, search, size]);

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  const handleSearchChange = (value) => {
    setSearch(value);
    setPage(0);
  };

  const handleActiveChange = (value) => {
    setActive(value);
    setPage(0);
  };

  return {
    products,
    pageInfo,
    page,
    setPage,
    size,
    search,
    setSearch: handleSearchChange,
    active,
    setActive: handleActiveChange,
    loading,
    error,
    refetch: loadProducts,
  };
}