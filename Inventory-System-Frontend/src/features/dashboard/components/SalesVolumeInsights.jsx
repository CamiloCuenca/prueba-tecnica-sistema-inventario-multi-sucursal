import Card from "../../../components/Card";
import { growthLabel } from "../salesVolumeUtils";

export default function SalesVolumeInsights({ growthVsPrevious, trendDirection, selectedRangeLabel }) {
  return (
    <Card className="rounded-lg border border-border bg-white p-4 text-black">
      <p className="text-sm text-black">Insights</p>
      <p className="mt-2">
        Crecimiento vs mes anterior: <span className="font-semibold">{growthLabel(growthVsPrevious)}</span>
      </p>
      <p className="mt-1">
        La tendencia de ingresos actual es <span className="font-semibold">{trendDirection}</span>.
      </p>
      <p className="mt-1 text-black">Comparacion calculada para el rango seleccionado {selectedRangeLabel}.</p>
    </Card>
  );
}
