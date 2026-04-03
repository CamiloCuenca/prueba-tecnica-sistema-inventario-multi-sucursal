export const CHART_AXIS_STYLE = { fontSize: 12, fill: "var(--color-text)" };
export const CHART_LINE_STYLE = { stroke: "var(--color-border)" };
export const CHART_TOOLTIP_STYLE = {
  backgroundColor: "var(--color-background)",
  borderColor: "var(--color-border)",
  color: "var(--color-text)",
};

export function currencyFormat(value) {
  if (value == null) return "-";
  return value.toLocaleString("es-MX", {
    style: "currency",
    currency: "MXN",
    maximumFractionDigits: 0,
  });
}
