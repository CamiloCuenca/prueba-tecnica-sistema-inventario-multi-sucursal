import { useMemo, useState } from 'react';
import TablePaginator from '../../../components/TablePaginator';

const normalizeText = (value) => String(value ?? '').toLowerCase();

export default function TransfersTable({
  title,
  rows,
  loading,
  error,
  pageInfo,
  onPageChange,
  showAction = false,
  renderAction,
}) {
  const [search, setSearch] = useState('');

  const filteredRows = useMemo(() => {
    const normalizedSearch = normalizeText(search).trim();
    if (!normalizedSearch) return rows;

    return rows.filter((row) => {
      return [row.creado, row.estado, row.origen, row.destino, row.despachado, row.llegadaEstimada, row.recibido, row.items, row.id]
        .some((value) => normalizeText(value).includes(normalizedSearch));
    });
  }, [rows, search]);

  return (
    <div className="bg-white rounded-lg shadow p-4 space-y-4">
      <h3 className="text-base font-semibold text-gray-900">{title}</h3>

      <div className="w-full max-w-md">
        <label className="mb-1 block text-sm font-medium text-gray-700">Buscar</label>
        <input
          type="search"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Buscar por estado, origen, destino o ID"
          className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
        />
      </div>

      {loading ? (
        <div className="text-center py-8 text-gray-500">Cargando transferencias...</div>
      ) : error ? (
        <div className="text-center py-8 text-red-500">{error}</div>
      ) : filteredRows.length === 0 ? (
        <div className="text-center py-8 text-gray-600">No hay transferencias activas para mostrar.</div>
      ) : (
        <div className="overflow-x-auto border rounded-lg">
          <table className="w-full text-sm">
            <thead className="bg-gray-100">
              <tr>
                <th className="text-left px-3 py-2">Creado</th>
                <th className="text-left px-3 py-2">Estado</th>
                <th className="text-left px-3 py-2">Origen</th>
                <th className="text-left px-3 py-2">Destino</th>
                <th className="text-left px-3 py-2">Despachado</th>
                <th className="text-left px-3 py-2">Llegada Estimada</th>
                <th className="text-left px-3 py-2">Recibido</th>
                <th className="text-left px-3 py-2">Items</th>
                <th className="text-left px-3 py-2">ID</th>
                {showAction && <th className="text-left px-3 py-2">Acción</th>}
              </tr>
            </thead>
            <tbody>
              {filteredRows.map((row) => (
                <tr key={row.id} className="border-t">
                  <td className="px-3 py-2 whitespace-nowrap">{row.creado}</td>
                  <td className="px-3 py-2">{row.estado}</td>
                  <td className="px-3 py-2">{row.origen}</td>
                  <td className="px-3 py-2">{row.destino}</td>
                  <td className="px-3 py-2 whitespace-nowrap">{row.despachado}</td>
                  <td className="px-3 py-2 whitespace-nowrap">{row.llegadaEstimada}</td>
                  <td className="px-3 py-2 whitespace-nowrap">{row.recibido}</td>
                  <td className="px-3 py-2">{row.items}</td>
                  <td className="px-3 py-2">{row.id}</td>
                  {showAction && <td className="px-3 py-2">{renderAction?.(row)}</td>}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <TablePaginator
        page={pageInfo.currentPage}
        totalPages={pageInfo.totalPages}
        onPageChange={onPageChange}
        isFirst={pageInfo.isFirst}
        isLast={pageInfo.isLast}
      />
    </div>
  );
}
