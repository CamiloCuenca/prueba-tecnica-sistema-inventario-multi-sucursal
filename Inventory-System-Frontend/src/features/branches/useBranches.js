import { useCallback, useEffect, useMemo, useState } from 'react';
import { getBranches } from './branchesApi';

const defaultPageInfo = {
  currentPage: 0,
  totalPages: 0,
  totalElements: 0,
  isFirst: true,
  isLast: true,
};

const normalizePage = (data, page) => ({
  content: data?.content || [],
  pageInfo: {
    currentPage: data?.number ?? page,
    totalPages: data?.totalPages ?? 0,
    totalElements: data?.totalElements ?? 0,
    isFirst: data?.first ?? page === 0,
    isLast: data?.last ?? true,
  },
});

const getErrorMessage = (err, fallbackMessage) => {
  const status = err?.status || err?.response?.status;
  if (status === 401) return 'Por favor inicia sesion.';
  if (status === 403) return 'No tienes permisos para esta accion.';
  if (err?.message) return err.message;
  if (err?.error) return err.error;
  return fallbackMessage;
};

export function useBranches({ page = 0, size = 10, search = '' } = {}) {
  const [branches, setBranches] = useState([]);
  const [pageInfo, setPageInfo] = useState(defaultPageInfo);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const loadBranches = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getBranches({ page, size, sort: 'createdAt,desc' });
      const normalized = normalizePage(data, page);
      setBranches(normalized.content);
      setPageInfo(normalized.pageInfo);
    } catch (err) {
      setError(getErrorMessage(err, 'Error al cargar sucursales'));
      setBranches([]);
      setPageInfo(defaultPageInfo);
    } finally {
      setLoading(false);
    }
  }, [page, size]);

  useEffect(() => {
    loadBranches();
  }, [loadBranches]);

  const filteredBranches = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase();
    if (!normalizedSearch) return branches;
    
    return branches.filter(branch =>
      branch.name?.toLowerCase().includes(normalizedSearch) ||
      branch.address?.toLowerCase().includes(normalizedSearch)
    );
  }, [branches, search]);

  const refetch = useCallback(() => {
    loadBranches();
  }, [loadBranches]);

  return {
    branches: filteredBranches,
    pageInfo,
    loading,
    error,
    refetch,
  };
}
