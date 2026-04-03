import Card from "../../../../components/Card";
import { currencyFormat } from "../branchPerformanceUtils";

export default function BranchPerformanceTable({ data }) {
  return (
    <Card className="border border-border bg-white p-4 text-text">
      <h2 className="mb-4 text-lg font-bold text-text">Tabla de sucursales</h2>
      <div className="overflow-x-auto rounded-lg border border-border">
        <table className="min-w-full bg-surface text-text">
          <thead>
            <tr className="text-left text-sm text-text-secondary">
              <th className="border-b border-border px-4 py-3">Sucursal</th>
              <th className="border-b border-border px-4 py-3">Ingresos</th>
            </tr>
          </thead>
          <tbody>
            {data.map((item) => (
              <tr key={item.branchId}>
                <td className="border-b border-border px-4 py-3">{item.branchName}</td>
                <td className="border-b border-border px-4 py-3">{currencyFormat(item.totalRevenue)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Card>
  );
}
