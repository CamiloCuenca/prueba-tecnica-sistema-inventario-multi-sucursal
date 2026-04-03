export function numberFormat(value) {
  if (value == null) return "-";
  return value.toLocaleString("es-MX");
}

export function currencyFormat(value) {
  if (value == null) return "-";
  return value.toLocaleString("es-MX", {
    style: "currency",
    currency: "MXN",
    maximumFractionDigits: 0,
  });
}

export function sortByRevenueDesc(rows) {
  return [...rows].sort((a, b) => (Number(b.totalRevenue) || 0) - (Number(a.totalRevenue) || 0));
}
