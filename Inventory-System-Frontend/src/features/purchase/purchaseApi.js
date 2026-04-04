import { api } from '../../api/client';

// ============ PURCHASES ENDPOINTS ============

export const createPurchase = async (purchaseData) => {
  try {
    const response = await api.post('/api/purchases', purchaseData);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const getPurchaseById = async (id) => {
  try {
    const response = await api.get(`/api/purchases/${id}`);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const getPurchases = async (filters = {}) => {
  try {
    const response = await api.get('/api/purchases', { params: filters });
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const updatePurchase = async (id, purchaseData) => {
  try {
    const response = await api.put(`/api/purchases/${id}`, purchaseData);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const deletePurchase = async (id) => {
  try {
    const response = await api.delete(`/api/purchases/${id}`);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// ============ PROVIDERS ENDPOINTS ============

export const getProviders = async () => {
  try {
    const response = await api.get('/api/providers');
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const getProviderProducts = async (providerId) => {
  try {
    const response = await api.get(`/api/providers/${providerId}/products`);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};
