import { currencyFormat, numberFormat } from "../salesVolumeUtils";

export default function SalesVolumeTopProductsTable({ latestMonthProducts }) {
  return (
    <div>
      <h2 className="mb-2 text-lg font-bold text-black">Top productos del mes actual</h2>
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
            {latestMonthProducts.map((item, index) => (
              <tr key={`${item.productId}-${index}`}>
                <td className="border-b border-border px-4 py-3">{item.productName}</td>
                <td className="border-b border-border px-4 py-3">{numberFormat(item.totalUnitsSold)}</td>
                <td className="border-b border-border px-4 py-3">{currencyFormat(item.totalRevenue)}</td>
              </tr>
            ))}
            {latestMonthProducts.length === 0 && (
              <tr>
                <td colSpan={3} className="px-4 py-6 text-center text-black">
                  Sin productos para el mes actual.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
