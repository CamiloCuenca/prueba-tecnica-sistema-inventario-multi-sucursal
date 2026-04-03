import { decodeJWT } from './jwt';

/**
 * Decodifica un JWT y retorna su payload
 * @param {string} token - El token JWT
 * @returns {object} - El payload decodificado
 */
export const decodeToken = (token) => {
  try {
    return decodeJWT(token);
  } catch (error) {
    console.error('Error decodificando token:', error);
    return null;
  }
};

/**
 * Obtiene el branchId del token almacenado en sessionStorage
 * @returns {string|null} - El branchId o null si no existe
 */
export const getBranchIdFromToken = () => {
  const token = sessionStorage.getItem('token') || sessionStorage.getItem('authToken');
  if (!token) {
    console.warn('No token found in sessionStorage');
    return null;
  }

  const decoded = decodeToken(token);
  return decoded?.branchId || decoded?.branch_id || decoded?.branch || null;
};
