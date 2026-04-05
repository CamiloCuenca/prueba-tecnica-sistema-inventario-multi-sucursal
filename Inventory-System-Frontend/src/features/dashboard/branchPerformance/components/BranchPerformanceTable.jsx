import { useMemo, useState } from 'react';
import Card from "../../../../components/Card";
import { currencyFormat } from "../branchPerformanceUtils";

const normalizeText = (value) => String(value ?? '').toLowerCase();

export default function BranchPerformanceTable({ data }) {
  const [search, setSearch] = useState('');

  const filteredData = useMemo(() => {
    const normalizedSearch = normalizeText(search).trim();
    if (!normalizedSearch) return data;

    return data.filter((item) =>
      normalizeText(item.branchName).includes(normalizedSearch)
    );
  }, [data, search]);

  return (
    <Card className="border border-border bg-white p-4 text-text">
      <h2 className="mb-4 text-lg font-bold text-text">Tabla de sucursales</h2>
      <div className="mb-4 w-full max-w-md">
        <label className="mb-1 block text-sm font-medium text-text">Buscar</label>
        <input
          type="search"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Buscar por sucursal"
          className="w-full rounded border border-border bg-white px-3 py-2 text-sm text-text focus:outline-none focus:ring-2 focus:ring-primary/20"
        />
      </div>
      <div className="overflow-x-auto rounded-lg border border-border">
        <table className="min-w-full bg-surface text-text">
          <thead>
            <tr className="text-left text-sm text-text-secondary">
              <th className="border-b border-border px-4 py-3">Sucursal</th>
              <th className="border-b border-border px-4 py-3">Ingresos</th>
            </tr>
          </thead>
          <tbody>
            {filteredData.map((item) => (
              <tr key={item.branchId}>
                <td className="border-b border-border px-4 py-3">{item.branchName}</td>
                <td className="border-b border-border px-4 py-3">{currencyFormat(item.totalRevenue)}</td>
              </tr>
            ))}
            {filteredData.length === 0 && (
              <tr>
                <td colSpan={2} className="border-b border-border px-4 py-6 text-center text-text">
                  Sin resultados para la búsqueda.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </Card>
  );
}
