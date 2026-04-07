import BranchList from '../../inventory/BranchList';

export default function BranchSelector({ isAdmin, selectedBranchId, setSelectedBranchId }) {
  if (!isAdmin) return null;
  return (
    <div className="space-y-3">
      <h2 className="text-lg font-semibold text-gray-900">Selecciona sucursal</h2>
      <BranchList
        selectedBranchId={selectedBranchId}
        onBranchSelect={setSelectedBranchId}
      />
      {!selectedBranchId && (
        <p className="text-sm text-gray-600">Selecciona una sucursal para ver sus transferencias activas.</p>
      )}
    </div>
  );
}
