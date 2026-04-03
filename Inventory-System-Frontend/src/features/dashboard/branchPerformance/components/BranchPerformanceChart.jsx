import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import Card from "../../../../components/Card";
import { CHART_AXIS_STYLE, CHART_LINE_STYLE, CHART_TOOLTIP_STYLE, currencyFormat } from "../branchPerformanceStyles";

export default function BranchPerformanceChart({ data }) {
  return (
    <Card className="border border-border bg-white p-4 text-black">
      <h2 className="mb-4 text-lg font-bold text-black">Rendimiento por sucursal</h2>
      <div className="h-75 w-full rounded-lg border border-border bg-white p-3">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={data} margin={{ top: 12, right: 12, left: 0, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
            <XAxis dataKey="branchName"   tick={{ fill: "#000" }} tickLine={CHART_LINE_STYLE} axisLine={CHART_LINE_STYLE} />
            <YAxis tickFormatter={(value) => currencyFormat(value)}   tick={{ fill: "#000" }} tickLine={CHART_LINE_STYLE} axisLine={CHART_LINE_STYLE} />
            <Tooltip
              formatter={(value) => [currencyFormat(value), "Ingresos"]}
              contentStyle={CHART_TOOLTIP_STYLE}
              labelStyle={{ color: "var(--color-text)" }}
              itemStyle={{ color: "var(--color-text)" }}
            />
            <Bar dataKey="totalRevenue" fill="var(--color-primary)" />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </Card>
  );
}
