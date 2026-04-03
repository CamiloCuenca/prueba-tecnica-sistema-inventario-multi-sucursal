import Card from "../../../components/Card";
import BranchPerformanceChart from "./components/BranchPerformanceChart";
import BranchPerformanceKpi from "./components/BranchPerformanceKpi";
import BranchPerformanceTable from "./components/BranchPerformanceTable";
import { sortByRevenueDesc } from "./branchPerformanceUtils";
import { useBranchPerformance } from "./useBranchPerformance";

export default function BranchPerformanceDashboard() {
  const { data, loading, error } = useBranchPerformance();

  if (loading) {
    return <div className="w-full py-8 text-center text-black">Cargando rendimiento de sucursales...</div>;
  }

  if (error) {
    return <div className="w-full py-8 text-center text-red-500">{error}</div>;
  }

  const sortedData = sortByRevenueDesc(data);

  if (sortedData.length === 0) {
    return <div className="w-full py-8 text-center text-black">No hay rendimiento de sucursales para mostrar.</div>;
  }

  const topBranch = sortedData[0];

  return (
    <div className="space-y-8">
      <BranchPerformanceKpi topBranch={topBranch} />
      <BranchPerformanceChart data={sortedData} />
      <BranchPerformanceTable data={sortedData} />
    </div>
  );
}
