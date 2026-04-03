import { useState, useEffect } from 'react';
import { getPurchases } from './purchaseApi';
import { getBranchIdFromToken } from '../../utils/tokenUtils';

export const usePurchases = (page = 0, size = 20) => {
  const [purchases, setPurchases] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [pageInfo, setPageInfo] = useState({
    currentPage: 0,
    pageSize: 20,
    totalPages: 0,
    totalElements: 0,
    isFirst: true,
    isLast: true,
  });

  const branchId = getBranchIdFromToken();

  const fetchPurchases = async (pageNum = 0) => {
    setLoading(true);
    setError(null);
    try {
      if (!branchId) {
        throw new Error('No se pudo obtener el ID de la sucursal del token');
      }

      const data = await getPurchases({
        branchId,
        page: pageNum,
        size,
      });

      // Normalizar respuesta paginada
      setPurchases(data?.content || []);
      setPageInfo({
        currentPage: data?.number || pageNum,
        pageSize: data?.size || size,
        totalPages: data?.totalPages || 0,
        totalElements: data?.totalElements || 0,
        isFirst: data?.first || false,
        isLast: data?.last || false,
      });
    } catch (err) {
      setError(err?.message || 'Error al cargar las compras');
      console.error('Error fetching purchases:', err);
      setPurchases([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPurchases(page);
  }, [page, branchId]);

  const goToPage = (pageNumber) => {
    fetchPurchases(pageNumber);
  };

  return {
    purchases,
    loading,
    error,
    pageInfo,
    goToPage,
    branchId,
  };
};
