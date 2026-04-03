import Card from "../../../components/Card";
import { currencyFormat, growthLabel, numberFormat } from "../salesVolumeUtils";

export default function SalesVolumeKpiCards({ currentMonth, growthVsPrevious }) {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
      <Card className="border border-border bg-white p-4 text-black">
        <p className="text-sm text-black">Ingresos del mes final</p>
        <p className="mt-2 text-2xl font-bold">{currencyFormat(currentMonth.revenue)}</p>
      </Card>

      <Card className="border border-border bg-white p-4 text-black">
        <p className="text-sm text-black">Unidades vendidas del mes final</p>
        <p className="mt-2 text-2xl font-bold">{numberFormat(currentMonth.units)}</p>
      </Card>

      <Card className="border border-border bg-white p-4 text-black">
        <p className="text-sm text-black">Crecimiento vs mes anterior</p>
        <p className="mt-2 text-2xl font-bold text-black">{growthLabel(growthVsPrevious)}</p>
      </Card>
    </div>
  );
}
