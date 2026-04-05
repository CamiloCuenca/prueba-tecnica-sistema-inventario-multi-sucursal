import { useCallback, useEffect, useState } from 'react';
import { getProviders, mapProviderApiError, normalizePageResponse, normalizeProvider } from './providersApi';

const defaultPageInfo = {
  currentPage: 0,
  totalPages: 0,
  totalElements: 0,
  isFirst: true,
  isLast: true,
};

const SEARCH_FETCH_SIZE = 1000;

const normalizeSearchValue = (value) => String(value || '').trim().toLowerCase();

const matchesProviderSearch = (provider, searchTerm) => {
  if (!searchTerm) return true;

  const normalizedName = normalizeSearchValue(provider.name);
  const normalizedContact = normalizeSearchValue(provider.contactInfo);

  return normalizedName.includes(searchTerm) || normalizedContact.includes(searchTerm);
};

export function useProviderList() {
  const [providers, setProviders] = useState([]);
  const [pageInfo, setPageInfo] = useState(defaultPageInfo);
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const loadProviders = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const searchTerm = normalizeSearchValue(search);

      if (searchTerm) {
        const data = await getProviders({ page: 0, size: SEARCH_FETCH_SIZE });
        const normalized = normalizePageResponse(data, normalizeProvider);
        const filtered = normalized.content.filter((provider) => matchesProviderSearch(provider, searchTerm));
        const start = page * size;
        const paginated = filtered.slice(start, start + size);

        setProviders(paginated);
        setPageInfo({
          currentPage: page,
          totalPages: filtered.length > 0 ? Math.ceil(filtered.length / size) : 0,
          totalElements: filtered.length,
          isFirst: page === 0,
          isLast: filtered.length === 0 ? true : page >= Math.ceil(filtered.length / size) - 1,
        });
        return;
      }

      const data = await getProviders({ page, size });
      const normalized = normalizePageResponse(data, normalizeProvider);
      setProviders(normalized.content);
      setPageInfo(normalized.pageInfo);
    } catch (err) {
      const mapped = mapProviderApiError(err, 'No se pudieron cargar los proveedores');
      setError(mapped);
      setProviders([]);
      setPageInfo(defaultPageInfo);
    } finally {
      setLoading(false);
    }
  }, [page, search, size]);

  useEffect(() => {
    loadProviders();
  }, [loadProviders]);

  const handleSearchChange = (value) => {
    setSearch(value);
    setPage(0);
  };

  return {
    providers,
    pageInfo,
    page,
    setPage,
    size,
    search,
    setSearch: handleSearchChange,
    loading,
    error,
    refetch: loadProviders,
  };
}