import Card from "../../../../components/Card";
import { currencyFormat } from "../branchPerformanceUtils";

export default function BranchPerformanceKpi({ topBranch }) {
  return (
    <Card className="border border-border bg-white p-4 text-black">
      <p className="text-sm text-black">Sucursal con mayor ingreso</p>
      <p className="mt-2 text-xl font-bold">{topBranch?.branchName ?? "-"}</p>
      <p className="mt-1 text-sm text-black">
        Ingresos: <span className="font-semibold text-black">{currencyFormat(topBranch?.totalRevenue)}</span>
      </p>
    </Card>
  );
}
