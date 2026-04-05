import { api } from '../../api/client';

export const getUsers = async ({ page = 0, size = 10, sort = ['createdAt,desc'] } = {}) => {
  try {
    const response = await api.get('/admin/users', {
      params: { page, size, sort },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || error;
  }
};

export const getUserById = async (id) => {
  try {
    const response = await api.get(`/admin/users/${id}`);
    return response.data;
  } catch (error) {
    throw error.response?.data || error;
  }
};

export const createUser = async (payload) => {
  try {
    const response = await api.post('/admin/users', payload);
    return response.data;
  } catch (error) {
    throw error.response?.data || error;
  }
};

export const updateUser = async (id, payload) => {
  try {
    const response = await api.put(`/admin/users/${id}`, payload);
    return response.data;
  } catch (error) {
    throw error.response?.data || error;
  }
};

export const deleteUser = async (id) => {
  try {
    await api.delete(`/admin/users/${id}`);
    return true;
  } catch (error) {
    throw error.response?.data || error;
  }
};

export const getBranchesForUsers = async () => {
  try {
    const response = await api.get('/api/branches');
    return response.data;
  } catch (error) {
    throw error.response?.data || error;
  }
};
