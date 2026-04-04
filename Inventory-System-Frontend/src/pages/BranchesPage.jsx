import React from 'react';
import { BranchesTabs } from '../features/branches';
import { decodeJWT } from '../utils/jwt';

export default function BranchesPage() {
  const token = sessionStorage.getItem('token') || sessionStorage.getItem('authToken');
  const role = decodeJWT(token)?.role;
  const isAdmin = role === 'ADMIN';
  const isManager = role === 'MANAGER';

  // Only ADMIN and MANAGER can access branches list
  if (!isAdmin && !isManager) {
    return (
      <div className="p-6 bg-red-50 border border-red-300 rounded-md">
        <h2 className="text-lg font-semibold text-red-800">Acceso denegado</h2>
        <p className="text-red-700 mt-2">
          No tienes permisos para acceder a la gestión de sucursales. Contacta a un administrador.
        </p>
      </div>
    );
  }

  return <BranchesTabs />;
}
