import { useMemo, useState } from 'react';
import { currencyFormat, numberFormat } from "../salesVolumeUtils";

const normalizeText = (value) => String(value ?? '').toLowerCase();

export default function SalesVolumeTopProductsTable({ latestMonthProducts }) {
  const [search, setSearch] = useState('');

  const filteredProducts = useMemo(() => {
    const normalizedSearch = normalizeText(search).trim();
    if (!normalizedSearch) return latestMonthProducts;

    return latestMonthProducts.filter((item) =>
      normalizeText(item.productName).includes(normalizedSearch) ||
      normalizeText(item.productId).includes(normalizedSearch)
    );
  }, [latestMonthProducts, search]);

  return (
    <div>
      <h2 className="mb-2 text-lg font-bold text-black">Top productos del mes actual</h2>
      <div className="mb-3 w-full max-w-md">
        <label className="mb-1 block text-sm font-medium text-black">Buscar</label>
        <input
          type="search"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Buscar por producto o ID"
          className="w-full rounded border border-border px-3 py-2 text-sm text-black focus:outline-none focus:ring-2 focus:ring-primary/20"
        />
      </div>
      <div className="overflow-x-auto rounded-lg border border-border">
        <table className="min-w-full bg-white text-black">
          <thead>
            <tr className="text-left text-sm text-black">
              <th className="border-b border-border px-4 py-3">Producto</th>
              <th className="border-b border-border px-4 py-3">Unidades vendidas</th>
              <th className="border-b border-border px-4 py-3">Ingresos</th>
            </tr>
          </thead>
          <tbody>
            {filteredProducts.map((item, index) => (
              <tr key={`${item.productId}-${index}`}>
                <td className="border-b border-border px-4 py-3">{item.productName}</td>
                <td className="border-b border-border px-4 py-3">{numberFormat(item.totalUnitsSold)}</td>
                <td className="border-b border-border px-4 py-3">{currencyFormat(item.totalRevenue)}</td>
              </tr>
            ))}
            {filteredProducts.length === 0 && (
              <tr>
                <td colSpan={3} className="px-4 py-6 text-center text-black">
                  Sin productos para mostrar.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
