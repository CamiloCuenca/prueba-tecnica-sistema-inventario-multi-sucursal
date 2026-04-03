export const MONTH_LABEL_FORMAT = new Intl.DateTimeFormat("es-ES", { month: "short", year: "2-digit" });

export const DEFAULT_END = new Date();
export const DEFAULT_START = new Date(DEFAULT_END.getFullYear(), DEFAULT_END.getMonth() - 1, 1);

export const CHART_AXIS_STYLE = { fontSize: 12, fill: "#000" };
export const CHART_LINE_STYLE = { stroke: "#000" };
export const CHART_TOOLTIP_STYLE = {
  backgroundColor: "var(--color-background)",
  borderColor: "var(--color-border)",
  color: "#000",
};

export function toMonthInputValue(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  return `${year}-${month}`;
}

export function shiftMonthValue(monthValue, deltaMonths) {
  const [yearString, monthString] = monthValue.split("-");
  const year = Number(yearString);
  const month = Number(monthString);

  if (!year || !month) return monthValue;
  return toMonthInputValue(new Date(year, month - 1 + deltaMonths, 1));
}

export function monthInputToDateRange(monthValue, bound) {
  const [yearString, monthString] = monthValue.split("-");
  const year = Number(yearString);
  const month = Number(monthString);

  if (!year || !month) return null;
  if (bound === "start") return `${year}-${monthString}-01`;

  const lastDay = new Date(year, month, 0).getDate();
  return `${year}-${monthString}-${String(lastDay).padStart(2, "0")}`;
}

export function currencyFormat(value) {
  if (value == null) return "-";
  return value.toLocaleString("es-MX", {
    style: "currency",
    currency: "MXN",
    maximumFractionDigits: 0,
  });
}

export function numberFormat(value) {
  if (value == null) return "-";
  return value.toLocaleString("es-MX");
}

export function growthLabel(growth) {
  if (growth == null) return "N/A";
  const sign = growth > 0 ? "+" : "";
  return `${sign}${growth.toFixed(1)}%`;
}

export function aggregateByMonth(rows) {
  const grouped = new Map();

  rows.forEach((item) => {
    const month = Number(item.month);
    const year = Number(item.year);
    if (!month || !year) return;

    const key = `${year}-${String(month).padStart(2, "0")}`;
    const current = grouped.get(key) ?? {
      key,
      year,
      month,
      date: new Date(year, month - 1, 1),
      revenue: 0,
      units: 0,
    };

    current.revenue += Number(item.totalRevenue) || 0;
    current.units += Number(item.totalUnitsSold) || 0;
    grouped.set(key, current);
  });

  return Array.from(grouped.values())
    .sort((a, b) => a.date - b.date)
    .map((entry) => ({
      ...entry,
      monthLabel: MONTH_LABEL_FORMAT.format(entry.date),
    }));
}
