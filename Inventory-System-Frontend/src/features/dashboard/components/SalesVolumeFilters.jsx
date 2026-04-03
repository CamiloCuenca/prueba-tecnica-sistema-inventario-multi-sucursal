import Card from "../../../components/Card";

export default function SalesVolumeFilters({
  fromMonth,
  toMonth,
  onFromChange,
  onToChange,
  selectedRangeLabel,
  onSetQuickRange,
}) {
  return (
    <Card className="border border-border bg-white p-4 text-black">
      <div className="flex flex-col gap-4">
        <div className="grid grid-cols-1 items-end gap-4 md:grid-cols-3">
          <label className="flex flex-col gap-2 text-sm font-medium text-black">
            Mes inicial
            <input
              type="month"
              value={fromMonth}
              onChange={(event) => onFromChange(event.target.value)}
              className="rounded-lg border border-border bg-white px-3 py-2 text-black focus:border-primary focus:outline-none"
            />
          </label>

          <label className="flex flex-col gap-2 text-sm font-medium text-black">
            Mes final
            <input
              type="month"
              value={toMonth}
              onChange={(event) => onToChange(event.target.value)}
              className="rounded-lg border border-border bg-white px-3 py-2 text-black focus:border-primary focus:outline-none"
            />
          </label>

          <div className="rounded-lg border border-border bg-white px-3 py-2 text-sm text-black">
            Rango activo: <span className="font-semibold text-black">{selectedRangeLabel}</span>
          </div>
        </div>

        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            onClick={() => onSetQuickRange(-2)}
            className="rounded-full border border-border px-3 py-1.5 text-sm font-semibold text-black hover:border-primary"
          >
            Ultimos 3 meses
          </button>
          <button
            type="button"
            onClick={() => onSetQuickRange(-5)}
            className="rounded-full border border-border px-3 py-1.5 text-sm font-semibold text-black hover:border-primary"
          >
            Ultimos 6 meses
          </button>
          <button
            type="button"
            onClick={() => onSetQuickRange(-11)}
            className="rounded-full border border-border px-3 py-1.5 text-sm font-semibold text-black hover:border-primary"
          >
            Ultimos 12 meses
          </button>
        </div>
      </div>
    </Card>
  );
}
