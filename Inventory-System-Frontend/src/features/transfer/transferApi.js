import { api } from '../../api/client';

/**
 * Solicita una transferencia de productos entre sucursales
 * @param {Object} transferData - Datos de la transferencia
 * @param {UUID} transferData.originBranchId - ID de la sucursal origen
 * @param {UUID} transferData.destinationBranchId - ID de la sucursal destino
 * @param {Array} transferData.items - Array de items a transferir
 * @param {UUID} transferData.items[].productId - ID del producto
 * @param {number} transferData.items[].quantity - Cantidad a transferir
 * @returns {Promise<Object>} Respuesta con ID y estado de la transferencia
 */
export const requestTransfer = async (transferData) => {
  try {
    const response = await api.post('/api/transfers', transferData);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

/**
 * Obtiene el inventario de una sucursal específica (disponible para transferir)
 * @param {UUID} branchId - ID de la sucursal
 * @param {Object} filters - Filtros opcionales
 * @param {number} filters.page - Número de página (default: 0)
 * @param {number} filters.size - Tamaño de página (default: 20)
 * @returns {Promise<Object>} Inventario paginado de la sucursal
 */
export const getBranchInventoryForTransfer = async (branchId, filters = {}) => {
  try {
    const { page = 0, size = 20 } = filters;
    const response = await api.get(`/api/branches/${branchId}/inventory`, {
      params: { page, size },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

/**
 * Obtiene todas las sucursales disponibles en el sistema
 * @returns {Promise<Array>} Lista de sucursales
 */
export const getAllBranches = async () => {
  try {
    const response = await api.get('/api/branches');
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const getIncomingTransfers = async ({ page = 0, size = 10, sort = [], branchId } = {}) => {
  try {
    const response = await api.get('/api/transfers/incoming', {
      params: {
        page,
        size,
        sort,
        ...(branchId ? { branchId } : {}),
      },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const getOutgoingTransfers = async ({ page = 0, size = 10, sort = [], branchId } = {}) => {
  try {
    const response = await api.get('/api/transfers/outgoing', {
      params: {
        page,
        size,
        sort,
        ...(branchId ? { branchId } : {}),
      },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const getTransferById = async (id) => {
  try {
    const response = await api.get(`/api/transfers/${id}`);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const prepareTransfer = async (id, payload) => {
  try {
    const response = await api.post(`/api/transfers/${id}/prepare`, payload);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const dispatchTransfer = async (id, payload) => {
  try {
    const response = await api.post(`/api/transfers/${id}/dispatch`, payload);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};
