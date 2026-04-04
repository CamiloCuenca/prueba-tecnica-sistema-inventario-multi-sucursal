import { api } from '../../api/client';

export const getBranches = async ({ page = 0, size = 10, sort = 'createdAt,desc' } = {}) => {
  try {
    const response = await api.get('/api/branches', {
      params: { page, size, sort },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || error;
  }
};

export const getBranchById = async (id) => {
  try {
    const response = await api.get(`/api/branches/${id}`);
    return response.data;
  } catch (error) {
    throw error.response?.data || error;
  }
};

export const createBranch = async (payload) => {
  try {
    const response = await api.post('/api/branches', payload);
    return response.data;
  } catch (error) {
    throw error.response?.data || error;
  }
};

export const updateBranch = async (id, payload) => {
  try {
    const response = await api.put(`/api/branches/${id}`, payload);
    return response.data;
  } catch (error) {
    throw error.response?.data || error;
  }
};

export const deleteBranch = async (id) => {
  try {
    await api.delete(`/api/branches/${id}`);
    return true;
  } catch (error) {
    throw error.response?.data || error;
  }
};
