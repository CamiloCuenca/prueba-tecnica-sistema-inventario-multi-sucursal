import { useCallback, useEffect, useMemo, useState } from 'react';
import { getUsers } from './usersApi';

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

export function useUsers({ page = 0, size = 10, roleFilter = '', search = '' } = {}) {
  const [users, setUsers] = useState([]);
  const [pageInfo, setPageInfo] = useState(defaultPageInfo);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const loadUsers = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getUsers({ page, size, sort: ['createdAt,desc'] });
      const normalized = normalizePage(data, page);
      setUsers(normalized.content);
      setPageInfo(normalized.pageInfo);
    } catch (err) {
      setError(getErrorMessage(err, 'Error al cargar usuarios'));
      setUsers([]);
      setPageInfo(defaultPageInfo);
    } finally {
      setLoading(false);
    }
  }, [page, size]);

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  const filteredUsers = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase();

    return users.filter((user) => {
      const matchesRole = roleFilter ? user.role === roleFilter : true;
      const matchesSearch = !normalizedSearch
        ? true
        : String(user?.name || '').toLowerCase().includes(normalizedSearch) ||
          String(user?.email || '').toLowerCase().includes(normalizedSearch);

      return matchesRole && matchesSearch;
    });
  }, [users, roleFilter, search]);

  return {
    users: filteredUsers,
    pageInfo,
    loading,
    error,
    refetch: loadUsers,
  };
}
