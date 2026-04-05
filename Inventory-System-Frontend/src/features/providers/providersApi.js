import { api } from '../../api/client';

export const normalizeProvider = (provider = {}) => ({
  id: String(provider.id || provider.providerId || ''),
  name: provider.name || '',
  contactInfo: provider.contactInfo || provider.contact_info || '',
  createdAt: provider.createdAt || null,
  updatedAt: provider.updatedAt || null,
});

export const normalizeProviderProduct = (product = {}) => ({
  id: String(product.productId || product.id || ''),
  productId: String(product.productId || product.id || ''),
  name: product.name || '',
  sku: product.sku || '',
  category: product.category || '',
  active: Boolean(product.active),
  defaultReferencePrice: product.defaultReferencePrice ?? product.default_reference_price ?? null,
  stock: product.stock ?? 0,
});

export const normalizePageResponse = (response, itemMapper = (item) => item) => {
  if (Array.isArray(response)) {
    return {
      content: response.map(itemMapper),
      pageInfo: {
        currentPage: 0,
        totalPages: 1,
        totalElements: response.length,
        isFirst: true,
        isLast: true,
      },
    };
  }

  if (Array.isArray(response?.content)) {
    return {
      content: response.content.map(itemMapper),
      pageInfo: {
        currentPage: response.number ?? 0,
        totalPages: response.totalPages ?? 0,
        totalElements: response.totalElements ?? response.content.length ?? 0,
        isFirst: response.first ?? true,
        isLast: response.last ?? true,
      },
    };
  }

  return {
    content: [],
    pageInfo: {
      currentPage: 0,
      totalPages: 0,
      totalElements: 0,
      isFirst: true,
      isLast: true,
    },
  };
};

export const mapProviderApiError = (error, fallbackMessage = 'Error en proveedores') => {
  const status = error?.status || error?.response?.status || null;
  const fieldErrors = {};
  const validationErrors = error?.errors || error?.violations || error?.fieldErrors;

  if (Array.isArray(validationErrors)) {
    validationErrors.forEach((item) => {
      if (item?.field) {
        fieldErrors[item.field] = item?.message || 'Valor invalido';
      }
    });
  } else if (validationErrors && typeof validationErrors === 'object') {
    Object.entries(validationErrors).forEach(([field, message]) => {
      fieldErrors[field] = Array.isArray(message) ? message[0] : String(message);
    });
  }

  if (status === 401) return { status, message: 'Por favor inicia sesion.', fieldErrors };
  if (status === 403) return { status, message: 'No tiene permisos para esta accion.', fieldErrors };
  if (status === 404) return { status, message: 'Proveedor no encontrado.', fieldErrors };
  if (status === 500) return { status, message: 'Error del servidor, intentelo mas tarde.', fieldErrors };

  if (typeof error === 'string') return { status, message: error, fieldErrors };
  if (error?.message) return { status, message: error.message, fieldErrors };
  if (error?.error) return { status, message: error.error, fieldErrors };

  return { status, message: fallbackMessage, fieldErrors };
};

export const getProviders = async ({ page = 0, size = 10, sort = 'name,asc', q } = {}) => {
  try {
    const response = await api.get('/api/providers', {
      params: {
        page,
        size,
        sort,
        ...(q ? { q } : {}),
      },
    });

    return response.data;
  } catch (error) {
    throw error.response?.data || error;
  }
};

export const getProviderById = async (id) => {
  try {
    const response = await api.get(`/api/providers/${id}`);
    return response.data;
  } catch (error) {
    throw error.response?.data || error;
  }
};

export const createProvider = async (payload) => {
  try {
    const response = await api.post('/api/providers', payload);
    return response.data;
  } catch (error) {
    throw error.response?.data || error;
  }
};

export const updateProvider = async (id, payload) => {
  try {
    const response = await api.put(`/api/providers/${id}`, payload);
    return response.data;
  } catch (error) {
    throw error.response?.data || error;
  }
};

export const deleteProvider = async (id) => {
  try {
    await api.delete(`/api/providers/${id}`);
    return true;
  } catch (error) {
    throw error.response?.data || error;
  }
};

export const getProviderProducts = async (id, { page = 0, size = 10, sort = 'name,asc', q, active } = {}) => {
  try {
    const response = await api.get(`/api/providers/${id}/products`, {
      params: {
        page,
        size,
        sort,
        ...(q ? { q } : {}),
        ...(active === '' || active === undefined ? {} : { active }),
      },
    });

    return response.data;
  } catch (error) {
    throw error.response?.data || error;
  }
};