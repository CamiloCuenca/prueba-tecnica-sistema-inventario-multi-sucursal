import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import {
  CHART_AXIS_STYLE,
  CHART_LINE_STYLE,
  CHART_TOOLTIP_STYLE,
  currencyFormat,
  numberFormat,
} from "../salesVolumeUtils";

export default function SalesVolumeRevenueChart({ monthly }) {
  return (
    <div>
      <h2 className="mb-2 text-lg font-bold text-black">Tendencia de ingresos mensuales</h2>
      <div className="h-75 w-full rounded-lg border border-border bg-white p-3">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={monthly} margin={{ top: 12, right: 12, left: 0, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
            <XAxis dataKey="monthLabel" tick={CHART_AXIS_STYLE} tickLine={CHART_LINE_STYLE} axisLine={CHART_LINE_STYLE} />
            <YAxis
              tickFormatter={(value) => numberFormat(value)}
              tick={CHART_AXIS_STYLE}
              tickLine={CHART_LINE_STYLE}
              axisLine={CHART_LINE_STYLE}
            />
            <Tooltip
              formatter={(value, name) => [
                name === "revenue" ? currencyFormat(value) : numberFormat(value),
                name === "revenue" ? "Ingresos" : "Unidades",
              ]}
              contentStyle={CHART_TOOLTIP_STYLE}
              labelStyle={{ color: "#ffffff" }}
              itemStyle={{ color: "#ffffff" }}
            />
            <Line type="monotone" dataKey="revenue" stroke="var(--color-primary)" strokeWidth={3} dot={{ r: 3 }} />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
