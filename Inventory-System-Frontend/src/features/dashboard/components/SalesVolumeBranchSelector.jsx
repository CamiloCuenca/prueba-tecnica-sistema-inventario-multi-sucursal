import BranchCard from "../../inventory/branchCard";

export default function SalesVolumeBranchSelector({
  loading,
  error,
  branches,
  selectedBranchId,
  onSelect,
}) {
  return (
    <div>
      <h2 className="mb-2 text-lg font-bold text-black">Selecciona sucursal</h2>
      {loading && <div className="w-full py-4 text-center text-black">Cargando sucursales...</div>}
      {error && <div className="w-full py-4 text-center text-red-500">{error}</div>}
      {!loading && !error && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {branches.map((branch) => (
            <BranchCard
              key={branch.id}
              data={branch}
              isCurrent={selectedBranchId === branch.id}
              onSelect={onSelect}
            />
          ))}
          {branches.length === 0 && (
            <div className="col-span-full py-4 text-center text-black">
              No hay sucursales disponibles para seleccionar.
            </div>
          )}
        </div>
      )}
    </div>
  );
}
