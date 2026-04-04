import React from 'react';
import TablePaginator from '../../components/TablePaginator';

const BranchesListView = ({
  branches = [],
  pageInfo,
  loading = false,
  search = '',
  onSearchChange = () => {},
  onCreate = () => {},
  onEdit = () => {},
  onDelete = () => {},
  onPageChange = () => {},
  isAdmin = false,
}) => {
  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-CO') + ' ' + date.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <div className="space-y-4">
      {/* Toolbar */}
      <div className="flex gap-2 items-end">
        <div className="flex-1">
          <label className="block text-sm font-medium text-gray-700 mb-1">Buscar sucursal</label>
          <input
            type="text"
            placeholder="Nombre o dirección"
            value={search}
            onChange={(e) => onSearchChange(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        {isAdmin && (
          <button
            onClick={onCreate}
            className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition"
          >
            Crear sucursal
          </button>
        )}
      </div>

      {/* Table */}
      <div className="overflow-x-auto border rounded-md">
        <table className="w-full text-sm">
          <thead className="bg-gray-100 border-b">
            <tr>
              <th className="px-6 py-3 text-left font-semibold text-gray-700">Nombre</th>
              <th className="px-6 py-3 text-left font-semibold text-gray-700">Dirección</th>
              <th className="px-6 py-3 text-left font-semibold text-gray-700">Latitud</th>
              <th className="px-6 py-3 text-left font-semibold text-gray-700">Longitud</th>
              <th className="px-6 py-3 text-left font-semibold text-gray-700">Fecha creación</th>
              {isAdmin && <th className="px-6 py-3 text-left font-semibold text-gray-700">Acciones</th>}
            </tr>
          </thead>
          <tbody className="divide-y">
            {loading ? (
              <tr>
                <td colSpan={isAdmin ? 6 : 5} className="px-6 py-8 text-center text-gray-500">
                  Cargando...
                </td>
              </tr>
            ) : branches.length === 0 ? (
              <tr>
                <td colSpan={isAdmin ? 6 : 5} className="px-6 py-8 text-center text-gray-500">
                  No hay sucursales
                </td>
              </tr>
            ) : (
              branches.map((branch) => (
                <tr key={branch.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 font-medium text-gray-900">{branch.name}</td>
                  <td className="px-6 py-4 text-gray-600">{branch.address || '-'}</td>
                  <td className="px-6 py-4 text-gray-600">{branch.latitude !== undefined ? branch.latitude : '-'}</td>
                  <td className="px-6 py-4 text-gray-600">{branch.longitude !== undefined ? branch.longitude : '-'}</td>
                  <td className="px-6 py-4 text-gray-600">{formatDate(branch.createdAt)}</td>
                  {isAdmin && (
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => onEdit(branch)}
                        className="mr-2 px-3 py-1 bg-blue-600 text-white text-xs rounded hover:bg-blue-700 transition"
                      >
                        Editar
                      </button>
                      <button
                        onClick={() => onDelete(branch)}
                        className="px-3 py-1 bg-red-600 text-white text-xs rounded hover:bg-red-700 transition"
                      >
                        Eliminar
                      </button>
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Paginator */}
      {branches.length > 0 && (
        <TablePaginator
          pageInfo={pageInfo}
          onPageChange={onPageChange}
        />
      )}
    </div>
  );
};

export default BranchesListView;
