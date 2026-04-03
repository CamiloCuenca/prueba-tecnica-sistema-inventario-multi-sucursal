import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import {
  CHART_AXIS_STYLE,
  CHART_LINE_STYLE,
  CHART_TOOLTIP_STYLE,
  numberFormat,
} from "../salesVolumeUtils";

export default function SalesVolumeUnitsChart({ monthly }) {
  return (
    <div>
      <h2 className="mb-2 text-lg font-bold text-black">Tendencia de unidades vendidas</h2>
      <div className="h-75 w-full rounded-lg border border-border bg-white p-3">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={monthly} margin={{ top: 12, right: 12, left: 0, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
            <XAxis dataKey="monthLabel" tick={CHART_AXIS_STYLE} tickLine={CHART_LINE_STYLE} axisLine={CHART_LINE_STYLE} />
            <YAxis
              tickFormatter={(value) => numberFormat(value)}
              tick={CHART_AXIS_STYLE}
              tickLine={CHART_LINE_STYLE}
              axisLine={CHART_LINE_STYLE}
            />
            <Tooltip
              formatter={(value) => [numberFormat(value), "Unidades vendidas"]}
              contentStyle={CHART_TOOLTIP_STYLE}
              labelStyle={{ color: "#ffffff" }}
              itemStyle={{ color: "#ffffff" }}
            />
            <Bar dataKey="units" fill="var(--color-primary-hover)" />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
